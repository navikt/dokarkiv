update T_K_FAGOMRADE
set dato_tom = date '2023-05-05'
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824',
    er_gyldig = '0'
where k_fagomrade in('UKJ', 'SIK', 'SAA', 'OKO', 'CON');

update T_K_FAGOMRADE
set dekode = 'Rehabiliteringspenger',
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824'
where k_fagomrade='REH';

update T_K_FAGOMRADE
set dekode = 'Arbeidsrådgivning – psykologtester',
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824'
where k_fagomrade='ARP';

update T_K_FAGOMRADE
set dekode = 'Klage – lønnsgaranti',
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824'
where k_fagomrade='KLL';

update T_K_FAGOMRADE
set dekode = 'Kontroll – anmeldelse',
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824'
where k_fagomrade='KTA';

update T_K_FAGOMRADE
set dekode = 'Arbeidsrådgivning – skjermet',
    dato_endret = current_timestamp,
    endret_av = 'MMA-6824'
where k_fagomrade='ARS';
