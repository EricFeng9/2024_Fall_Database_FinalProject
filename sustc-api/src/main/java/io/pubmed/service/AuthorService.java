package io.pubmed.service;

import io.pubmed.dto.Author;

public interface AuthorService {

    /**
     * sort given author's articles by citations, from more to less.
     *
     * @param author the author to be queried
     * @return a sorted list of pmid of author's articles
     */
    int[] getArticlesByAuthorSortedByCitations(Author author);

    /**
     * In which journal has a given author published the most articles ?
     *
     * @param author the author to be queried
     * @return the title of the required Journal
     */
    String getJournalWithMostArticlesByAuthor(Author author);

    /**
     * This is a bonus task, you need find the minimum number of articles
     * that two authors need to be linked by citations, for example author A to E:
     * [article a, authors {A, B, C, D }, references {b, c, d, e}]  ->
     * [article b : authors {B, F, G }, references {q, w, e ,r}]    ->
     * [article q : authors{E, H, ... }]
     * Here, author A need 3 articles to link to author E
     * @return the number of the required articles, if there is no connection, return -1
     * If you don't want impl this task, just return -1
     */
    int getMinArticlesToLinkAuthors(Author A, Author E);


}
