update t_k_mottaks_kanal
set dekode      = 'Innlogget samtale',
    endret_av   = 'MMA-6071',
    dato_endret = current_timestamp
where k_mottaks_kanal = 'NAV_NO_CHAT';

update t_k_mottaks_kanal
set dekode      = 'Registrert av Nav-ansatt',
    endret_av   = 'MMA-6071',
    dato_endret = current_timestamp
where k_mottaks_kanal = 'INNSENDT_NAV_ANSATT';
