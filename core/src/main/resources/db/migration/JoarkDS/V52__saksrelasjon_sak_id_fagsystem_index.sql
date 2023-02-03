create index idx_t_saksrelasjon_sak_id_k_fagsystem
    on t_saksrelasjon (sak_id, k_fagsystem) online parallel;