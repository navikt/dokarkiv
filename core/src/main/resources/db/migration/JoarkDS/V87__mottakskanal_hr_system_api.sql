insert into t_k_mottaks_kanal (k_mottaks_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                               dato_endret, endret_av)
select 'HR_SYSTEM_API',
       'HR-system med integrasjon mot Nav',
       date '2025-04-29',
       null,
       '1',
       current_timestamp,
       'MMA-7996',
       current_timestamp,
       'MMA-7996'
from dual
where not exists(select 1
                 from t_k_mottaks_kanal
                 where k_mottaks_kanal = 'HR_SYSTEM_API');