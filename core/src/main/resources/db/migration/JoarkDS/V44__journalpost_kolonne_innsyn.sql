alter table t_journalpost
    add k_innsyn VARCHAR2(50);

alter table t_journalpost
    add constraint fk_k_innsyn
        foreign key (k_innsyn) references t_k_innsyn;
