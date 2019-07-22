INSERT INTO T_K_MOTTAKS_KANAL (k_mottaks_kanal,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT
       'NAV_NO_UINNLOGGET','Ditt NAV uten ID-porten-pålogging',date '1900-01-01',NULL,'1',timestamp '2019-07-10 12:00:00','Bjørnar Hunshamar',timestamp '2019-07-10 12:00:00','Bjørnar Hunshamar'
FROM dual
WHERE NOT exists(
            SELECT 1
            from T_K_MOTTAKS_KANAL
            where k_mottaks_kanal = 'NAV_NO_UINNLOGGET');
