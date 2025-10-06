package org.example.scholar;

import java.sql.*;

public class Migrate {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:sqlite:scholar.db";

        String[] statements = new String[] {
                "PRAGMA foreign_keys=off;",
                "BEGIN TRANSACTION;",
                // (1) Crear tabla temporal con el MISMO esquema y UNIQUE(researcher_id, title)
                """
            CREATE TABLE articles_new (
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
            """,
                // (2) Insertar datos normalizados y deduplicados
                """
            INSERT INTO articles_new (
              researcher_id, researcher_name, title, authors, publication_date,
              abstract, link, keywords, cited_by, created_at
            )
            SELECT
              CASE
                WHEN instr(researcher_id, '&') = 0 THEN researcher_id
                ELSE substr(researcher_id, 1, instr(researcher_id, '&') - 1)
              END AS researcher_id_clean,
              researcher_name,
              title,
              authors,
              publication_date,
              abstract,
              link,
              keywords,
              MAX(COALESCE(cited_by, 0)) AS cited_by,  -- si había duplicados, conserva el mayor
              MIN(created_at)                          -- conserva la fecha más antigua
            FROM articles
            GROUP BY researcher_id_clean, title;
            """,
                // (3) Reemplazar tabla
                "DROP TABLE articles;",
                "ALTER TABLE articles_new RENAME TO articles;",
                "COMMIT;",
                "PRAGMA foreign_keys=on;"
        };

        try (Connection c = DriverManager.getConnection(url)) {
            for (String sql : statements) {
                try (Statement st = c.createStatement()) {
                    st.execute(sql);
                }
            }
        }
        System.out.println("✔ Migración completada: IDs normalizados y duplicados consolidados.");
    }
}
