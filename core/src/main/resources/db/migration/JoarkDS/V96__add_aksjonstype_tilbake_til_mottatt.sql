insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'TILBAKE_TIL_MOTTATT',
       'Journalstatus endret tilbake til "mottatt" for journalpost',
       current_timestamp,
       'MMA-8237'
from dual
where not exists(
        select 1
        from t_k_aksjon_type
        where aksjon_type = 'TILBAKE_TIL_MOTTATT'
    );
