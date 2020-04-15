INSERT INTO T_K_MOTTAKS_KANAL (k_mottaks_kanal,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT
       'SKAN_IM','Skanning Iron Mountain',date '1900-01-01',NULL,'1',timestamp '2020-04-15 14:00:00','Erlend Axelsson',timestamp '2020-04-15 14:00:00','Erlend Axelsson'
FROM dual
WHERE NOT exists(
            SELECT 1
            from T_K_MOTTAKS_KANAL
            where k_mottaks_kanal = 'SKAN_IM');
