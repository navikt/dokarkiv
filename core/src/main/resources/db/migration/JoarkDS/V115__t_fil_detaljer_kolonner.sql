ALTER TABLE t_fil_detaljer
    ADD (
        sha256_sjekksum RAW(32),
        ekstern_dokument_referanse_id VARCHAR2(512 CHAR)
        );
CREATE UNIQUE INDEX uidx_fil_detaljer_ekstern_dok_id ON t_fil_detaljer (ekstern_dokument_referanse_id) ONLINE;
ALTER TABLE t_fil_detaljer
    ADD CONSTRAINT uidx_fil_detaljer_ekstern_dok_id UNIQUE (ekstern_dokument_referanse_id)
        USING INDEX uidx_fil_detaljer_ekstern_dok_id NOVALIDATE;
