-- legg til sporingskolonner i datavarehus-tabell
ALTER TABLE SAK_GR
    ADD
        (
        ENDRET_KILDE_NAVN VARCHAR2(20),
        DATO_ENDRET TIMESTAMP(6)
        );