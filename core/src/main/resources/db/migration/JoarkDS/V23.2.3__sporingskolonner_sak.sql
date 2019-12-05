-- Få med nye kolonner i insert/update trigger på datavarehus-tabell
create or replace TRIGGER trg_sak_insert_update
    AFTER INSERT OR UPDATE ON sak
    FOR EACH ROW
DECLARE
    endringstype CHAR(1);
BEGIN
    IF updating THEN endringstype := 'U'; END IF;
    IF inserting THEN endringstype :='I'; END IF;

    INSERT INTO sak_gr
    (sak_gr_id, sak_id, aktoerid, orgnr, tema, applikasjon, fagsaknr, opprettet_av, opprettet_tidspunkt, endret_kilde_navn, dato_endret, dato_overfort_grensesnitt, endring_type)
    VALUES
    (seq_sak_gr.nextval, :new.id, :new.aktoerid, :new.orgnr, :new.tema, :new.applikasjon, :new.fagsaknr, :new.opprettet_av, :new.opprettet_tidspunkt, :new.endret_kilde_navn, :new.dato_endret, CURRENT_TIMESTAMP, endringstype);
END;
/