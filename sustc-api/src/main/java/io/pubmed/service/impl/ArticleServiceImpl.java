package io.pubmed.service.impl;

import io.pubmed.dto.Article;
import io.pubmed.dto.Author;
import io.pubmed.dto.Journal;
import io.pubmed.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.pubmed.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {
    @Autowired
    private DataSource dataSource;
    /**
     * Find the number of citations for an article in a given year
     *
     * @param id   the article's id
     * @param year need queried year
     * @return the number of article's citations in given year,
     */
    @Override
    public int getArticleCitationsByYear(int id, int year) {
        //已测试1213
        String sql = """
          SELECT COALESCE(ac.citation_count, 0) AS citation_count
    FROM Article_Citations ac
    WHERE ac.article_id = ?
      AND ac.citation_year = ?;
    """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, year);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int totalCitations = rs.getInt("citation_count");
                log.info("Total citation count: {} for id: {} in year: {}", totalCitations, id, year);
                return totalCitations;
            }
        } catch (SQLException e) {
            log.error("Error fetching article citations by year", e);
        }
        return 0;  // Return 0 if no citations are found or an error occurs
    }

    /**
     * Fist, add one article to your database
     * Second, output the journal IF after adding this article
     * Third, delete the article from your database
     * <p>
     * if year = 2024, you need sum citations of given journal in 2024 /
     * [2022-2023] published articles num in the journal.
     * Example:
     * IF（2024） = A / B
     * A = The number of times all articles in the journal from 2022 to 2023 were cited in 2024.
     * B = Number of articles in the journal from 2022 to 2023.
     *
     * @param article all the article's info
     * @return the updated IF of given article's Journal
     */
    @Override
    public double addArticleAndUpdateIF(Article article) {
        String insertArticleSql = """
    INSERT INTO Article (id, title, pub_model, date_created, date_completed)
    VALUES (?, ?, ?, ?, ?);
    """;
        String insertReferenceSql = """
    INSERT INTO article_references (article_id, reference_id)
    VALUES (?, ?);
    """;
        String insertJournalArticleSql = """
    INSERT INTO article_journal (journal_id, article_id)
    VALUES (?, ?);
    """;
        String deleteArticleSql = "DELETE FROM Article WHERE id = ?";
        String deleteReferenceSql = "DELETE FROM article_references WHERE article_id = ?";
        String deleteJournalArticleSql = "DELETE FROM journal_article WHERE article_id = ?";

        try (Connection conn = dataSource.getConnection()) {
            // 禁用自动提交，开启事务
            conn.setAutoCommit(false);

            try (
                    PreparedStatement insertArticleStmt = conn.prepareStatement(insertArticleSql);
                    PreparedStatement insertReferenceStmt = conn.prepareStatement(insertReferenceSql);
                    PreparedStatement insertJournalArticleStmt = conn.prepareStatement(insertJournalArticleSql)
            ) {
                // 插入新文章
                insertArticleStmt.setInt(1, article.getId());
                insertArticleStmt.setString(2, article.getTitle());
                insertArticleStmt.setString(3, article.getPub_model());
                insertArticleStmt.setDate(4, article.getCreated() != null ? new java.sql.Date(article.getCreated().getTime()) : null);
                insertArticleStmt.setDate(5, article.getCompleted() != null ? new java.sql.Date(article.getCompleted().getTime()) : null);

                int affectedRows = insertArticleStmt.executeUpdate();
                if (affectedRows > 0) {
                    log.info("Inserted article with ID and createdtime: {},{}", article.getId(), article.getCreated());

                    // 插入 journal_article 关系表
                    String journalId = article.getJournal().getId();
                    if (journalId == null) {
                        log.warn("No journal found for article ID: {}", article.getId());
                        return 0.0;
                    }
                    insertJournalArticleStmt.setString(1, journalId);  // 期刊ID
                    insertJournalArticleStmt.setInt(2, article.getId());  // 文章ID
                    insertJournalArticleStmt.executeUpdate();

                    // 插入 references 数据
                    if (article.getReferences() != null && article.getReferences().length > 0) {
                        for (String reference : article.getReferences()) {
                            // 这里假设 `reference` 是参考文章的 ID
                            insertReferenceStmt.setInt(1, article.getId());
                            insertReferenceStmt.setInt(2, Integer.parseInt(reference));  // 假设 references 数组中存储的是文章 ID
                            insertReferenceStmt.addBatch();
                        }
                        insertReferenceStmt.executeBatch();  // 批量执行插入
                    }

                    // 提交事务（插入数据）
                    conn.commit();

                    // 计算影响因子
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(article.getCreated());
                    int createdYear = calendar.get(Calendar.YEAR) + 1;
                    log.info("Calculate from the year: {}", createdYear);

                    double impactFactor = calculateImpactFactor(conn, journalId, createdYear);
                    log.info("Calculated Impact Factor: {}", impactFactor);

                    // 返回影响因子
                    return impactFactor;
                } else {
                    // 插入失败，回滚事务
                    conn.rollback();
                    return 0.0;
                }
            } catch (SQLException e) {
                // 出现异常时回滚事务
                conn.rollback();
                log.error("Error adding article and updating IF", e);
                return 0.0;
            }
        } catch (SQLException e) {
            log.error("Database connection error", e);
            return 0.0;
        } finally {
            // 删除操作在返回影响因子后执行
            deleteInsertedData(article);
        }
    }

    private void deleteInsertedData(Article article) {
        String deleteArticleSql = "DELETE FROM Article WHERE id = ?";
        String deleteReferenceSql = "DELETE FROM article_references WHERE article_id = ?";
        String deleteJournalArticleSql = "DELETE FROM article_journal WHERE article_id = ?";

        try (Connection conn = dataSource.getConnection()) {
            // 禁用自动提交，开启事务
            conn.setAutoCommit(false);

            try (
                    PreparedStatement deleteArticleStmt = conn.prepareStatement(deleteArticleSql);
                    PreparedStatement deleteReferenceStmt = conn.prepareStatement(deleteReferenceSql);
                    PreparedStatement deleteJournalArticleStmt = conn.prepareStatement(deleteJournalArticleSql)
            ) {


                // 删除参考文献
                deleteReferenceStmt.setInt(1, article.getId());
                deleteReferenceStmt.executeUpdate();

                // 删除期刊和文章关联数据
                deleteJournalArticleStmt.setInt(1, article.getId());
                deleteJournalArticleStmt.executeUpdate();
                // 删除文章
                deleteArticleStmt.setInt(1, article.getId());
                deleteArticleStmt.executeUpdate();
                // 提交删除操作事务
                conn.commit();
                log.info("Successfully deleted article, references, and journal_article for article ID: {}", article.getId());
            } catch (SQLException e) {
                // 出现异常时回滚事务
                conn.rollback();
                log.error("Error deleting article, references, and journal_article", e);
            }
        } catch (SQLException e) {
            log.error("Database connection error", e);
        }
    }




    private double calculateImpactFactor(Connection conn, String journalId, int createdYear) throws SQLException {

// 将年份转换为字符串并拼接日期
        String startDateStr = (createdYear - 2) + "-01-01";
        String endDateStr = (createdYear - 1) + "-12-31";
        log.info("startDate: {}",startDateStr);
        log.info("endDate: {}",endDateStr);
// 将字符串转换为 java.sql.Date
        java.sql.Date startDate = java.sql.Date.valueOf(startDateStr);
        java.sql.Date endDate = java.sql.Date.valueOf(endDateStr);


        // 计算 A：文章的引用总次数（引用年份为2024年，文章创建年份在2022年到2023年之间）
        String citationSql = """
SELECT sum(ac.citation_count) as total_citations
FROM
    (SELECT aj.article_id
     FROM Article_Journal aj
              JOIN article a ON aj.article_id = a.id
     WHERE aj.journal_id = ?  -- 期刊ID
       AND a.date_created BETWEEN ? AND ?
    ) AS sa
        LEFT JOIN Article_Citations ac
                  ON sa.article_id = ac.article_id
                      AND ac.citation_year = ?;
""";

        int totalCitations = 0;
        try (PreparedStatement stmt = conn.prepareStatement(citationSql)) {
            stmt.setString(1, journalId);
            stmt.setDate(2, startDate);  // 设置动态的起始日期
            stmt.setDate(3, endDate);    // 设置动态的结束日期
            stmt.setInt(4,createdYear);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalCitations = rs.getInt("total_citations");
            }
        }

        // 计算 B：2022年到2023年发布的文章数量
        String articleCountSql = """
       SELECT count(DISTINCT a.id)  as article_count-- 使用 DISTINCT 以确保每篇文章只计数一次
       FROM Article a
                JOIN Article_Journal aj ON aj.article_id = a.id
       WHERE aj.journal_id = ? AND a.date_created BETWEEN ? AND ? ;
""";

        int articleCount = 0;
        try (PreparedStatement stmt = conn.prepareStatement(articleCountSql)) {
            stmt.setString(1, journalId);
            stmt.setDate(2, startDate);  // 设置动态的起始日期
            stmt.setDate(3, endDate);    // 设置动态的结束日期
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                articleCount = rs.getInt("article_count");
            }
        }

        // 计算影响因子
        if (articleCount > 0) {
            return (double) totalCitations / articleCount;
        }
        return 0.0; // 如果没有文章或引用，返回0
    }




}
