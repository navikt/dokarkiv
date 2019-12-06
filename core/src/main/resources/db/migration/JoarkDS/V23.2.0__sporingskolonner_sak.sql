-- legg til sporingskolonner for sporing ved manuell patching (f.eks. splitt/merge av aktoerid)
ALTER TABLE SAK
    ADD
        (
        ENDRET_KILDE_NAVN VARCHAR2(20),
        DATO_ENDRET TIMESTAMP(6)
        );