insert into T_K_FAGOMRADE (k_fagomrade,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT 'TSR','Tilleggsstønad arbeidsøkere',date '1900-01-01',NULL,'1',timestamp '2015-06-29 18:00:00','Roar Bjurstrøm',timestamp '2015-06-29 18:00:00','Roar Bjurstrøm'
from dual
where not exists(
    SELECT * FROM T_K_FAGOMRADE
    WHERE k_fagomrade= 'TSR');

insert into T_K_FAGOMRADE (k_fagomrade,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
  SELECT 'TSO','Tilleggsstønad',date '1900-01-01',NULL,'1',timestamp '2015-06-29 18:00:00','Roar Bjurstrøm',timestamp '2015-06-29 18:00:00','Roar Bjurstrøm'
  from dual
  where not exists(
      SELECT * FROM T_K_FAGOMRADE
      WHERE k_fagomrade= 'TSO');