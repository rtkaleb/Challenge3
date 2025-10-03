package org.example.scholar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorProfile {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metrics {
        public Integer hIndex;
        public Integer i10Index;
        public Integer citations;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Article {
        public String title;
        public String link;
        public String publication;
        public Integer year;
        public Integer citedBy;
    }

    private String name;
    private String affiliations;
    private Metrics metrics;
    private List<Article> topArticles;

    public String getName() { return name; }
    public String getAffiliations() { return affiliations; }
    public Metrics getMetrics() { return metrics; }
    public List<Article> getTopArticles() { return topArticles; }

    public void setName(String name) { this.name = name; }
    public void setAffiliations(String affiliations) { this.affiliations = affiliations; }
    public void setMetrics(Metrics metrics) { this.metrics = metrics; }
    public void setTopArticles(List<Article> topArticles) { this.topArticles = topArticles; }
}
