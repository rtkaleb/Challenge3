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
    private static final String BASE = "https://serpapi.com/search";
    private final String apiKey;
    private final HttpClient http;
    private final ObjectMapper mapper = new ObjectMapper();

    public SerpApiClient(String apiKey) {
        this.apiKey = apiKey;
        this.http = HttpClients.createDefault();
    }

    public List<AuthorSearchResult> searchAuthors(String query) throws Exception {
        String url = BASE
                + "?engine=google_scholar_profiles"
                + "&mauthors=" + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpGet get = new HttpGet(url);
        var response = http.execute(get);
        int status = response.getCode();
        String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
        if (status != 200) {
            throw new RuntimeException("HTTP " + status + " al buscar autores: " + body);
        }

        JsonNode root = mapper.readTree(body);
        JsonNode profiles = root.path("profiles"); // SerpApi coloca resultados de perfiles aquí
        List<AuthorSearchResult> list = new ArrayList<>();
        if (profiles.isArray()) {
            for (JsonNode p : profiles) {
                AuthorSearchResult r = new AuthorSearchResult();
                r.setName(p.path("name").asText(null));
                r.setAuthorId(p.path("author_id").asText(null));
                r.setAffiliations(p.path("affiliations").asText(null));
                Integer cited = p.path("cited_by").path("value").isInt() ? p.path("cited_by").path("value").asInt() : null;
                r.setCitedBy(cited);
                list.add(r);
            }
        }
        return list;
    }

    public AuthorProfile getAuthorProfile(String authorId) throws Exception {
        String url = BASE
                + "?engine=google_scholar_author"
                + "&author_id=" + URLEncoder.encode(authorId, StandardCharsets.UTF_8)
                + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

        HttpGet get = new HttpGet(url);
        var response = http.execute(get);
        int status = response.getCode();
        String body = response.getEntity() != null ? EntityUtils.toString(response.getEntity()) : "";
        if (status != 200) {
            throw new RuntimeException("HTTP " + status + " al obtener perfil: " + body);
        }

        JsonNode root = mapper.readTree(body);

        AuthorProfile profile = new AuthorProfile();
        profile.setName(root.path("author").path("name").asText(null));
        profile.setAffiliations(root.path("author").path("affiliations").asText(null));

        // Métricas rápidas
        AuthorProfile.Metrics m = new AuthorProfile.Metrics();
        m.hIndex = root.path("cited_by").path("table").path(0).path("h_index").path("all").asInt(0);
        m.i10Index = root.path("cited_by").path("table").path(0).path("i10_index").path("all").asInt(0);
        m.citations = root.path("cited_by").path("table").path(0).path("citations").path("all").asInt(0);
        profile.setMetrics(m);

        // Algunos artículos (primeros 5)
        List<AuthorProfile.Article> articles = new ArrayList<>();
        JsonNode arts = root.path("articles");
        int count = 0;
        if (arts.isArray()) {
            for (JsonNode a : arts) {
                AuthorProfile.Article art = new AuthorProfile.Article();
                art.title = a.path("title").asText(null);
                art.link = a.path("link").asText(null);
                art.publication = a.path("publication").asText(null);
                art.year = a.path("year").isInt() ? a.path("year").asInt() : null;
                art.citedBy = a.path("cited_by").path("value").isInt() ? a.path("cited_by").path("value").asInt() : null;
                articles.add(art);
                if (++count >= 5) break;
            }
        }
        profile.setTopArticles(articles);
        return profile;
    }
}
