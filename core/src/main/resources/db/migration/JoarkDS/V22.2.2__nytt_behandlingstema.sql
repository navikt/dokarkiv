INSERT INTO T_K_BEHANDLINGSTEMA (k_behandlingstema, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                 dato_endret, endret_av)
SELECT 'ab0423', 'Ekspertbistand', DATE '1900-01-01', NULL, '1', sysdate, 'M. Burheim Tingstad', sysdate, 'M. Burheim Tingstad'
FROM dual
WHERE NOT exists(
  SELECT 1
  from T_K_BEHANDLINGSTEMA
  where K_BEHANDLINGSTEMA = 'ab0423');
