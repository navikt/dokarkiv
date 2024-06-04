update T_K_FAGOMRADE
set dato_tom = date '2024-06-04',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7266',
    er_gyldig = '0'
where k_fagomrade in ('SAA', 'MOB');