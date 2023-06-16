update t_k_innsyn
set k_innsyn    = 'SKJULES_BRUKERS_ONSKE',
    endret_av   = 'MMA-6346',
    dato_endret = current_timestamp
where k_innsyn = 'SKJULES_BRUKERS_ØNSKE';
