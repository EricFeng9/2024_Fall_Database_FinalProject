package io.pubmed.service.impl;

import io.pubmed.dto.Author;
import io.pubmed.service.AuthorService;
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
        return new int[0];
    }

    /**
     * In which journal has a given author published the most articles ?
     *
     * @param author the author to be queried
     * @return the title of the required Journal
     */
    @Override
    public String getJournalWithMostArticlesByAuthor(Author author) {
        return null;
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
        return 0;
    }
}
