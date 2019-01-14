create table t_aksjonslogg
(
  aksjonslogg_id   number(11)     not null,
  applikasjon      varchar2(20)   not null,
  aksjon           varchar2(50)   not null,
  journalpost_id   number(11)     not null,
  dokument_info_id number(11)     null,
  bruker           varchar2(20)   null,
  hjemmel          varchar2(20)   null,
  utfoert_av       varchar2(20)   null,
  melding          varchar2(4000) null,

  dato_opprettet   timestamp(6)   not null,
  opprettet_av     varchar2(20)   not null,
  dato_endret      timestamp(6)   null,
  endret_av        varchar2(20)   null,
  versjon          number(11)     not null,
  CONSTRAINT aksjonslogg_pk PRIMARY KEY (aksjonslogg_id)
);


CREATE INDEX xihendelsetype
  ON t_aksjonslogg (aksjon);

CREATE INDEX xibruker
  ON t_aksjonslogg (bruker);

CREATE INDEX xijournalpostid
  ON t_aksjonslogg (journalpost_id);

CREATE INDEX xidokumentinfoid
  ON t_aksjonslogg (dokument_info_id);

CREATE SEQUENCE t_aksjonslogg_seq
  CACHE 30;