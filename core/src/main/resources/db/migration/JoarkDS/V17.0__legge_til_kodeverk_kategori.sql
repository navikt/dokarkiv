INSERT INTO T_K_KATEGORI_T (k_kategori_t, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av, dato_endret, endret_av)
  SELECT 'SOK', 'Søknad', DATE '1900-01-01', NULL, '1', TIMESTAMP '2017-03-05 10:00:00', 'Paul Magne Lunde', TIMESTAMP '2017-03-05 10:00:00', 'Paul Magne Lunde'
  FROM dual
  WHERE NOT exists(
      SELECT * FROM T_K_KATEGORI_T
      WHERE k_kategori_t = 'SOK');

INSERT INTO T_K_KATEGORI_T (k_kategori_t, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av, dato_endret, endret_av)
  SELECT 'KA', 'Klage eller anke', DATE '1900-01-01', NULL, '1', TIMESTAMP '2017-03-05 10:00:00', 'Paul Magne Lunde', TIMESTAMP '2017-03-05 10:00:00', 'Paul Magne Lunde'
  FROM dual
  WHERE NOT exists(
      SELECT * FROM T_K_KATEGORI_T
      WHERE k_kategori_t = 'KA');