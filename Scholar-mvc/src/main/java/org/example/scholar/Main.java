package org.example.scholar;

import org.example.scholar.controller.AuthorController;
import org.example.scholar.service.SerpApiClient;
import org.example.scholar.view.ConsoleView;

public class Main {
    public static void main(String[] args) {
        String apiKey = System.getenv("SERPAPI_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[ERROR] Define la variable de entorno SERPAPI_KEY con tu clave de SerpApi.");
            System.exit(1);
        }

        String query = (args.length > 0) ? String.join(" ", args) : "Geoffrey Hinton";
        ConsoleView view = new ConsoleView();
        view.showWelcome();

        SerpApiClient client = new SerpApiClient(apiKey);
        AuthorController controller = new AuthorController(client, view);

        controller.searchAndShow(query);
    }
}
