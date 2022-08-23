alter table t_journalpost
    add (
        k_innsyn VARCHAR2(50)
            constraint fk_k_innsyn references t_k_innsyn (k_innsyn)
        );
