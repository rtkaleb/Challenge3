package org.example.scholar;

import org.example.scholar.controller.AuthorController;
import org.example.scholar.service.SerpApiClient;
import org.example.scholar.view.ConsoleView;

/**
 * Main entry point of the Scholar MVC application.
 *
 * Responsibilities:
 *  - Initialize the MVC components (Controller, View, Service).
 *  - Load the SerpApi API key from environment variables.
 *  - Parse command-line arguments.
 *  - Trigger the author profile search and display process.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Google Scholar (SerpApi) - Demo MVC ===");

        // Load API key from environment variable
        String apiKey = System.getenv("SERPAPI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[ERROR] Please define the environment variable SERPAPI_KEY with your SerpApi key.");
            System.exit(1); // Exit program if no key is provided
        }

        // Show a masked version of the key for debugging (only last 4 chars visible)
        String tail = apiKey.length() >= 4 ? apiKey.substring(apiKey.length() - 4) : apiKey;
        System.out.println("(SERPAPI_KEY: ****" + tail + ")");

        // Check that at least one argument was provided
        if (args.length == 0) {
            System.out.println("[ERROR] Please provide an author_id or a Google Scholar profile URL as argument.");
            System.exit(1);
        }

        // The first argument is expected to be either author_id or Scholar profile URL
        String input = args[0];

        // === Initialize MVC components ===
        ConsoleView view = new ConsoleView();                  // View
        SerpApiClient client = new SerpApiClient(apiKey);      // Service
        AuthorController controller = new AuthorController(client, view); // Controller

        // === Trigger search and display ===
        controller.showAuthorByIdOrUrl(input);
    }
}
