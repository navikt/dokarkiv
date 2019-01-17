create table T_K_AKSJON_TYPE
(
  AKSJON_TYPE    VARCHAR2(50)  NOT NULL
    CONSTRAINT XPKT_T_K_AKSJON_TYPE
      PRIMARY KEY,
  BESKRIVELSE    VARCHAR2(500) NOT NULL,
  DATO_OPPRETTET TIMESTAMP(6)  NOT NULL,
  OPPRETTET_AV   VARCHAR2(20)  NOT NULL
);

create table t_aksjonslogg
(
  aksjonslogg_id   number(11)     not null,
  applikasjon      varchar2(50)   not null,
  aksjon           varchar2(50)   not null references T_K_AKSJON_TYPE(AKSJON_TYPE),
  journalpost_id   number(11)     not null,
  dokument_info_id number(11)     null,
  bruker           varchar2(50)   null,
  hjemmel          varchar2(50)   null,
  arkiv_element    varchar2(50)   null,
  fra_verdi        varchar2(50)   null,
  til_verdi        varchar2(50)   null,
  utfoert_av       varchar2(50)   not null,
  melding          varchar2(4000) null,
  dato_opprettet   timestamp(6)   not null,
  opprettet_av     varchar2(50)   not null,
  CONSTRAINT aksjonslogg_pk PRIMARY KEY (aksjonslogg_id)
);

INSERT INTO T_K_AKSJON_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('ENDRE_BEGRENSNING','Endre begrensning på et arkivelement',sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJON_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('KASSASJON','Kassering av et dokument',sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJON_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('SLETT','Sletting av et arkivelement',sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJON_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('ARKIVERING','Arkivering av et nytt dokument',sysdate, 'Ugur Alpay Cenar');

CREATE INDEX xiaksjon
  ON t_aksjonslogg (dato_opprettet);

CREATE INDEX xibruker
  ON t_aksjonslogg (bruker);

CREATE SEQUENCE t_aksjonslogg_seq
  CACHE 30;
