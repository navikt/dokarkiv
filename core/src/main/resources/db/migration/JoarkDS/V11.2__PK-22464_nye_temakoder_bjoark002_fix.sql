update T_K_FAGOMRADE
set dekode = 'Tilleggsstønad arbeidsøkere', dato_endret = timestamp '2015-12-07 12:00:00', opprettet_av='Roar Bjurstrøm', endret_av = 'Hans Petter Simonsen'
where k_fagomrade='TSR'
;
update T_K_FAGOMRADE
set dekode = 'Tilleggsstønad', dato_endret = timestamp '2015-12-07 12:00:00', opprettet_av='Roar Bjurstrøm', endret_av = 'Hans Petter Simonsen'
where k_fagomrade='TSO'
;
