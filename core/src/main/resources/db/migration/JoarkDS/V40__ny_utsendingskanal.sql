insert into t_k_utsendings_kanal (k_utsendings_kanal, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet,
                                  opprettet_av, dato_endret, endret_av)
select 'NAV_NO_CHAT',
       'Innlogget samtale',
       date '1900-01-01',
       null,
       '1',
       timestamp '2022-04-06 10:00:00',
       'MMA-6071',
       sysdate,
       'MMA-6071'
from dual
where not exists(
        select 1
        from t_k_utsendings_kanal
        where k_utsendings_kanal = 'NAV_NO_CHAT');
