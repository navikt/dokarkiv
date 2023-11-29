update T_K_FAGOMRADE
set dato_tom = date '2023-11-28',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7207',
    er_gyldig = '0'
where k_fagomrade = 'RVE';