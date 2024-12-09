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
public class PublicationType implements Serializable {
    /**
     * Id of the publication type.
     */
    private String id;

    /**
     * Name of the publication type,
     */
    private String name;
}
