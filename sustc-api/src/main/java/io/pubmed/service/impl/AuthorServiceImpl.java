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
        //bb上的不用加, ac.article_id DESC；db gen的要加
        String sql = """

      SELECT sum(COALESCE(ac.citation_count, 0)) AS citation_count, aa.article_id
      FROM Article_Authors aa
               LEFT JOIN Article_Citations ac ON aa.article_id = ac.article_id
               JOIN Authors a ON aa.author_id = a.author_id
      WHERE a.fore_name = ? AND a.last_name = ?
      group by aa.article_id
      ORDER BY citation_count DESC ;
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
        log.info("Starting getMinArticlesToLinkAuthors for Authors A: {}, E: {}", A, E);

        // Step 1: 获取两个作者的 author_id
        int authorIdA = getAuthorId(A);
        int authorIdE = getAuthorId(E);

        log.info("Author A ID: {}, Author E ID: {}", authorIdA, authorIdE);

        if (authorIdA == -1 || authorIdE == -1) {
            log.warn("One or both authors not found. Aborting.");
            return -1; // 如果任意作者不存在，返回 -1
        }
        if (authorIdA == authorIdE ) {
            log.warn("the two authors are same one");
            return -1; // 如果两个作者相同，返回 -1
        }
        // Step 2: 获取两个作者的文章列表
        List<Integer> articlesA = getArticlesByAuthor(authorIdA);
        List<Integer> articlesE = getArticlesByAuthor(authorIdE);
        log.info("Articles of Author A: {}", articlesA);
        log.info("Articles of Author E: {}", articlesE);

        if (articlesA.isEmpty() || articlesE.isEmpty()) {
            log.warn("One or both authors have no articles.");
            return -1; // 如果任意作者没有文章，返回 -1
        }

        // Step 3: 初始化 BFS
        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> visited = new HashSet<>();
        Map<Integer, Integer> distanceMap = new HashMap<>();

        for (int articleId : articlesA) {
            queue.offer(articleId);
            visited.add(articleId);
            distanceMap.put(articleId, 0); // 起始文章的距离为 0
        }
        log.info("Initialized BFS queue with articles: {}", articlesA);

        // Step 4: 执行 BFS 查找最短路径
        while (!queue.isEmpty()) {
            int currentArticleId = queue.poll();
            int currentDistance = distanceMap.get(currentArticleId);
            log.debug("Processing article ID: {}, Current Distance: {}", currentArticleId, currentDistance);

            // 如果当前文章属于目标作者的文章列表，则返回距离
            if (articlesE.contains(currentArticleId)) {
                log.info("Found connection at article ID: {}, Total Distance: {}", currentArticleId, currentDistance + 1);
                return currentDistance + 1; // 返回当前距离加 1
            }

            // 获取当前文章引用的其他文章
            List<Integer> referencedArticles = getCitedArticles(currentArticleId);
            log.debug("Referenced articles for {}: {}", currentArticleId, referencedArticles);

            // 遍历引用的文章，并加入队列
            for (int referencedArticleId : referencedArticles) {
                if (!visited.contains(referencedArticleId)) {
                    visited.add(referencedArticleId);
                    queue.offer(referencedArticleId);
                    distanceMap.put(referencedArticleId, currentDistance + 1);
                    log.debug("Added article ID: {} to queue with Distance: {}", referencedArticleId, currentDistance + 1);
                }
            }
        }

        // 如果未找到连接路径，返回 -1
        log.info("No connection found between Author A and Author E.");
        return -1;
    }

    private int getAuthorId(Author author) {
        log.info("Retrieving author ID for: {}", author);
        String sql = author.getInitials() != null
                ? "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ? AND initials = ?"
                : "SELECT author_id FROM Authors WHERE fore_name = ? AND last_name = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, author.getFore_name());
            stmt.setString(2, author.getLast_name());
            if (author.getInitials() != null) {
                stmt.setString(3, author.getInitials());
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int authorId = rs.getInt("author_id");
                log.info("Found author ID: {}", authorId);
                return authorId;
            }
        } catch (SQLException e) {
            log.error("Error retrieving author ID", e);
        }
        log.warn("Author not found: {}", author);
        return -1; // 未找到作者时返回 -1
    }

    private List<Integer> getArticlesByAuthor(int authorId) {
        log.info("Retrieving articles for author ID: {}", authorId);
        List<Integer> articles = new ArrayList<>();
        String sql = "SELECT article_id FROM Article_Authors WHERE author_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, authorId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int articleId = rs.getInt("article_id");
                articles.add(articleId);
            }
            log.info("Found articles for author ID {}: {}", authorId, articles);
        } catch (SQLException e) {
            log.error("Error getting articles for author", e);
        }
        return articles;
    }

    private List<Integer> getCitedArticles(int articleId) {
        log.debug("Retrieving cited articles for article ID: {}", articleId);
        List<Integer> citedArticles = new ArrayList<>();
        String sql = "SELECT reference_id FROM article_references WHERE article_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, articleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int referenceId = rs.getInt("reference_id");
                citedArticles.add(referenceId);
            }
            log.debug("Cited articles for {}: {}", articleId, citedArticles);
        } catch (SQLException e) {
            log.error("Error retrieving cited articles", e);
        }
        return citedArticles;
    }

}
