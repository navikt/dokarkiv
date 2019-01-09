create table t_hendelselogg
(
  hendelselogg_id  number(11)     not null,
  applikasjon      varchar2(20)   not null,
  aksjon           varchar2(50)   not null,
  journalpost_id   number(11)     null,
  dokument_info_id number(11)     null,
  bruker           number(11)     null,
  hjemmel          varchar2(20)   null,
  sak              varchar2(20)   null,
  melding          varchar2(4000) null,

  dato_opprettet   timestamp(6)   not null,
  opprettet_av     varchar2(20)   not null,
  dato_endret      timestamp(6)   null,
  endret_av        varchar2(20)   null,
  versjon          number(11)     not null,
  CONSTRAINT hendelselogg_pk PRIMARY KEY (hendelselogg_id)
);


CREATE INDEX xihendelsetype
  ON t_hendelselogg (aksjon);

CREATE SEQUENCE t_hendelselogg_seq
  CACHE 30;