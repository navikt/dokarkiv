CREATE TABLE t_utsendings_info (
    journalpost_id             NUMBER NOT NULL
    CONSTRAINT t_utsendings_info_pk PRIMARY KEY
    CONSTRAINT t_utsendings_info_fk FOREIGN KEY REFERENCES t_journalpost(journalpost_id),
    adresselinje1              VARCHAR(200),
    adresselinje2              VARCHAR(200),
    adresselinje3              VARCHAR(200),
    postnummer                 VARCHAR(10),
    poststed                   VARCHAR(200),
    landkode                   VARCHAR(2),
    digitalpostkasseadresse    VARCHAR(100),
    digitalpostkasseleverandor VARCHAR(20),
    digital_kontaktinformasjon VARCHAR(200),
    varslingstekst             VARCHAR(4000)
);
