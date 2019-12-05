-- Få med nye kolonner i delete trigger på datavarehus-tabell
create or replace TRIGGER trg_sak_delete
    AFTER DELETE ON sak
    FOR EACH ROW BEGIN
    INSERT INTO sak_gr
    (sak_gr_id, sak_id, tema, applikasjon, fagsaknr, opprettet_av, opprettet_tidspunkt, endret_kilde_navn, dato_endret,  dato_overfort_grensesnitt, endring_type)
    VALUES
    (seq_sak_gr.nextval, :old.id, :old.tema, :old.applikasjon, :old.fagsaknr, :old.opprettet_av, :old.opprettet_tidspunkt, :old.endret_kilde_navn, :old.dato_endret, CURRENT_TIMESTAMP, 'D');
END;
/