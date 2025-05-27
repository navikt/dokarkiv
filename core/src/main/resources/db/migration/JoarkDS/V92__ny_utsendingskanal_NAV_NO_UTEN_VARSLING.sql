insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'NAV_NO_UTEN_VARSLING',
       'Presentert direkte på nav.no for innlogget bruker',
       date '2025-05-27',
       null,
       '1',
       timestamp '2025-05-27 14:00:00',
       'MMA-8128',
       sysdate,
       'MMA-8128'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'NAV_NO_UTEN_VARSLING');