package io.pubmed.service.impl;

import io.pubmed.service.GrantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class GrantServiceImpl implements GrantService {
    @Autowired
    private DataSource dataSource;
    /**
     * Find all the funded articles by the given country
     *
     * @param country you need query
     * @return the pmid list of funded articles
     */
    @Override
    public int[] getCountryFundPapers(String country) {
        //已测试1213
        String sql = """
        SELECT ag.article_id
        FROM Article_Grants ag
        JOIN Grant_info gi ON ag.grant_id = gi.id
        WHERE gi.country = ?;
    """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, country);  // 设置国家参数

            ResultSet rs = stmt.executeQuery();
            List<Integer> articleIds = new ArrayList<>();

            // 遍历结果集，提取 article_id
            while (rs.next()) {
                articleIds.add(rs.getInt("article_id"));
            }

            // 将 List 转换为 int 数组
            return articleIds.stream().mapToInt(Integer::intValue).toArray();

        } catch (SQLException e) {
            log.error("Error fetching funded articles for country: {}", country, e);
        }

        return new int[0];  // 若发生错误，返回空数组
    }

}
