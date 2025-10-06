.headers on
.mode column

-- Tablas existen
SELECT CASE WHEN
  EXISTS(SELECT 1 FROM sqlite_master WHERE type='table' AND name='researcher') AND
  EXISTS(SELECT 1 FROM sqlite_master WHERE type='table' AND name='article')
  THEN 'OK tablas' ELSE 'FAIL tablas' END AS check;

-- Columnas mínimas en article
SELECT CASE WHEN
 (SELECT COUNT(*) FROM pragma_table_info('article')
  WHERE name IN ('id','title','authors','publication_date','abstract','link','keywords','cited_by','researcher_id'))=9
 THEN 'OK columnas article' ELSE 'FAIL columnas article' END AS check;

-- Hay exactamente 2 investigadores
SELECT CASE WHEN COUNT(*)=2 THEN 'OK investigadores=2' ELSE 'FAIL investigadores≠2' END AS check
FROM researcher;

-- Cada investigador tiene exactamente 3 artículos
WITH c AS (
  SELECT r.id, COUNT(a.id) n FROM researcher r LEFT JOIN article a ON a.researcher_id=r.id GROUP BY r.id
)
SELECT CASE WHEN MIN(n)=3 AND MAX(n)=3 THEN 'OK 3 artículos c/u' ELSE 'FAIL artículos por investigador' END AS check
FROM c;

-- Campos obligatorios y reglas
SELECT CASE WHEN EXISTS(SELECT 1 FROM article WHERE title IS NULL OR authors IS NULL OR link IS NULL OR researcher_id IS NULL)
  THEN 'FAIL NOT NULL' ELSE 'OK NOT NULL' END AS check;

SELECT CASE WHEN EXISTS(SELECT 1 FROM article GROUP BY link HAVING COUNT(*)>1)
  THEN 'FAIL links duplicados' ELSE 'OK links únicos' END AS check;

SELECT CASE WHEN EXISTS(SELECT 1 FROM article WHERE cited_by<0 OR cited_by!=CAST(cited_by AS INTEGER))
  THEN 'FAIL cited_by' ELSE 'OK cited_by' END AS check;

-- Formato de fecha flexible ISO
SELECT CASE WHEN EXISTS(
  SELECT 1 FROM article
  WHERE publication_date IS NOT NULL AND publication_date!=''
    AND publication_date NOT GLOB '[0-9][0-9][0-9][0-9]*'
) THEN 'FAIL fecha' ELSE 'OK fecha' END AS check;
