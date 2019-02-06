alter table t_aksjonslogg
  drop column fra_verdi;
alter table t_aksjonslogg
  drop column til_verdi;
alter table t_aksjonslogg
  drop column arkiv_element;
alter table t_aksjonslogg
  drop column OPPRETTET_AV;

alter table T_AKSJONSLOGG
  modify JOURNALPOST_ID null;

alter table T_AKSJONSLOGG
  add constraint check_jpid_dokid check ( (JOURNALPOST_ID is null AND DOKUMENT_INFO_ID is not null) or
                                          (JOURNALPOST_ID is not null AND DOKUMENT_INFO_ID is null) or
                                          (JOURNALPOST_ID is not null AND DOKUMENT_INFO_ID is not null));

delete from T_AKSJONSLOGG;
alter table T_AKSJONSLOGG drop column AKSJON;
alter table T_AKSJONSLOGG add aksjon varchar2(50) default 'ENDRE_SKJERMING' not null
  constraint t_r_aksjons_type references T_K_AKSJON_TYPE (AKSJON_TYPE);

create table t_arkiv_element_endring
(
  arkiv_element_endring_id number(11)    not null,
  tidspunkt                timestamp(6)  not null,
  arkiv_element            varchar2(200) null,
  fra_verdi                varchar2(200) null,
  til_verdi                varchar2(200) null,
  aksjonslogg_id           number(11)    null
    constraint constraint_aksjons_logg_id
      references T_AKSJONSLOGG,
  constraint arkiv_element_endring_pk primary key (arkiv_element_endring_id)
);

rename T_K_AKSJON_TYPE TO T_K_AKSJONS_TYPE;

update T_K_AKSJONS_TYPE set BESKRIVELSE='Endre skjerming på et arkivelement' where AKSJON_TYPE='ENDRE_BEGRENSNING';
update T_K_AKSJONS_TYPE set AKSJON_TYPE='ENDRE_SKJERMING' where AKSJON_TYPE='ENDRE_BEGRENSNING';

update T_K_AKSJONS_TYPE set BESKRIVELSE='Kassering av et dokument' where AKSJON_TYPE='KASSASJON';
update T_K_AKSJONS_TYPE set BESKRIVELSE='Sletting av et arkivelement' where AKSJON_TYPE='SLETT';
update T_K_AKSJONS_TYPE set BESKRIVELSE='Endre skjerming på et arkivelement' where AKSJON_TYPE='ENDRE_SKJERMING';
update T_K_AKSJONS_TYPE set BESKRIVELSE='Arkivering av et nytt variantformat og dokumentfil' where AKSJON_TYPE='ARKIVERING';

update T_AKSJONSLOGG set AKSJON='ENDRE_SKJERMING' where AKSJON='ENDRE_BEGRENSNING';

CREATE INDEX xiarkivelementendring
  ON t_arkiv_element_endring (tidspunkt);

CREATE INDEX xiaksjonsloggid
  ON t_arkiv_element_endring (aksjonslogg_id);

CREATE SEQUENCE t_arkivelementendring_seq
  CACHE 30;
