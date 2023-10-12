alter table T_K_FAGOMRADE
    add (
        beskrivelse VARCHAR2(200),
        );

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('NAV utreder og belyser saken på forespørsel fra Statens sivilrettsforvaltning')
where k_fagomrade='RVE';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Krav om økonomisk erstatning fordi NAV har gjort en feil')
where k_fagomrade='ERS';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Dokumentasjon knyttet til NAVs rekrutteringsbistand til arbeidsgivere')
where k_fagomrade='REK';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Intensjonsavtalen om et mer inkluderende arbeidsliv: Samarbeidsavtaler, mål- og handlingsplaner. Noe tilskudd',)
where k_fagomrade='IAR';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Endring av bankkonto eller midlertidige adresser')
where k_fagomrade='AGR';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Vedtak om stans av sykepenger, og behandling av klager og anker')
where k_fagomrade='SAP';

insert into T_K_FAGOMRADE (beskrivelse)
VALUES ('Samhandling mellom NAV og arbeidsgivere, utover det som omfattes av øvrige fagområder')
where k_fagomrade='OPA';