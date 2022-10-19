CREATE TABLE t_utsendings_info (
    utsendings_info_id         NUMBER NOT NULL,
    journalpost_id             NUMBER NOT NULL,
    adresselinje1              VARCHAR(200),
    adresselinje2              VARCHAR(200),
    adresselinje3              VARCHAR(200),
    postnummer                 VARCHAR(20),
    poststed                   VARCHAR(200),
    landkode                   VARCHAR(20),
    digitalpostkasseadresse    VARCHAR(100),
    digitalpostkasseleverandor VARCHAR(20),
    digital_kontaktinformasjon VARCHAR(200),
    varslingstekst             VARCHAR(400),

    CONSTRAINT pk_sak PRIMARY KEY (utsendings_info_id),
    CONSTRAINT FOREIGN KEY journalpost_id REFERENCES t_journalpost
);

CREATE SEQUENCE seq_utsendings_info;
