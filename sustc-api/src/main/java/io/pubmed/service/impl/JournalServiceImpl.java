package io.pubmed.service.impl;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.pubmed.dto.Journal;
import io.pubmed.service.JournalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class JournalServiceImpl implements JournalService {
    @Autowired
    private DataSource dataSource;
    /**
     * Please calculate the journal's Impact Factor(IF) at the given year.
     * At the given year, IF = the total number of citations of articles in the
     * given year / the total number of articles published in the journal in
     * the previous two years.
     * <p>
     * if year = 2024, you need sum citations of given journal in 2024 /
     * [2022-2023] published articles num in the journal.
     * Example:
     * IF（2024） = A / B
     * A = The number of times all articles in the journal from 2022 to 2023 were cited in 2024.
     * B = Number of articles in the journal from 2022 to 2023.
     *
     * @param journal_id need queried journal id
     * @param year          need queried year
     * @return the title of the required Journal
     */
    @Override
    public double getImpactFactor(String journal_id, int year) {
        //已测试1213
        String citationSql = """
        SELECT SUM(ac.citation_count) AS total_citations
        FROM Article_Citations ac
        JOIN Article_Journal aj ON ac.article_id = aj.article_id
        JOIN Journal j ON aj.journal_id = j.id
        WHERE j.id = ?
          AND ac.citation_year = ?
          AND EXISTS (
            SELECT 1
            FROM Article a
            WHERE a.id = aj.article_id
              AND EXTRACT(YEAR FROM a.date_created) BETWEEN ? AND ?
          );
    """;

        String articleCountSql = """
        SELECT COUNT(*) AS article_count
        FROM Article a
        JOIN Article_Journal aj ON a.id = aj.article_id
        JOIN Journal j ON aj.journal_id = j.id
        WHERE j.id = ?
          AND EXTRACT(YEAR FROM a.date_created) BETWEEN ? AND ?;
    """;

        try (Connection conn = dataSource.getConnection()) {
            // 查询总引用数 A
            int totalCitations = 0;
            try (PreparedStatement stmt = conn.prepareStatement(citationSql)) {
                stmt.setString(1, journal_id);
                stmt.setInt(2, year);
                stmt.setInt(3, year - 2);
                stmt.setInt(4, year - 1);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    totalCitations = rs.getInt("total_citations");
                }
            }

            // 查询文章总数 B
            int articleCount = 0;
            try (PreparedStatement stmt = conn.prepareStatement(articleCountSql)) {
                stmt.setString(1, journal_id);
                stmt.setInt(2, year - 2);
                stmt.setInt(3, year - 1);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    articleCount = rs.getInt("article_count");
                }
            }

            // 计算并返回影响因子
            if (articleCount > 0) {
                double ans = (double) totalCitations / articleCount;
                log.info("calculated IF: {}",ans);
                return ans;
            }
        } catch (SQLException e) {
            log.error("Error calculating impact factor for journal: {} in year: {}", journal_id, year, e);
        }

        return 0.0;  // 如果发生错误或无数据，返回0
    }


    /**
     * A journal changed its title from given year, but database data was not update,
     * please update the database, change the article's journal_title from given year
     * (include that year).
     *
     * @param journal  need update journal, only contain title and id fields
     * @param year     need update from and include year
     * @param new_name need update old journal tile to new_name
     * @param new_id   the new journal title's id
     * @return your implement success or not
     * Tips: After testing, you would better delete it from database for next testing.
     */
        /*
    问题如下。原journal中（id=0151424, title=Biochemical medicine, country=null, issn=null, issue=null)
                的country=null, issn=null, issue=null三列不为空，所以我是否应该通过journal的id=0151424信息来获取这个journal的所有属性并把它赋给更新后的journal？

     */
    @Override
    public boolean updateJournalName(Journal journal, int year, String new_name, String new_id) {
        // 第一步：查询旧 journal 的详细信息
        String fetchJournalSql = """
        SELECT country, issn, volume, issue
        FROM Journal
        WHERE id = ?
    """;

        // 第二步：插入新 journal 的 SQL
        String insertSql = """
        INSERT INTO Journal (id, title, country, issn, volume, issue)
        VALUES (?, ?, ?, ?, ?, ?)
        ON CONFLICT (id) DO NOTHING;
    """;

        // 第三步：更新 Article_Journal 的 journal_id
        String updateSql = """
        UPDATE Article_Journal aj
        SET journal_id = ?
        WHERE aj.journal_id = ? AND EXISTS (
            SELECT 1
            FROM Article a
            WHERE a.id = aj.article_id AND EXTRACT(YEAR FROM a.date_created)::INTEGER >= ? );
        """;

        try (Connection conn = dataSource.getConnection()) {
            // 获取未更新期刊的所有信息
            String country;
            String issn;
            String volume;
            String issue;

            try (PreparedStatement fetchStmt = conn.prepareStatement(fetchJournalSql)) {
                fetchStmt.setString(1, journal.getId());
                try (ResultSet rs = fetchStmt.executeQuery()) {
                    if (rs.next()) {
                        country = rs.getString("country");
                        issn = rs.getString("issn");
                        volume = rs.getString("volume");
                        issue = rs.getString("issue");
                    } else {
                        log.warn("Journal with id '{}' not found. Update aborted.", journal.getId());
                        return false; // 如果旧 journal 不存在，直接返回 false
                    }
                }
            }

            // 插入新期刊
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, new_id); // 新 journal_id
                insertStmt.setString(2, new_name); // 新 journal_title
                insertStmt.setString(3, country != null ? country : ""); // 继承旧期刊国家
                insertStmt.setString(4, issn != null ? issn : ""); // 继承旧期刊 ISSN
                insertStmt.setString(5, volume); // 继承旧期刊卷号
                insertStmt.setString(6, issue); // 继承旧期刊期号
                insertStmt.executeUpdate();
                log.info("Inserted new journal with id '{}' and title '{}'", new_id, new_name);
            }

            // 更新 Article_Journal 中的 journal_id
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, new_id); // 新 journal_id
                updateStmt.setString(2, journal.getId()); // 旧 journal_id
                updateStmt.setInt(3, year); // 更新年份条件

                int affectedRows = updateStmt.executeUpdate();

                log.info("Updated {} rows for journal title change to '{}'", affectedRows, new_name);
                return affectedRows > 0; // 如果有行更新成功，则返回 true
            }

        } catch (SQLException e) {
            log.error("Error updating journal name for journal: {} in year: {}", journal.getTitle(), year, e);
        }

        return false; // 如果发生错误或无更新，返回 false
    }


}
