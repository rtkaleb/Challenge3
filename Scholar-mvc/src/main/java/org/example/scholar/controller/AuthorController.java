package org.example.scholar.controller;

import org.example.scholar.model.AuthorProfile;
import org.example.scholar.model.AuthorSearchResult;
import org.example.scholar.service.SerpApiClient;
import org.example.scholar.view.ConsoleView;

import java.util.List;

public class AuthorController {
    private final SerpApiClient client;
    private final ConsoleView view;

    public AuthorController(SerpApiClient client, ConsoleView view) {
        this.client = client;
        this.view = view;
    }

    public void searchAndShow(String query) {
        try {
            view.showSearchQuery(query);
            List<AuthorSearchResult> results = client.searchAuthors(query);
            view.showSearchResults(results);

            if (results != null && !results.isEmpty()) {
                // Para principiantes: toma el primer resultado
                String authorId = results.get(0).getAuthorId();
                if (authorId != null && !authorId.isBlank()) {
                    AuthorProfile profile = client.getAuthorProfile(authorId);
                    view.showAuthorProfile(profile);
                } else {
                    view.showError("El resultado no tiene author_id.");
                }
            }
        } catch (Exception ex) {
            view.showError(ex.getMessage());
        }
    }
}
