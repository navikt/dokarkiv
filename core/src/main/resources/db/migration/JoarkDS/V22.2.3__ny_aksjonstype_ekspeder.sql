INSERT INTO T_K_AKSJON_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
SELECT
  'EKSPEDER',
  'Journalpost fikk status "ekspedert"',
  sysdate,
  'M. Burheim Tingstad'
  FROM dual
  WHERE NOT exists(
    SELECT 1
    FROM T_K_AKSJON_TYPE
    WHERE AKSJON_TYPE = 'EKSPEDER'
  );
