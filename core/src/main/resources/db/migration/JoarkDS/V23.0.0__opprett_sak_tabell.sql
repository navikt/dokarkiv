CREATE TABLE SAK
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

CREATE SEQUENCE SAK_SEQ;

CREATE INDEX sak_orgnr
    ON SAK (orgnr);
CREATE INDEX sak_aktoerid
    ON SAK (aktoerid);
create index sak_fagsaknr
    on SAK (fagsaknr);
