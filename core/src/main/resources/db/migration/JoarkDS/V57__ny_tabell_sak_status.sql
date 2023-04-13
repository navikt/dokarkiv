CREATE TABLE T_K_SAK_STATUS
(
    K_SAK_STATUS    VARCHAR2(40)  NOT NULL
        CONSTRAINT IKSAKSTATUSU
        PRIMARY KEY,
    DEKODE         VARCHAR2(200) NOT NULL,
    DATO_FOM       DATE          NOT NULL,
    DATO_TOM       DATE,
    ER_GYLDIG      CHAR          NOT NULL,
    DATO_OPPRETTET TIMESTAMP(6)  NOT NULL,
    OPPRETTET_AV   VARCHAR2(20)  NOT NULL,
    DATO_ENDRET    TIMESTAMP(6)  NOT NULL,
    ENDRET_AV      VARCHAR2(20)  NOT NULL
);

CREATE TABLE T_SAK_STATUS
(
    SAK_STATUS_ID           NUMBER NOT NULL
        CONSTRAINT PK_SAK_STATUS_ID
        PRIMARY KEY,
    K_SAK_STATUS            VARCHAR2(40) NOT NULL,
    BRUKER_ID               VARCHAR2(11) NOT NULL,
    BRUKER_ID_TYPE          VARCHAR2(40) NOT NULL,
    K_FAGOMRADE             VARCHAR2(3)  NOT NULL,
    FAGSAKNR                VARCHAR2(40),
    APPLIKASJON             VARCHAR2(40),
    DATO_OPPRETTET          TIMESTAMP NOT NULL,
    OPPRETTET_AV            VARCHAR2(40) NOT NULL,
    DATO_ENDRET             TIMESTAMP,
    ENDRET_AV               VARCHAR2(40),
    DATO_AVSLUTTET          TIMESTAMP,
    AVSLUTTET_AV            VARCHAR2(40),
    DATO_KASSERT            TIMESTAMP,
    DATO_AVLEVERT           TIMESTAMP,
    DATO_SAK_OPPRETTET      TIMESTAMP,
    ADMINISTRATIV_ENHET     VARCHAR2(40),
    SAK_ANSVARLIG           VARCHAR2(40),
    CONSTRAINT FK_K_FAGOMRADE FOREIGN KEY (K_FAGOMRADE) REFERENCES T_K_FAGOMRADE(K_FAGOMRADE),
    CONSTRAINT FK_K_SAK_STATUS FOREIGN KEY (K_SAK_STATUS) REFERENCES T_K_SAK_STATUS(K_SAK_STATUS)
);

CREATE SEQUENCE T_SAK_STATUS_SEQ
    CACHE 50;


insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                           dato_endret, endret_av)
VALUES ('AAPEN', 'Åpen', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('AVSLUTTET', 'Avsluttet', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('KAN_KASSERES', 'Kan kasseres', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('KASSERT', 'Kassert', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('KAN_AVLEVERES', 'Kan avleveres', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('AVLEVERT_GODKJENNING', 'Avlevert og venter på godkjenning', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('AVLEVERT', 'Avlevert', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('KAN_SLETTES', 'Kan slettes', date '1900-01-01', NULL, '1', timestamp '2023-03-28 12:00:00',
        'MMA-6626', null, null);

