update T_K_FAGOMRADE
set dekode = 'Ajourhold – grunnopplysninger',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='AGR';

update T_K_FAGOMRADE
set dekode = 'Enslig mor eller far',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='ENF';

update T_K_FAGOMRADE
set dekode = 'Feilutbetaling',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='FEI';

update T_K_FAGOMRADE
set dekode = 'Foreldreskap',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='FAR';

update T_K_FAGOMRADE
set dekode = 'Tiltakspenger',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='IND';

update T_K_FAGOMRADE
set dekode = 'Oppfølging – arbeidsgiver',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='OPA';

update T_K_FAGOMRADE
set dekode = 'Rehabiliteringspenger',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='REH';

update T_K_FAGOMRADE
set dekode = 'Rekruttering',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='REK';

update T_K_FAGOMRADE
set dekode = 'Sakskostnader',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='SAK';

update T_K_FAGOMRADE
set dekode = 'Sanksjon – person'
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='SAP';

update T_K_FAGOMRADE
set dekode = 'Tilleggsstønad – arbeidssøkere',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='TSR';

update T_K_FAGOMRADE
set dekode = 'Yrkesskade og menerstatning',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='YRK';

update T_K_FAGOMRADE
set dekode = 'Arbeidsrådgivning – psykologtester',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='ARP';

update T_K_FAGOMRADE
set dekode = 'Arbeidsrådgivning – skjermet',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='ARS';

update T_K_FAGOMRADE
set dekode = 'Klage – lønnsgaranti',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='KLL';

update T_K_FAGOMRADE
set dekode = 'Kontroll – anmeldelse',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='KTA';

update T_K_FAGOMRADE
set dekode = 'Omsorgspenger, pleiepenger og opplæringspenger',
    dato_endret = current_timestamp,
    endret_av = 'MMA-7023'
where k_fagomrade='OMS';
