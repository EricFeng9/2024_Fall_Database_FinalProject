package io.pubmed.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The article_id information class
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleID implements Serializable {
    /**
     * Type of the ArticleId. e.g.,
     * - "pubmed" means in this dataset, usually the same with article.id.
     * - "doi" means id in DOI Index.
     * - "pii" means id in the publisher's index system (dependent on publisher).
     * - and other types may exist.
     */
    private String ty;

    private String id;
}
