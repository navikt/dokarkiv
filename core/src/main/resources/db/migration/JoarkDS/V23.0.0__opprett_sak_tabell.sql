CREATE TABLE sak
(
    id                  NUMBER(10)   NOT NULL,
    aktoerid            VARCHAR2(40),
    orgnr               VARCHAR2(9),
    tema                VARCHAR2(40) NOT NULL,
    applikasjon         VARCHAR2(40),
    fagsaknr            VARCHAR2(40),
    opprettet_av        VARCHAR2(40) NOT NULL,
    opprettet_tidspunkt TIMESTAMP    NOT NULL,

    CONSTRAINT pk_sak PRIMARY KEY (id)
);

CREATE SEQUENCE seq_sak;

CREATE INDEX idx_sak_orgnr
    ON sak (orgnr);
CREATE INDEX idx_sak_aktoerid
    ON sak (aktoerid);
CREATE INDEX idx_sak_fagsaknr
    ON sak (fagsaknr);
