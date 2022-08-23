insert into t_k_aksjon_type (aksjon_type, beskrivelse, dato_opprettet, opprettet_av)
select 'OVERSTYR_INNSYN',
       'Reglene for innsyn på nav.no ble overstyrt',
       current_timestamp,
       'MMA-6327'
from dual
where not exists(
        select 1
        from t_k_aksjon_type
        where aksjon_type = 'OVERSTYR_INNSYN'
    );
