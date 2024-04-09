insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'TILKNYTT_NYTT_DOKUMENT',
       'Last opp nytt dokument som vedlegg',
       current_timestamp,
       'MMA-7325'
from dual
where not exists(
    select 1
    from t_k_aksjon_type
    where aksjon_type = 'TILKNYTT_NYTT_DOKUMENT'
);