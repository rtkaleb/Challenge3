package org.example.scholar.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.example.scholar.model.AuthorProfile;
import org.example.scholar.model.AuthorSearchResult;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SerpApiClient {
    // Base URL for all SerpApi requests
    private static final String BASE = "https://serpapi.com/search";

    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SerpApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClients.createDefault(); // Create default Apache HttpClient
    }

    /**
     * Deprecated method: SerpApi has discontinued the "google_scholar_profiles" engine.
     * This method will throw an exception if called.
     */
    public List<AuthorSearchResult> searchAuthorsDeprecated(String query) throws Exception {
        throw new UnsupportedOperationException("The google_scholar_profiles API was discontinued by SerpApi.");
    }

    /**
     * Fetches the profile of a scholar author from SerpApi using their author_id.
     * @param authorId The Google Scholar author_id (extracted from profile URL).
     * @return AuthorProfile object with parsed information.
     */
    public AuthorProfile getAuthorProfile(String authorId) throws Exception {
        // Build the request URL
        String url = BASE
                + "?engine=google_scholar_author"
                + "&author_id=" + URLEncoder.encode(authorId, StandardCharsets.UTF_8)
                + "&hl=en"
                + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpGet get = new HttpGet(url);

        // Execute HTTP GET request and capture response body
        String body = http.execute(get, response -> {
            int status = response.getCode();
            String txt = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
            if (status >= 200 && status < 300) {
                return txt; // Return body if request was successful
            } else {
                throw new RuntimeException("HTTP " + status + " - " + txt); // Throw error with details
            }
        });

        // Parse JSON response
        JsonNode root = mapper.readTree(body);

        // Create an AuthorProfile object to store the parsed data
        AuthorProfile profile = new AuthorProfile();
        profile.setName(root.path("author").path("name").asText(null));          // Author name
        profile.setAffiliations(root.path("author").path("affiliations").asText(null)); // Author affiliation

        // === Extract citation metrics ===
        // SerpApi provides citations, h-index, and i10-index in separate objects inside "cited_by.table"
        AuthorProfile.Metrics m = new AuthorProfile.Metrics();
        JsonNode table = root.path("cited_by").path("table");

        if (table.isArray()) {
            for (JsonNode entry : table) {
                // Total citations
                if (entry.has("citations")) {
                    m.citations = entry.path("citations").path("all").asInt(0);
                }
                // h-index
                if (entry.has("h_index")) {
                    m.hIndex = entry.path("h_index").path("all").asInt(0);
                }
                // i10-index
                if (entry.has("i10_index")) {
                    m.i10Index = entry.path("i10_index").path("all").asInt(0);
                }
            }
        }
        profile.setMetrics(m);

        // === Extract top 5 articles ===
        // Each article contains title, link, publication, year, and citation count
        List<AuthorProfile.Article> articles = new ArrayList<>();
        JsonNode arts = root.path("articles");
        int count = 0;
        if (arts.isArray()) {
            for (JsonNode a : arts) {
                AuthorProfile.Article art = new AuthorProfile.Article();
                art.title = a.path("title").asText(null);                // Article title
                art.link = a.path("link").asText(null);                  // Article link
                art.publication = a.path("publication").asText(null);    // Journal or publisher
                art.year = a.path("year").isInt() ? a.path("year").asInt() : null; // Publication year
                art.citedBy = a.path("cited_by").path("value").isInt() ? a.path("cited_by").path("value").asInt() : null; // Citation count
                articles.add(art);
                if (++count >= 5) break; // Limit to 5 articles
            }
        }
        profile.setTopArticles(articles);

        return profile;
    }
}
