insert into T_K_KATEGORI_T (k_kategori_t,dekode,dato_fom,dato_tom,er_gyldig,dato_opprettet,opprettet_av,dato_endret,endret_av)
SELECT 'FORVALTNINGSNOTAT','Forvaltningsnotat',date '1900-01-01',NULL,'1',timestamp '2015-06-16 10:00:00','Gerhard Wiese',timestamp '2015-06-16 10:00:00','Gerhard Wiese'
  FROM dual
  where not exists(
      SELECT * FROM T_K_KATEGORI_T
      WHERE k_kategori_t= 'FORVALTNINGSNOTAT');

insert into T_DOKUMENT_MAL_INFO (brev_kode,brev_gruppe,tittel,redigerbart,k_kategori_t,k_journalpost_t,organ_internt,sensitivt,dato_opprettet,opprettet_av,dato_endret,endret_av)
  SELECT 'GEN_NOT_004','InterntNotat','Referat fra samtale med bruker','T','FORVALTNINGSNOTAT','N',NULL,'F',timestamp '2015-07-01 09:35:00','Hanne Oustad',timestamp '2015-07-13 10:24:00','Hanne Oustad'
  FROM dual
  where not exists(
      SELECT * FROM T_DOKUMENT_MAL_INFO
      WHERE brev_kode= 'GEN_NOT_004');