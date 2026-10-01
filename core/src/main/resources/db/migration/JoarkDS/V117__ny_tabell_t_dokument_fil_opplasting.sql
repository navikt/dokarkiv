create table T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND
(
    K_OPPLASTING_TILSTAND varchar2(128 char) not null primary key,
    DEKODE                varchar2(512 char) not null,
    DATO_OPPRETTET        timestamp          not null,
    OPPRETTET_AV          varchar2(512 char) not null,
    DATO_ENDRET           timestamp,
    ENDRET_AV             varchar2(512 char)
);

insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('LASTER_OPP', 'Dokumentfil lastes opp til ekstern lagring', current_timestamp, 'MMA-8883');
insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('LASTET_OPP', 'Dokumentfil er lastet opp til ekstern lagring', current_timestamp, 'MMA-8883');
insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('ARKIVERES', 'Dokumentfil er klar for arkivering i joark', current_timestamp, 'MMA-8883');
insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('ARKIVERT', 'Dokumentfil er arkivert i joark', current_timestamp, 'MMA-8883');
insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('FEILET', 'Behandling av dokumentfil feilet', current_timestamp, 'MMA-8883');

create table T_DOKUMENT_FIL_OPPLASTING
(
    DOKUMENT_FIL_ID               varchar2(128 char) not null primary key,
    EKSTERN_DOKUMENT_REFERANSE_ID varchar2(512 char) not null,
    MEDIA_TYPE                    varchar2(128 char) not null,
    SHA256_SJEKKSUM               raw(32)            not null,
    TILSTAND                      varchar2(128 char) not null
        constraint fk_dok_fil_opplasting_tilstand references T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND),
    OPPRETTET_KILDE_NAVN          varchar2(512 char) not null,
    DATO_OPPRETTET                timestamp          not null,
    DATO_KLIENT_LEASE             timestamp          not null,
    VERSJON                       number             not null,
    ENDRET_KILDE_NAVN             varchar2(512 char),
    DATO_ENDRET                   timestamp,
    CRC32C_SJEKKSUM               raw(4),
    ANTALL_BYTES                  number,
    DATO_LASTET_OPP               timestamp,
    DATO_ARKIVERT                 timestamp,
    CONSTRAINT uidx_dok_fil_opplasting_ekst_ref
        UNIQUE (ekstern_dokument_referanse_id)
);

create index idx_dok_fil_opplasting_tilstand on T_DOKUMENT_FIL_OPPLASTING (TILSTAND);