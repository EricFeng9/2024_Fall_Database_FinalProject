package io.pubmed.service;

import io.pubmed.dto.Article;

public interface ArticleService {

    /**
     * Find the number of citations for an article in a given year
     *
     * @param id the article's id
     * @param year need queried year
     * @return the number of article's citations in given year,
     */
    int getArticleCitationsByYear(int id, int year);


    /**
     * Fist, add one article to your database
     * Second, output the journal IF after adding this article
     * Third, delete the article from your database
     *
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
    double addArticleAndUpdateIF(Article article);
}
