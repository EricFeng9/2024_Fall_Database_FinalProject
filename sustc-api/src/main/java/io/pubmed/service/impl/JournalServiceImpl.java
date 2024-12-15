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
     * @param journal_title need queried journal title
     * @param year          need queried year
     * @return the title of the required Journal
     */
    @Override
    public double getImpactFactor(String journal_title, int year) {
        //已测试1213
        String citationSql = """
        SELECT SUM(ac.citation_count) AS total_citations
        FROM Article_Citations ac
        JOIN Article_Journal aj ON ac.article_id = aj.article_id
        JOIN Journal j ON aj.journal_id = j.id
        WHERE j.title = ?
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
        WHERE j.title = ?
          AND EXTRACT(YEAR FROM a.date_created) BETWEEN ? AND ?;
    """;

        try (Connection conn = dataSource.getConnection()) {
            // 查询总引用数 A
            int totalCitations = 0;
            try (PreparedStatement stmt = conn.prepareStatement(citationSql)) {
                stmt.setString(1, journal_title);
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
                stmt.setString(1, journal_title);
                stmt.setInt(2, year - 2);
                stmt.setInt(3, year - 1);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    articleCount = rs.getInt("article_count");
                }
            }

            // 计算并返回影响因子
            if (articleCount > 0) {
                return (double) totalCitations / articleCount;
            }
        } catch (SQLException e) {
            log.error("Error calculating impact factor for journal: {} in year: {}", journal_title, year, e);
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
    @Override
    public boolean updateJournalName(Journal journal, int year, String new_name, String new_id) {
        //已测试1213
        String updateSql = """
        WITH UpdatedJournal AS (
            INSERT INTO Journal (id, title, country, issn)
            VALUES (?, ?, '', '')
            ON CONFLICT (id) DO NOTHING
        )
        UPDATE Article_Journal aj
        SET journal_id = ?
        WHERE aj.journal_id = ? AND EXISTS (
            SELECT 1
            FROM Article a
            WHERE a.id = aj.article_id AND EXTRACT(YEAR FROM a.date_created) >= ?
        );
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            stmt.setString(1, new_id);  // 新的 journal_id
            stmt.setString(2, new_name);  // 新的 journal_title
            stmt.setString(3, new_id);  // 更新到的新 journal_id
            stmt.setString(4, journal.getId());  // 原 journal_id
            stmt.setInt(5, year);  // 更新年份条件

            int affectedRows = stmt.executeUpdate();
            log.info("Updated {} rows for journal title change to {}", affectedRows, new_name);
            return affectedRows > 0;
        } catch (SQLException e) {
            log.error("Error updating journal name for journal: {} in year: {}", journal.getTitle(), year, e);
        }

        return false;  // 如果发生错误或无更新，返回 false
    }

}
