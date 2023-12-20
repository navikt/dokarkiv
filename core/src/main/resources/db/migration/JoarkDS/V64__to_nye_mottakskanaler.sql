INSERT INTO T_K_MOTTAKS_KANAL (k_mottaks_kanal,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT
    'E_POST','E-post',date '1900-01-01',NULL, '1',timestamp '2023-12-20 11:00:00','MMA-7224',timestamp '2023-12-20 11:00:00','MMA-7224'
FROM dual
WHERE NOT exists(
    SELECT 1
    from T_K_MOTTAKS_KANAL
    where k_mottaks_kanal = 'E_POST');

INSERT INTO T_K_MOTTAKS_KANAL (k_mottaks_kanal,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT
    'ALTINN_INNBOKS','Altinn Innboks',date '1900-01-01',NULL, '1',timestamp '2023-12-20 11:00:00','MMA-7224',timestamp '2023-12-20 11:00:00','MMA-7224'
FROM dual
WHERE NOT exists(
    SELECT 1
    from T_K_MOTTAKS_KANAL
    where k_mottaks_kanal = 'ALTINN_INNBOKS');