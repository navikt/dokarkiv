update t_k_utsendings_kanal
set dekode      = 'Taushetsbelagt Post via Altinn',
    dato_endret = current_timestamp,
    endret_av   = 'MMA-7331'
where k_utsendings_kanal = 'DPVT';
