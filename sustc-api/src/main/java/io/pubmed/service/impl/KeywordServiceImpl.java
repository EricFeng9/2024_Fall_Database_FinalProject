package io.pubmed.service.impl;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import io.pubmed.service.KeywordService;
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
public class KeywordServiceImpl implements KeywordService {
    @Autowired
    private DataSource dataSource;
    /**
     * Find the number of published articles containing a keyword over the past year, in descending order by year.
     *
     * @param keyword the keyword need queried
     * @return article's numbers in past years which contains given keyword.
     */
    @Override
    public int[] getArticleCountByKeywordInPastYears(String keyword) {
        String sql = """
        SELECT EXTRACT(YEAR FROM a.date_created) AS publication_year, COUNT(*) AS article_count
        FROM Article a
        JOIN Article_Keywords ak ON a.id = ak.article_id
        JOIN Keywords k ON ak.keyword_id = k.id
        WHERE k.keyword = ?
        GROUP BY EXTRACT(YEAR FROM a.date_created)
        ORDER BY publication_year DESC;
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 设置查询参数
            stmt.setString(1, keyword);

            // 执行查询
            ResultSet rs = stmt.executeQuery();
            //log.info(rs.toString());
            // 收集查询结果
            List<Integer> counts = new ArrayList<>();
            while (rs.next()) {
                int articleCount = rs.getInt("article_count");
                counts.add(articleCount);
            }

            // 将结果转换为数组
            return counts.stream().mapToInt(Integer::intValue).toArray();

        } catch (SQLException e) {
            log.error("Error retrieving article counts for keyword: {}", keyword, e);
        }

        // 发生错误时返回空数组
        return new int[0];
    }

}
