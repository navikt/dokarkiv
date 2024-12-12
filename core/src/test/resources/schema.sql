-- Opprett tabell siden dette ikke er en egen JPA entitet
create table t_jp_tillegg
(
    journalpost_id number(11, 0)      not null,
    nokkel         varchar2(20 char)  not null,
    verdi          varchar2(100 char) not null
);

alter table t_jp_tillegg
    add constraint xpkt_jp_tillegg primary key ("JOURNALPOST_ID", "NOKKEL");

alter table t_jp_tillegg
    add constraint xpkt_jp_tillegg_uniqe unique ("NOKKEL", "VERDI");
