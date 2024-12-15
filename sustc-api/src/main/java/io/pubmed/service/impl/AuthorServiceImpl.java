package io.pubmed.service.impl;

import io.pubmed.dto.Author;
import io.pubmed.service.AuthorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.*;


import io.pubmed.service.DatabaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
/**
 * It's important to mark your implementation class with {@link Service} annotation.
 * As long as the class is annotated and implements the corresponding interface, you can place it under any package.
 */
@Service
@Slf4j
public class AuthorServiceImpl implements AuthorService {
    @Autowired
    private DataSource dataSource;
    /**
     * sort given author's articles by citations, from more to less.
     *
     * @param author the author to be queried
     * @return a sorted list of pmid of author's articles
     */

    @Override
    public int[] getArticlesByAuthorSortedByCitations(Author author) {
        //已测试1211
        String sql = """
        SELECT ac.article_id, ac.citation_count
        FROM Article_Authors  aa
        JOIN Article_Citations ac ON aa.article_id = ac.article_id
        JOIN Authors a ON aa.author_id = a.author_id
        WHERE a.fore_name = ? AND a.last_name = ?
        ORDER BY ac.citation_count DESC
    """;
        List<Integer> articleIds = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, author.getFore_name());
            stmt.setString(2, author.getLast_name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articleIds.add(rs.getInt("article_id"));
            }
        } catch (SQLException e) {
            log.error("Error retrieving articles sorted by citations for author: {}, {}",
                    author.getFore_name(), author.getLast_name(), e);
            throw new RuntimeException(e);
        }

        return articleIds.stream().mapToInt(i -> i).toArray();
    }







    /**
     * In which journal has a given author published the most articles ?
     *
     * @param author the author to be queried
     * @return the title of the required Journal
     */
    @Override
    public String getJournalWithMostArticlesByAuthor(Author author) {
        //已测试1211
        boolean hasInitials = author.getInitials() != null;
        // Step 1: Find the author_id using the given fore_name, last_name, and initials
        String findAuthorIdSql;
        if (hasInitials){
            findAuthorIdSql = "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ? AND initials = ?";
        }else findAuthorIdSql = "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt0 = conn.prepareStatement(findAuthorIdSql)) {

            // Set parameters for fore_name, last_name, and initials
            stmt0.setString(1, author.getFore_name());
            stmt0.setString(2, author.getLast_name());
            if (hasInitials){
                stmt0.setString(3, author.getInitials());
            }
            ResultSet rs0 = stmt0.executeQuery();

            // Step 2: If author_id is found, continue; otherwise, return a default message
            if (rs0.next()) {
                int authorId = rs0.getInt("author_id");

                // Step 3: Use the found author_id to query the journal with most articles
                String sql = "SELECT j.title, COUNT(*) AS article_count " +
                        "FROM Article_Authors aa " +
                        "JOIN Article_Journal aj ON aa.article_id = aj.article_id " +
                        "JOIN Journal j ON aj.journal_id = j.id " +
                        "WHERE aa.author_id = ? " +
                        "GROUP BY j.title " +
                        "ORDER BY article_count DESC " +
                        "LIMIT 1";

                try (PreparedStatement stmt1 = conn.prepareStatement(sql)) {
                    // Set the author_id for the journal query
                    stmt1.setInt(1, authorId);

                    // Execute the query and process the result
                    ResultSet rs1 = stmt1.executeQuery();

                    if (rs1.next()) {
                        return rs1.getString("title");
                    } else {
                        // No articles found for the author
                        return "No journals found for the given author.";
                    }
                }

            } else {
                // If author_id not found, return a default message
                return "Author not found.";
            }

        } catch (SQLException e) {
            // Handle SQL exceptions
            log.error("Error querying for journal with most articles by author", e);
            throw new RuntimeException("Error querying for journal with most articles", e);
        }
    }


    /**
     * This is a bonus task, you need find the minimum number of articles
     * that two authors need to be linked by citations, for example author A to E:
     * [article a, authors {A, B, C, D }, references {b, c, d, e}]  ->
     * [article b : authors {B, F, G }, references {q, w, e ,r}]    ->
     * [article q : authors{E, H, ... }]
     * Here, author A need 3 articles to link to author E
     *
     * @param A
     * @param E
     * @return the number of the required articles, if there is no connection, return -1
     * If you don't want impl this task, just return -1
     */
    @Override
    public int getMinArticlesToLinkAuthors(Author A, Author E) {
        //todo 找样例试一下,明明curdistance有值但是还是返回-1 为什么？

        // Step 1: Find author IDs using fore_name, last_name, and initials for both authors
        int authorIdA = getAuthorId(A);
        int authorIdE = getAuthorId(E);
        if (authorIdA == -1 || authorIdE == -1) {
            // If either author is not found, return -1
            return -1;
        }

        // Step 2: Get all articles written by Author A and Author E
        List<Integer> articlesA = getArticlesByAuthor(authorIdA);
        List<Integer> articlesE = getArticlesByAuthor(authorIdE);
        /*log.info(articlesA.toString());
        log.info(articlesE.toString());*/
        // Step 3: Use BFS to find the minimum number of articles to link A and E
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> articleDistance = new HashMap<>();

        // Enqueue all articles of Author A and initialize their distance as 0
        for (int articleId : articlesA) {
            queue.offer(articleId);
            visited.add(articleId);
            articleDistance.put(articleId, 0);  // Distance from Author A's articles
        }

        // Step 4: Perform BFS to explore the graph
        while (!queue.isEmpty()) {
            int currentArticleId = queue.poll();
            int currentDistance = articleDistance.get(currentArticleId);

            // Step 5: If we find an article written by Author E, return the distance
            if (articlesE.contains(currentArticleId)) {
                return currentDistance + 1;  // Add 1 because we found a link
            }

            // Step 6: Get articles cited by the current article
            List<Integer> referencedArticles = getCitedArticles(currentArticleId);

            // Step 7: Traverse the referenced articles and update the BFS queue
            for (int citedArticle : referencedArticles) {
                if (!visited.contains(citedArticle)) {
                    visited.add(citedArticle);
                    queue.offer(citedArticle);
                    articleDistance.put(citedArticle, currentDistance + 1);
                }
            }
            log.info(String.valueOf(currentDistance));
        }

        // If no connection is found, return -1
        return -1;
    }

    private int getAuthorId(Author author) {
        // Query the database to find the author_id for the given author
        boolean hasInitials = author.getInitials() != null;
        // Step 1: Find the author_id using the given fore_name, last_name, and initials
        String sql;
        if (hasInitials){
            sql = "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ? AND initials = ?";
        }else sql = "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, author.getFore_name());
            stmt.setString(2, author.getLast_name());
            if (hasInitials){
                stmt.setString(3, author.getInitials());
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("author_id");
            }
        } catch (SQLException e) {
            log.error("Error finding author ID", e);
        }
        return -1;  // Return -1 if the author is not found
    }

    private List<Integer> getArticlesByAuthor(int authorId) {
        List<Integer> articles = new ArrayList<>();
        String sql = "SELECT article_id FROM Article_Authors WHERE author_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                articles.add(rs.getInt("article_id"));
            }
        } catch (SQLException e) {
            log.error("Error getting articles for author", e);
        }
        return articles;
    }

    private List<Integer> getCitedArticles(int articleId) {
        List<Integer> citedArticles = new ArrayList<>();
        String sql = "SELECT reference_id FROM article_references WHERE article_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, articleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                citedArticles.add(rs.getInt("reference_id"));
            }
        } catch (SQLException e) {
            log.error("Error getting cited articles", e);
        }
        return citedArticles;
    }

}
