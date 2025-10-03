package org.example.scholar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorSearchResult {
    private String name;
    private String authorId;
    private String affiliations;
    private Integer citedBy;

    public String getName() { return name; }
    public String getAuthorId() { return authorId; }
    public String getAffiliations() { return affiliations; }
    public Integer getCitedBy() { return citedBy; }

    public void setName(String name) { this.name = name; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }
    public void setAffiliations(String affiliations) { this.affiliations = affiliations; }
    public void setCitedBy(Integer citedBy) { this.citedBy = citedBy; }
}
