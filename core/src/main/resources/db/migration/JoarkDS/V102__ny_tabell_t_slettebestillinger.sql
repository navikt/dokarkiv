create table T_K_SLETTEBESTILLING_TYPE
(
    K_SLETTEBESTILLING_TYPE varchar2(128 char) not null primary key,
    DEKODE                  varchar2(128 char) not null,
    DATO_OPPRETTET          timestamp not null,
    OPPRETTET_AV            varchar2(512 char) not null,
    DATO_ENDRET             timestamp,
    ENDRET_AV               varchar2(512 char)
);

insert into T_K_SLETTEBESTILLING_TYPE (K_SLETTEBESTILLING_TYPE, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('DOKUMENT', 'Ett dokument skal slettes', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_TYPE (K_SLETTEBESTILLING_TYPE, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('DOKUMENTER_PA_SAK', 'Alle dokumenter knyttet til arkivsak skal slettes', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_TYPE (K_SLETTEBESTILLING_TYPE, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('SAK', 'En sak skal slettes', systimestamp, 'MMA-8345');


create table T_K_SLETTEBESTILLING_STATUS
(
    K_SLETTEBESTILLING_STATUS varchar2(128 char) not null primary key,
    DEKODE                    varchar2(128 char) not null,
    DATO_OPPRETTET            timestamp not null,
    OPPRETTET_AV              varchar2(512 char) not null,
    DATO_ENDRET               timestamp,
    ENDRET_AV                 varchar2(512 char)
);

insert into T_K_SLETTEBESTILLING_STATUS (K_SLETTEBESTILLING_STATUS, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('OPPRETTET', 'Slettebestillingen er opprettet', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_STATUS (K_SLETTEBESTILLING_STATUS, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('FERDIGSTILT', 'Slettebestillingen er ferdigstilt', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_STATUS (K_SLETTEBESTILLING_STATUS, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('AVBRUTT', 'Slettebestillingen er avbrutt', systimestamp, 'MMA-8345');


create table T_K_SLETTEBESTILLING_HJEMMEL
(
    K_SLETTEBESTILLING_HJEMMEL varchar2(128 char) not null primary key,
    DEKODE                     varchar2(128 char) not null,
    DATO_OPPRETTET             timestamp not null,
    OPPRETTET_AV               varchar2(512 char) not null,
    DATO_ENDRET                timestamp,
    ENDRET_AV                  varchar2(512 char)
);

insert into T_K_SLETTEBESTILLING_HJEMMEL (K_SLETTEBESTILLING_HJEMMEL, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('POL', 'Slettes etter personopplysningsloven', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_HJEMMEL (K_SLETTEBESTILLING_HJEMMEL, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('ARK', 'Slettes etter arkivloven', systimestamp, 'MMA-8345');


create table T_K_SLETTEBESTILLING_ARSAK
(
    K_SLETTEBESTILLING_ARSAK varchar2(128 char) not null primary key,
    DEKODE                   varchar2(128 char) not null,
    DATO_OPPRETTET           timestamp not null,
    OPPRETTET_AV             varchar2(512 char) not null,
    DATO_ENDRET              timestamp,
    ENDRET_AV                varchar2(512 char)
);

insert into T_K_SLETTEBESTILLING_ARSAK (K_SLETTEBESTILLING_ARSAK, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('BEVARINGSTID', 'Elementet som slettes har passert bevaringstiden', systimestamp, 'MMA-8345');
insert into T_K_SLETTEBESTILLING_ARSAK (K_SLETTEBESTILLING_ARSAK, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('ENKELTSLETTING', 'Elementet slettes etter manuell vurdering', systimestamp, 'MMA-8345');


create table T_SLETTEBESTILLING
(
    SLETTEBESTILLING_ID        number(19)    not null primary key,
    K_SLETTEBESTILLING_TYPE    varchar2(128 char) not null,
    K_SLETTEBESTILLING_STATUS  varchar2(128 char) not null,
    K_SLETTEBESTILLING_HJEMMEL varchar2(128 char) not null,
    K_SLETTEBESTILLING_ARSAK   varchar2(128 char) not null,
    BEGRUNNELSE                varchar2(512 char),
    DOKUMENT_INFO_ID           number(19),
    SAK_ID                     number(19),
    DATO_UTFORES               date      not null,
    DATO_UTFORT                timestamp,
    DATO_OPPRETTET             timestamp not null,
    OPPRETTET_AV               varchar2(512 char) not null,
    OPPRETTET_AV_NAVN          varchar2(512 char) not null,
    OPPRETTET_AV_KILDE_NAVN    varchar2(512 char) not null,
    DATO_ENDRET                timestamp,
    ENDRET_AV                  varchar2(512 char),
    ENDRET_AV_NAVN             varchar2(512 char),
    ENDRET_AV_KILDE_NAVN       varchar2(512 char),
    constraint FK_T_SLETTEBESTILLING_T_SLETTEBESTILLING_TYPE foreign key (K_SLETTEBESTILLING_TYPE) references T_K_SLETTEBESTILLING_TYPE (K_SLETTEBESTILLING_TYPE),
    constraint FK_T_SLETTEBESTILLING_T_SLETTEBESTILLING_STATUS foreign key (K_SLETTEBESTILLING_STATUS) references T_K_SLETTEBESTILLING_STATUS (K_SLETTEBESTILLING_STATUS),
    constraint FK_T_SLETTEBESTILLING_T_SLETTEBESTILLING_HJEMMEL foreign key (K_SLETTEBESTILLING_HJEMMEL) references T_K_SLETTEBESTILLING_HJEMMEL (K_SLETTEBESTILLING_HJEMMEL),
    constraint FK_T_SLETTEBESTILLING_T_SLETTEBESTILLING_ARSAK foreign key (K_SLETTEBESTILLING_ARSAK) references T_K_SLETTEBESTILLING_ARSAK (K_SLETTEBESTILLING_ARSAK),
    constraint CHECK_DOKUMENT_OR_SAK_NOT_NULL CHECK (
        (K_SLETTEBESTILLING_TYPE = 'DOKUMENT' and DOKUMENT_INFO_ID is not null) or
        ((K_SLETTEBESTILLING_TYPE = 'SAK' or K_SLETTEBESTILLING_TYPE = 'DOKUMENTER_PA_SAK') and SAK_ID is not null)
        )
);
