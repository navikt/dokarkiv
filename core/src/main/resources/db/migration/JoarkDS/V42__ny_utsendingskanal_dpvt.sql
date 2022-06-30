insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'DPVT',
       'Taushetsbelagt digital post til virksomhet',
       date '1900-01-01',
       null,
       '1',
       timestamp '2022-06-28 16:00:00',
       'MMA-6226',
       sysdate,
       'MMA-6226'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'DPVT');

DELETE FROM t_k_utsendings_kanal WHERE k_utsendings_kanal = 'DPV';
DELETE FROM t_k_utsendings_kanal WHERE k_utsendings_kanal = 'DPVS';