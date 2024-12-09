package io.pubmed.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The article information class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article implements Serializable {
    /**
     * Article's main id, pmid.
     */
    private int id;

    /**
     * The article's authors.
     */
    private Author[] authors;

    /**
     * The article's title.
     */
    private String title;

    /**
     * The article's keywords.
     */
    private String[] keywords;

    /**
     * Journal of this article.
     */
    private Journal journal;

    /**
     * List of article's references to other articles.
     */
    private String[] references;

    private ArticleID[] article_ids;

    private PublicationType[] publication_types;

    /**
     * Grants awarded to this article.
     */
    private Grant[] grants;

    /**
     * date_created
     */
    private Date created;

    /**
     * date_completed
     */
    private Date completed;

    /**
     * pub_model of this article.
     */
    private String pub_model;


}
