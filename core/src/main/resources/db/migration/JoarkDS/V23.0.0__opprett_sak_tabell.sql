CREATE TABLE t_sak
(
    id                  NUMBER(10)   NOT NULL,
    aktoerid            VARCHAR2(40),
    orgnr               VARCHAR2(9),
    tema                VARCHAR2(40) NOT NULL,
    applikasjon         VARCHAR2(40) NOT NULL,
    fagsaknr            VARCHAR2(40),
    opprettet_av        VARCHAR2(40) NOT NULL,
    opprettet_tidspunkt TIMESTAMP    NOT NULL,

    CONSTRAINT pk_sak PRIMARY KEY (id)
);

CREATE SEQUENCE T_SAK_SEQ;

CREATE INDEX sak_orgnr
    ON T_SAK (orgnr);
CREATE INDEX sak_aktoerid
    ON T_SAK (aktoerid);
