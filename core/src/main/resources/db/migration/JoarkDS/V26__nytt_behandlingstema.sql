INSERT INTO T_K_BEHANDLINGSTEMA (k_behandlingstema, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                 dato_endret, endret_av)
SELECT 'ab0438', 'Lønnskompensasjon', DATE '1900-01-01', NULL, '1', sysdate, 'Joakim Bjørnstad', sysdate, 'Joakim Bjørnstad'
FROM dual
WHERE NOT exists(
  SELECT 1
  from T_K_BEHANDLINGSTEMA
  where K_BEHANDLINGSTEMA = 'ab0438');
