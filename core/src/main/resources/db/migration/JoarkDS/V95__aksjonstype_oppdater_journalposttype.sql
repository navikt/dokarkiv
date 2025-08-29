insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'ENDRE_JOURNALPOSTTYPE',
       'Journalposttypen ble endret for journalpost',
       current_timestamp,
       'MMA-8234'
from dual
where not exists(
        select 1
        from t_k_aksjon_type
        where aksjon_type = 'ENDRE_JOURNALPOSTTYPE'
    );
