INSERT INTO T_K_FIL_T (k_fil_t, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av, dato_endret, endret_av)
  SELECT
    'PNG',
    'PNG',
    DATE '1900-01-01',
    NULL,
    '1',
    TIMESTAMP '2018-06-18 10:00:00',
    'Ketill Fenne',
    TIMESTAMP '2018-06-18 10:00:00',
    'Ketill Fenne'
  FROM dual
  WHERE NOT exists(
      SELECT 1
      FROM T_K_FIL_T
      WHERE k_fil_t = 'PNG');