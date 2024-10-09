CREATE TABLE K_KASSASJON_STATUS
(
    K_KASSASJON_STATUS    VARCHAR2(128)  NOT NULL
        CONSTRAINT PK_K_KASSASJON_STATUS
        PRIMARY KEY,
    DEKODE         VARCHAR2(512) NOT NULL,
    DATO_FOM       DATE          NOT NULL,
    DATO_TOM       DATE,
    ER_GYLDIG      CHAR          NOT NULL,
    DATO_OPPRETTET TIMESTAMP(6)  NOT NULL,
    OPPRETTET_AV   VARCHAR2(128)  NOT NULL,
    DATO_ENDRET    TIMESTAMP(6)  NULL,
    ENDRET_AV      VARCHAR2(128)  NULL
);

insert into K_KASSASJON_STATUS (k_kassasjon_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                   dato_endret, endret_av)
VALUES ('KASSASJONSTID_NAADD', 'Saken har nådd kassasjonstid', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7628', null, null);

insert into K_KASSASJON_STATUS (k_kassasjon_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                dato_endret, endret_av)
VALUES ('KLAR_FOR_KASSASJON', 'Saken kan kasseres', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7628', null, null);

insert into K_KASSASJON_STATUS (k_kassasjon_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                dato_endret, endret_av)
VALUES ('DOKUMENTER_KASSERT', 'Dokumenter på saken er kassert', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7628', null, null);


