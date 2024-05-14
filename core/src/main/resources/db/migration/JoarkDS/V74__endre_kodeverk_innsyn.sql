DELETE FROM t_k_innsyn WHERE k_innsyn = 'SKJULES_BRUKERS_ØNSKE';

insert into t_k_innsyn (k_innsyn, beskrivelse, dato_opprettet, opprettet_av)
values ('SKJULES_BRUKERS_SIKKERHET', 'Skjules på nav.no for å ivareta brukers sikkerhet',
        current_timestamp, 'MMA-6995');