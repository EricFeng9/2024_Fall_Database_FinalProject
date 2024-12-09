package io.pubmed.service.impl;

import io.pubmed.dto.Article;
import io.pubmed.service.ArticleService;
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
        return 0;
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
        return 0;
    }
}
