insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'SLADD_DOKUMENT',
       'Dokument tilhørende journalposten og dokumentinfo ble sladdet',
       current_timestamp,
       'MMA-8693'
from dual
where not exists(
        select 1
        from t_k_aksjon_type
        where aksjon_type = 'SLADD_DOKUMENT'
    );
