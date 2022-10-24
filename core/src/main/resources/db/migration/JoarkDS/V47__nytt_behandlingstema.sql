INSERT INTO T_K_BEHANDLINGSTEMA (k_behandlingstema, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                 dato_endret, endret_av)
SELECT 'ab0443', 'Regning lese- og sekretærhjelp', DATE '2020-11-12', NULL, '1', sysdate, 'Joakim Borgersen', sysdate, 'Joakim Borgersen'
FROM dual
WHERE NOT exists(
  SELECT 1
  from T_K_BEHANDLINGSTEMA
  where K_BEHANDLINGSTEMA = 'ab0443');