INSERT INTO T_K_FAGSYSTEM (k_fagsystem, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av, dato_endret, endret_av)
  SELECT
    'OB36',
    'UR',
    TIMESTAMP '1899-12-31 00:00:00',
    NULL,
    1,
    TIMESTAMP '2018-06-19 10:00:00',
    'J. Bjørnstad',
    TIMESTAMP '2018-06-19 10:00:00',
    'J. Bjørnstad'
  FROM DUAL
  WHERE NOT EXISTS(SELECT 1
                   FROM T_K_FAGSYSTEM
                   WHERE k_fagsystem = 'OB36');
