CREATE TABLE T_K_AVLEVERING_STATUS
(
    K_AVLEVERING_STATUS    VARCHAR2(40)  NOT NULL
        CONSTRAINT PK_K_AVLEVERING_STATUS
        PRIMARY KEY,
    DEKODE         VARCHAR2(200) NOT NULL,
    DATO_FOM       DATE          NOT NULL,
    DATO_TOM       DATE,
    ER_GYLDIG      CHAR          NOT NULL,
    DATO_OPPRETTET TIMESTAMP(6)  NOT NULL,
    OPPRETTET_AV   VARCHAR2(20)  NOT NULL,
    DATO_ENDRET    TIMESTAMP(6)  NULL,
    ENDRET_AV      VARCHAR2(20)  NULL
);

insert into T_K_AVLEVERING_STATUS (k_avlevering_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                           dato_endret, endret_av)
VALUES ('KLAR_FOR_AVLEVERING', 'Klar for avlevering', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7627', null, null);

insert into T_K_AVLEVERING_STATUS (k_avlevering_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                   dato_endret, endret_av)
VALUES ('AVLEVERING_OVERFOERT', 'Overført til arkivverket', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7627', null, null);
insert into T_K_AVLEVERING_STATUS (k_avlevering_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                   dato_endret, endret_av)
VALUES ('AVLEVERT', 'Avlevert og godkjent av arkivverket', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7627', null, null);
insert into T_K_AVLEVERING_STATUS (k_avlevering_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                                   dato_endret, endret_av)
VALUES ('AVBRUTT', 'Avbrutt og skal ikke avleveres', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7627', null, null);

