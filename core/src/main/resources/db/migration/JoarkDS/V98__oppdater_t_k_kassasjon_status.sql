delete from t_k_kassasjon_status where k_kassasjon_status in ('KASSASJONSTID_NAADD', 'DOKUMENTER_KASSERT');

alter table t_k_kassasjon_status drop (dato_fom, dato_tom, er_gyldig);

update t_k_kassasjon_status
set dekode = 'Saken er avlevert og kan kasseres',
    endret_av = 'MMA-8271',
    dato_endret = current_timestamp
where k_kassasjon_status = 'KLAR_FOR_KASSASJON';

insert into t_k_kassasjon_status (k_kassasjon_status, dekode, dato_opprettet, opprettet_av)
values ('BEVARINGSTID_PASSERT', 'Sakens bevaringstid er passert og kan avleveres', current_timestamp, 'MMA-8271');

insert into t_k_kassasjon_status (k_kassasjon_status, dekode, dato_opprettet, opprettet_av)
values ('BEVARINGSTID_PASSERT_DOK_KASSASJON_BESTILT', 'Sakens bevaringstid er passert og kan avleveres, og kassering av dokumenter på saken er bestilt', current_timestamp, 'MMA-8271');

insert into t_k_kassasjon_status (k_kassasjon_status, dekode, dato_opprettet, opprettet_av)
values ('BEVARINGSTID_PASSERT_DOK_KASSERT', 'Sakens bevaringstid er passert og kan avleveres, og dokumenter på saken er kassert	', current_timestamp, 'MMA-8271');

insert into t_k_kassasjon_status (k_kassasjon_status, dekode, dato_opprettet, opprettet_av)
values ('KASSERT', 'Saken er kassert', current_timestamp, 'MMA-8271');