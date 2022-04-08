insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'DPV',
       'Digital Post til Virksomhet',
       date '1900-01-01',
       null,
       '1',
       timestamp '2022-04-07 16:00:00',
       'MMA-6045',
       sysdate,
       'MMA-6045'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'DPV');

insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'DPVS',
       'Digital Post til Virksomhet (sensitiv)',
       date '1900-01-01',
       null,
       '1',
       timestamp '2022-04-07 16:00:00',
       'MMA-6045',
       sysdate,
       'MMA-6045'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'DPVS');
