package org.example.scholar;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Uso: Main <AUTHOR_ID_1> <AUTHOR_ID_2> [MAX_ARTICLES]\n" +
                    "Ejemplo: Main LSsXyncAAAAJ AbCdEf123456 3");
            System.exit(1);
        }
        int max = args.length >= 3 ? Integer.parseInt(args[2]) : 3;

        String apiKey = System.getenv("SERPAPI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Define SERPAPI_API_KEY en variables de entorno.");
        }

        ScholarDb db = new ScholarDb("jdbc:sqlite:scholar.db");
        db.init(); // Crea la tabla si no existe

        ScholarClient client = new ScholarClient(apiKey);
        for (int i = 0; i < 2; i++) {
            // *** Importante: limpiar/extraer el ID por si pegaste la URL completa o viene con &hl=... ***
            String authorId = extractAuthorId(args[i]);

            ScholarClient.FetchResult fr = client.fetchTopArticles(authorId, max);
            db.saveAuthorArticles(authorId, fr.researcherName(), fr.articles());
            System.out.printf("Guardados %d artículos de %s (%s)%n",
                    fr.articles().size(), fr.researcherName(), authorId);
        }

        System.out.println("Listo. Archivo DB: scholar.db");
    }

    // --- Helper para aceptar tanto IDs limpios como URLs completas o IDs con &hl=... ---
    private static String extractAuthorId(String input) {
        if (input == null) return null;
        String s = input.trim();

        // Si ya parece un ID limpio (sin http ni &), devuélvelo
        if (!s.contains("http") && !s.contains("&") && s.matches("[A-Za-z0-9_-]+")) {
            return s;
        }
        // Si es URL: busca user=
        int i = s.indexOf("user=");
        if (i >= 0) {
            String after = s.substring(i + "user=".length());
            int amp = after.indexOf('&');
            return (amp >= 0 ? after.substring(0, amp) : after).trim();
        }
        // Si vino como ID&hl=es (u otro parámetro)
        int amp = s.indexOf('&');
        if (amp >= 0) s = s.substring(0, amp);

        return s.trim();
    }

    // --- Modelo de datos ---
    public record Article(
            String title,
            String authors,
            String publicationDate,
            String abs,
            String link,
            String keywords,
            Integer citedBy
    ) {}

    // --- Cliente SerpApi ---
    public static class ScholarClient {
        private static final String BASE = "https://serpapi.com/search.json";
        private final HttpClient http = HttpClient.newHttpClient();
        private final ObjectMapper mapper = new ObjectMapper();
        private final String apiKey;

        public ScholarClient(String apiKey) { this.apiKey = apiKey; }

        public FetchResult fetchTopArticles(String authorId, int max) throws Exception {
            String url = BASE + "?engine=google_scholar_author"
                    + "&author_id=" + URLEncoder.encode(authorId, StandardCharsets.UTF_8)
                    + "&num=" + max
                    + "&hl=en"
                    + "&api_key=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);

            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 429) {
                throw new RuntimeException("Rate limit (429). Reintenta más tarde.");
            }
            if (resp.statusCode() >= 400) {
                throw new RuntimeException("HTTP " + resp.statusCode() + ": " + resp.body());
            }

            JsonNode root = mapper.readTree(resp.body());
            String status = root.path("search_metadata").path("status").asText("");
            if (!status.equalsIgnoreCase("Success")) {
                String msg = root.path("error").asText("Error SerpApi desconocido");
                throw new RuntimeException("SerpApi status=\"" + status + "\": " + msg);
            }

            String researcherName = root.path("author").path("name").asText("");
            JsonNode articles = root.path("articles");
            List<Article> list = new ArrayList<>();
            for (int i = 0; i < Math.min(articles.size(), max); i++) {
                JsonNode a = articles.get(i);
                String title = a.path("title").asText("");
                String link = a.path("link").asText("");
                String authors = a.path("authors").asText("");
                String year = a.path("year").asText("");
                String publicationDate = year.isBlank() ? null : year + "-01-01";
                Integer citedBy = a.path("cited_by").path("value").isMissingNode()
                        ? null : a.path("cited_by").path("value").asInt();
                String abs = a.path("snippet").asText(null);
                String keywords = deriveKeywords(title);
                list.add(new Article(title, authors, publicationDate, abs, link, keywords, citedBy));
            }
            return new FetchResult(researcherName, list);
        }

        private static String deriveKeywords(String title) {
            if (title == null) return "";
            String[] tokens = title.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
            Set<String> stop = Set.of("the","a","an","of","and","for","to","in","on","with","by",
                    "from","using","based","analysis","study","model");
            LinkedHashSet<String> unique = new LinkedHashSet<>();
            for (String t : tokens) if (t.length() > 3 && !stop.contains(t)) unique.add(t);
            return String.join(",", unique.stream().limit(6).toList());
        }

        public record FetchResult(String researcherName, List<Article> articles) {}
    }

    // --- Capa de base de datos ---
    public static class ScholarDb {
        private final String url;
        public ScholarDb(String url) { this.url = url; }

        public void init() throws SQLException {
            try (Connection c = DriverManager.getConnection(url); Statement s = c.createStatement()) {
                s.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS articles (
                      id               INTEGER PRIMARY KEY AUTOINCREMENT,
                      researcher_id    TEXT    NOT NULL,
                      researcher_name  TEXT,
                      title            TEXT    NOT NULL,
                      authors          TEXT,
                      publication_date TEXT,
                      abstract         TEXT,
                      link             TEXT,
                      keywords         TEXT,
                      cited_by         INTEGER,
                      created_at       TEXT DEFAULT (datetime('now')),
                      UNIQUE(researcher_id, title)
                    );
                """);
            }
        }

        public void saveAuthorArticles(String researcherId, String researcherName, List<Article> items) throws SQLException {
            String sql = """
                INSERT INTO articles (researcher_id, researcher_name, title, authors, publication_date, abstract, link, keywords, cited_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(researcher_id, title) DO UPDATE SET
                    authors=excluded.authors,
                    publication_date=excluded.publication_date,
                    abstract=excluded.abstract,
                    link=excluded.link,
                    keywords=excluded.keywords,
                    cited_by=excluded.cited_by,
                    researcher_name=excluded.researcher_name
            """;
            try (Connection c = DriverManager.getConnection(url); PreparedStatement ps = c.prepareStatement(sql)) {
                c.setAutoCommit(false);
                for (Article a : items) {
                    ps.setString(1, researcherId);
                    ps.setString(2, researcherName);
                    ps.setString(3, a.title());
                    ps.setString(4, a.authors());
                    ps.setString(5, a.publicationDate());
                    ps.setString(6, a.abs());
                    ps.setString(7, a.link());
                    ps.setString(8, a.keywords());
                    if (a.citedBy() == null) ps.setNull(9, Types.INTEGER); else ps.setInt(9, a.citedBy());
                    ps.addBatch();
                }
                ps.executeBatch();
                c.commit();
            }
        }
    }
}
