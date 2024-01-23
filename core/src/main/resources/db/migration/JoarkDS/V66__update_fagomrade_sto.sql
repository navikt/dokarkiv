
update T_K_FAGOMRADE
set dekode = 'Regnskap/utbetaling/årsoppgave',
    dato_endret = timestamp '2023-01-23 14:00:00',
    endret_av = 'MMA-7278'
where k_fagomrade='STO'
;