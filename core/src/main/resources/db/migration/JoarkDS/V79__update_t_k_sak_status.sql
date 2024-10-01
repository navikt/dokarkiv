insert into T_K_SAK_STATUS (k_sak_status, dekode, dato_fom, dato_tom, er_gyldig, dato_opprettet, opprettet_av,
                            dato_endret, endret_av)
VALUES ('AVBRUTT', 'Avbrutt', date '2024-10-01', null, '1', timestamp '2024-10-01 12:00:00',
        'MMA-7626', null, null);

DELETE FROM T_K_SAK_STATUS WHERE k_sak_status = 'KAN_KASSERES';
DELETE FROM T_K_SAK_STATUS WHERE k_sak_status = 'KASSERT';
DELETE FROM T_K_SAK_STATUS WHERE k_sak_status = 'KAN_AVLEVERES';
DELETE FROM T_K_SAK_STATUS WHERE k_sak_status = 'AVLEVERT_GODKJENNING';
DELETE FROM T_K_SAK_STATUS WHERE k_sak_status = 'KAN_SLETTES';