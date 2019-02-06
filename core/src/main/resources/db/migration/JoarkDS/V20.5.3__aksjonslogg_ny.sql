alter table t_aksjonslogg
  drop column fra_verdi;
alter table t_aksjonslogg
  drop column til_verdi;
alter table t_aksjonslogg
  drop column arkiv_element;
alter table t_aksjonslogg
  drop column OPPRETTET_AV;

alter table T_AKSJONSLOGG modify JOURNALPOST_ID null;
alter table T_AKSJONSLOGG add constraint check_jpid_dokid check ( (JOURNALPOST_ID is null AND DOKUMENT_INFO_ID is not null) or (JOURNALPOST_ID is not null AND DOKUMENT_INFO_ID is null) or (JOURNALPOST_ID is not null AND DOKUMENT_INFO_ID is not null));

create table t_arkiv_element_endring
(
  arkiv_element_endring_id number(11)    not null,
  tidspunkt                timestamp(6)  not null,
  arkiv_element            varchar2(50)  null,
  fra_verdi                varchar2(200) null,
  til_verdi                varchar2(200) null,
  aksjonslogg_id          number(11)    null
    constraint constraint_aksjons_logg_id
      references T_AKSJONSLOGG,
  constraint arkiv_element_endring_pk primary key (arkiv_element_endring_id)
);

delete from T_AKSJONSLOGG;
delete from T_K_AKSJON_TYPE;

rename T_K_AKSJON_TYPE TO T_K_AKSJONS_TYPE;

INSERT INTO T_K_AKSJONS_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('ENDRE_SKJERMING', 'Endre skjerming på et arkivelement', sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJONS_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('KASSASJON', 'Kassering av et dokument', sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJONS_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('SLETT', 'Sletting av et arkivelement', sysdate, 'Ugur Alpay Cenar');

INSERT INTO T_K_AKSJONS_TYPE (AKSJON_TYPE, BESKRIVELSE, DATO_OPPRETTET, OPPRETTET_AV)
VALUES ('ARKIVERING', 'Arkivering av et nytt variantformat og dokumentfil', sysdate, 'Ugur Alpay Cenar');

CREATE INDEX xiarkivelementendring
  ON t_arkiv_element_endring (tidspunkt);

CREATE INDEX xiaksjonsloggid
  ON t_arkiv_element_endring (aksjonslogg_id);

CREATE SEQUENCE t_arkivelementendring_seq
  CACHE 30;
