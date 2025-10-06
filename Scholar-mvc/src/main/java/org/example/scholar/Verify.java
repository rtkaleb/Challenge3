package org.example.scholar;

import java.sql.*;

public class Verify {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:scholar.db";
        try (Connection c = DriverManager.getConnection(url)) {
            // Conteo por autor
            System.out.println("== Conteo por researcher_id ==");
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("""
                     SELECT researcher_id, COUNT(*) AS n_rows
                     FROM articles
                     GROUP BY researcher_id
                 """)) {
                while (rs.next()) {
                    System.out.printf("%s -> %d filas%n",
                            rs.getString("researcher_id"),
                            rs.getInt("n_rows"));
                }
            }

            // Lista para un autor concreto (si pasas su ID como argumento)
            if (args.length > 0) {
                String researcher = args[0];
                System.out.println("\n== Artículos de " + researcher + " ==");
                try (PreparedStatement ps = c.prepareStatement("""
                        SELECT title, COALESCE(cited_by, 0) AS cited_by, link
                        FROM articles
                        WHERE researcher_id = ?
                        ORDER BY cited_by DESC, title
                    """)) {
                    ps.setString(1, researcher);
                    try (ResultSet rs = ps.executeQuery()) {
                        int i = 1;
                        while (rs.next()) {
                            System.out.printf("%02d) %s  [citas=%d]%n   %s%n",
                                    i++,
                                    rs.getString("title"),
                                    rs.getInt("cited_by"),
                                    rs.getString("link"));
                        }
                    }
                }
            }
        }
    }
}
