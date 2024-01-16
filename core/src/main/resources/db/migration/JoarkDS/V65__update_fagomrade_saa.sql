update T_K_FAGOMRADE
set dato_tom = date '2024-12-31',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7265',
    er_gyldig = '1'
where k_fagomrade = 'SAA';