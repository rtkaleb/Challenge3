package org.example.scholar.controller;

import org.example.scholar.model.AuthorProfile;
import org.example.scholar.service.ScholarUtils;
import org.example.scholar.service.SerpApiClient;
import org.example.scholar.view.ConsoleView;

/**
 * AuthorController is part of the MVC pattern (Controller).
 * It coordinates between the Service (SerpApiClient) and the View (ConsoleView).
 * Responsibilities:
 *  - Accept user input (either author_id or Google Scholar profile URL).
 *  - Extract a valid author_id.
 *  - Fetch the author profile via the API client.
 *  - Forward results or errors to the View for display.
 */
public class AuthorController {
    private final SerpApiClient client; // Handles API requests
    private final ConsoleView view;     // Handles console output

    /**
     * Constructor for AuthorController.
     * @param client The SerpApi client used to make API requests.
     * @param view   The console view used to display results or errors.
     */
    public AuthorController(SerpApiClient client, ConsoleView view) {
        this.client = client;
        this.view = view;
    }

    /**
     * Given either an author_id or a Google Scholar profile URL,
     * attempt to extract the author_id, fetch the profile,
     * and display the results in the console.
     *
     * @param idOrUrl Either a plain author_id or a Google Scholar profile URL.
     */
    public void showAuthorByIdOrUrl(String idOrUrl) {
        try {
            // Assume input is already an author_id
            String authorId = idOrUrl;

            // If it does not look like a plain author_id, try extracting from URL
            if (!ScholarUtils.looksLikeAuthorId(idOrUrl)) {
                authorId = ScholarUtils.extractAuthorIdFromUrl(idOrUrl);
            }

            // If no valid author_id could be extracted, show an error
            if (authorId == null || authorId.isBlank()) {
                view.showError("Could not extract author_id. Please provide a valid profile URL or author_id.");
                return;
            }

            // Call the API client to fetch the profile
            AuthorProfile profile = client.getAuthorProfile(authorId);

            // Send the profile to the View for display
            view.showAuthorProfile(profile);

        } catch (Exception ex) {
            // Handle any unexpected error and show a friendly message
            view.showError(ex.getMessage());
        }
    }
}
