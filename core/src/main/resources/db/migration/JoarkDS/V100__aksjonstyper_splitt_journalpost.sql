insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'SPLITT',
       'Journalposten ble splittet',
       current_timestamp,
       'MMA-8340'
from dual
where not exists(
    select 1
    from t_k_aksjon_type
    where aksjon_type = 'SPLITT'
);

insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'KOPIER_DOKUMENT',
       'Dokumentet ble kopiert uten endringer til den nye journalposten',
       current_timestamp,
       'MMA-8340'
from dual
where not exists(
    select 1
    from t_k_aksjon_type
    where aksjon_type = 'KOPIER_DOKUMENT'
);

insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'ENDRE_DOKUMENT',
       'Dokumentet ble kopiert med endringer til den nye journalposten',
       current_timestamp,
       'MMA-8340'
from dual
where not exists(
    select 1
    from t_k_aksjon_type
    where aksjon_type = 'ENDRE_DOKUMENT'
);

insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'OPPRETT_FRA_SPLITT',
       'Journalposten ble opprettet på bakgrunn av splitt',
       current_timestamp,
       'MMA-8340'
from dual
where not exists(
    select 1
    from t_k_aksjon_type
    where aksjon_type = 'OPPRETT_FRA_SPLITT'
);