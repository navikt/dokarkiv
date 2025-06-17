create index idx_ekspederte_journalposter on t_journalpost (k_utsendings_kanal, dato_ekspedert) online parallel 8;
drop index XIF6JOURNALP;