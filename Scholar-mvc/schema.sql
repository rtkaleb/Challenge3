CREATE TABLE IF NOT EXISTS articles (
  id               INTEGER PRIMARY KEY AUTOINCREMENT,
  researcher_id    TEXT    NOT NULL,   -- ID del autor en Google Scholar (parámetro 'user' de la URL)
  researcher_name  TEXT,
  title            TEXT    NOT NULL,
  authors          TEXT,               -- separados por coma
  publication_date TEXT,               -- usa 'YYYY-01-01' si sólo tienes año
  abstract         TEXT,
  link             TEXT,
  keywords         TEXT,               -- separados por coma
  cited_by         INTEGER,
  created_at       TEXT DEFAULT (datetime('now')),
  UNIQUE(researcher_id, title)
);
