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
public class Grant implements Serializable {
    private int id;

    private String acronym;

    private String country;
}
