alter table T_DOKUMENT_FIL_OPPLASTING
    add (FORSOK number default 0);
insert into T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND (K_OPPLASTING_TILSTAND, DEKODE, DATO_OPPRETTET, OPPRETTET_AV)
values ('UNDER_OPPLASTING', 'Dokumentfil lastes opp til ekstern lagring', current_timestamp, 'MMA-8895');
delete from T_K_DOKUMENT_FIL_OPPLASTING_TILSTAND where K_OPPLASTING_TILSTAND = 'LASTER_OPP';
