CREATE TABLE T_K_AVSEND_MOTTAK_ID_T
  (
    K_AVSEND_MOTTAK_ID_T     VARCHAR2(20) NOT NULL
      CONSTRAINT XPKK_AVSEND_MOTTAK_ID_T
      PRIMARY KEY,
    DEKODE         VARCHAR2(200) NOT NULL,
    DATO_OPPRETTET TIMESTAMP(6)  NOT NULL,
    OPPRETTET_AV   VARCHAR2(20)  NOT NULL,
    DATO_ENDRET    TIMESTAMP(6)  NULL,
    ENDRET_AV      VARCHAR2(20)  NULL
);

INSERT INTO T_K_AVSEND_MOTTAK_ID_T (K_AVSEND_MOTTAK_ID_T,dekode,dato_opprettet,opprettet_av,dato_endret,endret_av)
  VALUES ('FNR','Fodselsnummer',timestamp '2019-05-10 09:14:00','Olav R Thorsen',null ,NULL );

INSERT INTO T_K_AVSEND_MOTTAK_ID_T (K_AVSEND_MOTTAK_ID_T,dekode,dato_opprettet,opprettet_av,dato_endret,endret_av)
  VALUES ('ORGNR','Organisasjonsnummer',timestamp '2019-05-10 09:14:00','Olav R Thorsen',NULL,NULL);

INSERT INTO T_K_AVSEND_MOTTAK_ID_T (K_AVSEND_MOTTAK_ID_T,dekode,dato_opprettet,opprettet_av,dato_endret,endret_av)
  VALUES ('HPRNR','Helsepersonellnummer',timestamp '2019-05-10 09:14:00','Olav R Thorsen',NULL,NULL);

INSERT INTO T_K_AVSEND_MOTTAK_ID_T (K_AVSEND_MOTTAK_ID_T,dekode,dato_opprettet,opprettet_av,dato_endret,endret_av)
  VALUES ('UTL_ORG','Organisasjonsnummer utland',timestamp '2019-05-10 09:14:00','Olav R Thorsen',NULL,NULL);

ALTER TABLE T_JOURNALPOST ADD K_AVSEND_MOTTAK_ID_T VARCHAR2(20 CHAR) NULLABLE;

ALTER TABLE T_JOURNALPOST
  ADD CONSTRAINT T_JOURNALPOST_R_57
FOREIGN KEY (K_AVSEND_MOTTAK_ID_T) REFERENCES T_K_AVSEND_MOTTAK_ID_T NOVALIDATE;