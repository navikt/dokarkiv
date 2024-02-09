CREATE TABLE T_K_BRUKER_ID_T
(
    K_BRUKER_ID_T VARCHAR2(40)  NOT NULL
        PRIMARY KEY,
    DEKODE        VARCHAR2(200) NOT NULL,
    OPPRETTET_AV  VARCHAR2(20)  NOT NULL,
    DATO_ENDRET   TIMESTAMP(6)  NULL,
    ENDRET_AV     VARCHAR2(20)  NULL
);

INSERT INTO T_K_BRUKER_ID_T (K_BRUKER_ID_T, DEKODE, OPPRETTET_AV)
VALUES ('FNR',
        'Identitetsnummer i Folkeregisteret. Kan være fødselsnummer eller D-nummer. Identifiserer personer som har tilknytning til Norge',
        'MMA-7293');

INSERT INTO T_K_BRUKER_ID_T (K_BRUKER_ID_T, DEKODE, OPPRETTET_AV)
VALUES ('ORGNR',
        'Organisasjonsnummer i Enhetsregisteret og Foretaksregisteret. Identifiserer juridiske personer som bedrifter i Norge',
        'MMA-7293');