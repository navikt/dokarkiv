create table T_K_BEVARINGSTID
(
    K_BEVARINGSTID varchar2(128 char) not null primary key,
    DEKODE         varchar2(512 char) not null,
    DATO_OPPRETTET timestamp    not null,
    OPPRETTET_AV   varchar2(512 char) not null,
    DATO_ENDRET    timestamp,
    ENDRET_AV      varchar2(512 char)
);

insert into T_K_BEVARINGSTID (K_BEVARINGSTID, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('10_AAR_ETTER_BRUKERS_DOED', '10 år etter brukers død', systimestamp, 'MMA-8269');
insert into T_K_BEVARINGSTID (K_BEVARINGSTID, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('25_AAR_ETTER_BRUKERS_DOED', '25 år etter brukers død', systimestamp, 'MMA-8269');
insert into T_K_BEVARINGSTID (K_BEVARINGSTID, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('100_AAR_ETTER_AVSLUTTET_SAK', '100 år etter avsluttet sak', systimestamp, 'MMA-8269');
insert into T_K_BEVARINGSTID (K_BEVARINGSTID, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('10_AAR_ETTER_AVSLUTTET_SAK', '10 år etter avsluttet sak', systimestamp, 'MMA-8269');

alter table T_K_FAGOMRADE add
(
    K_BEVARINGSTID varchar2(128 char),
    constraint FK_BEVARINGSTID foreign key (K_BEVARINGSTID) references T_K_BEVARINGSTID (K_BEVARINGSTID)
);

update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8269', K_BEVARINGSTID = '10_AAR_ETTER_BRUKERS_DOED' where K_FAGOMRADE in ('AAP', 'BIL', 'ENF');
update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8269', K_BEVARINGSTID = '25_AAR_ETTER_BRUKERS_DOED' where K_FAGOMRADE in ('TRY', 'UFM', 'UFO', 'YRK');
update T_K_FAGOMRADE set DATO_ENDRET = systimestamp, ENDRET_AV = 'MMA-8269', K_BEVARINGSTID = '100_AAR_ETTER_AVSLUTTET_SAK' where K_FAGOMRADE in ('FAR', 'BID');
