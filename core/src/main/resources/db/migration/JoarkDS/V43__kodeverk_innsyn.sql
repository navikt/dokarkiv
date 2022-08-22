create table t_k_innsyn
(
    k_innsyn       VARCHAR2(50) not null
        constraint xpkk_innsyn
        primary key,
    beskrivelse    VARCHAR2(200) not null,
    dato_opprettet TIMESTAMP not null,
    opprettet_av   VARCHAR2(20) not null,
    dato_endret    TIMESTAMP,
    endret_av      VARCHAR2(20)
);

insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('BRUK_STANDARDREGLER', 'Standardreglene avgjør om dokumentet vises',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('VISES_MASKINELT_GODKJENT', 'Vises på nav.no etter maskinell godkjenning',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('VISES_MANUELT_GODKJENT', 'Vises på nav.no etter gjennomgang av en NAV-ansatt',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('VISES_FORVALTNINGSNOTAT', 'Vises på nav.no fordi dokumentet er markert som forvaltningsnotat',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('SKJULES_FEILSENDT', 'Skjules på nav.no fordi dokumentet ble sendt til feil bruker',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('SKJULES_BRUKERS_ØNSKE', 'Skjules på nav.no etter brukers eget ønske',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('SKJULES_ORGAN_INTERNT', 'Skjules på nav.no fordi dokumentet er markert som organinternt',
        current_timestamp, 'MMA-6327');
insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('SKJULES_INNSKRENKET_PARTSINNSYN', 'Skjules på nav.no fordi dokumentet er markert som innskrenket partsinnsyn',
        current_timestamp, 'MMA-6327');
