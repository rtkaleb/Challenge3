package org.example.scholar.view;

import org.example.scholar.model.AuthorProfile;
import org.example.scholar.model.AuthorSearchResult;
import java.util.List;

public class ConsoleView {

    public void showWelcome() {
        System.out.println("=== Google Scholar (SerpApi) - Demo MVC ===");
    }

    public void showSearchQuery(String q) {
        System.out.println("\nBuscando autores por: \"" + q + "\"");
    }

    public void showSearchResults(List<AuthorSearchResult> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No se encontraron autores.");
            return;
        }
        System.out.println("\nResultados:");
        int i = 1;
        for (AuthorSearchResult r : results) {
            System.out.printf("%d) %s  [author_id=%s]%n   Afiliación: %s | Citas: %s%n",
                    i++, r.getName(), r.getAuthorId(),
                    nullToDash(r.getAffiliations()),
                    r.getCitedBy() == null ? "-" : r.getCitedBy());
        }
    }

    public void showAuthorProfile(AuthorProfile p) {
        System.out.println("\n=== PERFIL DEL AUTOR ===");
        System.out.println("Nombre: " + p.getName());
        System.out.println("Afiliación: " + nullToDash(p.getAffiliations()));
        if (p.getMetrics() != null) {
            System.out.printf("Citas: %d | h-index: %d | i10-index: %d%n",
                    safe(p.getMetrics().citations),
                    safe(p.getMetrics().hIndex),
                    safe(p.getMetrics().i10Index));
        }
        System.out.println("\nArtículos destacados:");
        if (p.getTopArticles() == null || p.getTopArticles().isEmpty()) {
            System.out.println("  (sin artículos)");
        } else {
            p.getTopArticles().forEach(a -> {
                System.out.printf(" - %s (%s) %s | Citas: %s%n",
                        a.title,
                        a.year == null ? "-" : a.year.toString(),
                        a.publication == null ? "" : " · " + a.publication,
                        a.citedBy == null ? "-" : a.citedBy.toString());
                System.out.println("   " + (a.link == null ? "" : a.link));
            });
        }
    }

    public void showError(String message) {
        System.out.println("\n[ERROR] " + message);
    }

    private static String nullToDash(String s) { return s == null ? "-" : s; }
    private static int safe(Integer i) { return i == null ? 0 : i; }
}
