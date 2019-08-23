CREATE TABLE sak_gr (
    sak_gr_id                  NUMBER(38)    NOT NULL,
    sak_id                     NUMBER(10),
    aktoerid                   VARCHAR2(40),
    orgnr                      VARCHAR2(9),
    tema                       VARCHAR2(40),
    applikasjon                VARCHAR2(40),
    fagsaknr                   VARCHAR2(40),
    opprettet_av               VARCHAR2(40),
    opprettet_tidspunkt        TIMESTAMP,
    dato_overfort_grensesnitt  TIMESTAMP     NOT NULL,
    endring_type               CHAR(1)       NOT NULL,

    CONSTRAINT pk_sak_gr PRIMARY KEY (sak_gr_id)
);

CREATE SEQUENCE seq_sak_gr;

create TRIGGER trg_sak_delete
AFTER DELETE ON sak
FOR EACH ROW BEGIN
    INSERT INTO sak_gr
    (sak_gr_id, sak_id, tema, applikasjon, fagsaknr, opprettet_av, opprettet_tidspunkt, dato_overfort_grensesnitt, endring_type)
    VALUES
    (seq_sak_gr.nextval, :old.id, :old.tema, :old.applikasjon, :old.fagsaknr, :old.opprettet_av, :old.opprettet_tidspunkt, CURRENT_TIMESTAMP, 'D');
END;
/

create TRIGGER trg_sak_insert_update
AFTER INSERT OR UPDATE ON sak
FOR EACH ROW
DECLARE
    endringstype CHAR(1);
BEGIN
    IF updating THEN endringstype := 'U'; END IF;
    IF inserting THEN endringstype :='I'; END IF;

    INSERT INTO sak_gr
    (sak_gr_id, sak_id, aktoerid, orgnr, tema, applikasjon, fagsaknr, opprettet_av, opprettet_tidspunkt, dato_overfort_grensesnitt, endring_type)
    VALUES
    (seq_sak_gr.nextval, :new.id, :new.aktoerid, :new.orgnr, :new.tema, :new.applikasjon, :new.fagsaknr, :new.opprettet_av, :new.opprettet_tidspunkt, CURRENT_TIMESTAMP, endringstype);
END;
/
