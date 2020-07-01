INSERT INTO T_K_MOTTAKS_KANAL (k_mottaks_kanal,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT
       'INNSENDT_NAV_ANSATT','Innsendt av NAV-ansatt',date '1900-01-01',NULL,'1',timestamp '2020-07-01 12:00:00','Erlend Axelsson',timestamp '2020-07-01 12:00:00','Erlend Axelsson'
FROM dual
WHERE NOT exists(
            SELECT 1
            from T_K_MOTTAKS_KANAL
            where k_mottaks_kanal = 'INNSENDT_NAV_ANSATT');
