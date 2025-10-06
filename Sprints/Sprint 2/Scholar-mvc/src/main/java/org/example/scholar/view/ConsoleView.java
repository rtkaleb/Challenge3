package org.example.scholar.view;

import org.example.scholar.model.AuthorProfile;

/**
 * ConsoleView is responsible for displaying information of an AuthorProfile
 * and handling user-facing messages. This is the "View" part of the MVC pattern.
 */
public class ConsoleView {

    /**
     * Print the author profile information to the console.
     * @param profile AuthorProfile object containing scholar details.
     */
    public void showAuthorProfile(AuthorProfile profile) {
        if (profile == null) {
            System.out.println("[ERROR] No author profile available.");
            return;
        }

        System.out.println();
        System.out.println("=== AUTHOR PROFILE ===");
        System.out.println("Name: " + profile.getName());
        System.out.println("Affiliation: " + profile.getAffiliations());

        // Print metrics in a small table for clarity
        AuthorProfile.Metrics m = profile.getMetrics();
        if (m != null) {
            int citations = (m.citations != null) ? m.citations : 0;
            int hIndex = (m.hIndex != null) ? m.hIndex : 0;
            int i10Index = (m.i10Index != null) ? m.i10Index : 0;

            System.out.println();
            System.out.println("Metrics:");
            System.out.println("+----------------+---------+");
            System.out.printf("| %-14s | %7d |\n", "Citations", citations);
            System.out.printf("| %-14s | %7d |\n", "h-index", hIndex);
            System.out.printf("| %-14s | %7d |\n", "i10-index", i10Index);
            System.out.println("+----------------+---------+");
        }

        // Print top articles
        System.out.println();
        System.out.println("Top Articles:");
        if (profile.getTopArticles() != null && !profile.getTopArticles().isEmpty()) {
            for (AuthorProfile.Article a : profile.getTopArticles()) {
                System.out.println(" - " + a.title
                        + " (" + (a.year != null ? a.year : "-") + ") · "
                        + (a.publication != null ? a.publication : "-")
                        + " | Citations: " + (a.citedBy != null ? a.citedBy : 0));
                if (a.link != null) {
                    System.out.println("   " + a.link);
                }
            }
        } else {
            System.out.println("   [No articles found]");
        }
    }

    /**
     * Print an error message in a consistent format.
     * @param message The error text to display
     */
    public void showError(String message) {
        System.out.println("[ERROR] " + message);
    }
}
