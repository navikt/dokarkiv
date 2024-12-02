insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'DPO',
       'Digital Post Offentlig',
       date '2024-12-02',
       null,
       '1',
       timestamp '2024-12-02 14:00:00',
       'MMA-7426',
       sysdate,
       'MMA-7426'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'DPO');