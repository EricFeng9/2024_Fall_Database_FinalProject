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
import java.util.ArrayList;
import java.util.Arrays;
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
        SELECT COUNT(*) AS citation_count
        FROM Article_Citations ac
        WHERE ac.article_id = ? AND ac.citation_year = ?;
    """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, year);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("citation_count");
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
        // 插入新文章的 SQL
        String insertSql = """
        INSERT INTO Article (id, title, pub_model, date_created, date_completed)
        VALUES (?, ?, ?, ?, ?);
    """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // 设置文章基本信息
            insertStmt.setInt(1, article.getId());
            insertStmt.setString(2, article.getTitle());
            insertStmt.setString(3, article.getPub_model());
            insertStmt.setDate(4, article.getCreated() != null ? new java.sql.Date(article.getCreated().getTime()) : null);
            insertStmt.setDate(5, article.getCompleted() != null ? new java.sql.Date(article.getCompleted().getTime()) : null);

            // 插入文章记录
            int affectedRows = insertStmt.executeUpdate();
            if (affectedRows > 0) {
                // fjm 获取期刊ID 直接article.getJournal().getId()取不到，因为db gen给的样例只往article里放了title
                String journalId = getJournalIdBy_JournalName(article.getJournal());
                log.info(journalId);
                if (journalId != null) {
                    // 计算影响因子
                    //todo 计算影响因子的方法有点问题需要修改
                    double impactFactor = calculateImpactFactor(conn, journalId);
                    log.info(String.valueOf(impactFactor));
                    // 删除刚插入的文章
                    String deleteSql = "DELETE FROM Article WHERE id = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                        deleteStmt.setInt(1, article.getId());
                        int result = deleteStmt.executeUpdate();
                        log.info("成功删除" +result);
                    }

                    return impactFactor;  // 返回计算的影响因子
                }
            }

        } catch (SQLException e) {
            log.error("Error adding article and updating IF", e);
        }

        return 0.0;  // 如果发生错误或没有插入数据，返回0
    }

    private double calculateImpactFactor(Connection conn, String journalId) throws SQLException {
        // 计算 A：2022-2023年期间该期刊所有文章在2024年的引用次数
        String citationSql = """
        SELECT SUM(ac.citation_count) AS total_citations
        FROM Article_Citations ac
        JOIN Article a ON ac.article_id = a.id
        WHERE a.journal_id = ?
          AND EXTRACT(YEAR FROM a.date_created) BETWEEN 2022 AND 2023
          AND ac.citation_year = 2024;
    """;
        try (PreparedStatement stmt = conn.prepareStatement(citationSql)) {
            stmt.setString(1, journalId);  // 使用字符串类型的期刊ID
            ResultSet rs = stmt.executeQuery();
            int totalCitations = 0;
            if (rs.next()) {
                totalCitations = rs.getInt("total_citations");
            }

            // 计算 B：2022-2023年期间该期刊发布的文章数量
            String articleCountSql = """
            SELECT COUNT(*) AS article_count
            FROM Article a
            WHERE a.journal_id = ?
              AND EXTRACT(YEAR FROM a.date_created) BETWEEN 2022 AND 2023;
        """;
            try (PreparedStatement countStmt = conn.prepareStatement(articleCountSql)) {
                countStmt.setString(1, journalId);
                ResultSet countRs = countStmt.executeQuery();
                int articleCount = 0;
                if (countRs.next()) {
                    articleCount = countRs.getInt("article_count");
                }

                // 计算影响因子
                if (articleCount > 0) {
                    return (double) totalCitations / articleCount;
                }
            }
        }
        return 0.0;  // 如果没有有效数据，则返回0
    }
    private String getJournalIdBy_JournalName(Journal journal) {
        List<Integer> articles = new ArrayList<>();
        String sql = "SELECT id FROM Journal WHERE title = ?";
        String journalsId = null;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, journal.getTitle());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                journalsId = rs.getString("id");
            }
        } catch (SQLException e) {
            log.error("Error getting articles for author", e);
        }
        return journalsId;
    }
}
