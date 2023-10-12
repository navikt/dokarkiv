alter table T_K_FAGOMRADE
    add (
        beskrivelse VARCHAR2(200)
        );

update T_K_FAGOMRADE set beskrivelse = 'NAV utreder og belyser saken på forespørsel fra Statens sivilrettsforvaltning'
where k_fagomrade='RVE';

update T_K_FAGOMRADE set beskrivelse = 'Krav om økonomisk erstatning fordi NAV har gjort en feil'
where k_fagomrade='ERS';

update T_K_FAGOMRADE set beskrivelse = 'Dokumentasjon knyttet til NAVs rekrutteringsbistand til arbeidsgivere'
where k_fagomrade='REK';

update T_K_FAGOMRADE set beskrivelse = 'Intensjonsavtalen om et mer inkluderende arbeidsliv: Samarbeidsavtaler, mål- og handlingsplaner. Noe tilskudd'
where k_fagomrade='IAR';

update T_K_FAGOMRADE set beskrivelse = 'Endring av bankkonto eller midlertidige adresser'
where k_fagomrade='AGR';

update T_K_FAGOMRADE set beskrivelse = 'Vedtak om stans av sykepenger, og behandling av klager og anker'
where k_fagomrade='SAP';

update T_K_FAGOMRADE set beskrivelse = 'Samhandling mellom NAV og arbeidsgivere, utover det som omfattes av øvrige fagområder'
where k_fagomrade='OPA';
