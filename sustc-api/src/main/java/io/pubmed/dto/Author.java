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
public class Author implements Serializable {
    private String fore_name;

    private String last_name;

    private String initials;

    private String collective_name;

    public String getFore_name() {
        return fore_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setFore_name(String fore_name) {
        this.fore_name = fore_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    public void setCollective_name(String collective_name) {
        this.collective_name = collective_name;
    }
}
