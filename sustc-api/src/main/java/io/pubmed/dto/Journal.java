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
public class Journal implements Serializable {
    private String id;

    private String title;

    private String country;

    private String issn;

    private JournalIssue issue;
}
