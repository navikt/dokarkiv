ALTER TABLE t_journalpost
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_jp_dok_info_rel
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_dokument_info
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_fil_detaljer
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_dokument_fil
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_bruker
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_saksrelasjon
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_skannet_innhold
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));

ALTER TABLE t_kryssreferanse
    MODIFY (opprettet_av varchar2(40),
            endret_av varchar2(40),
            opprettet_kilde_navn varchar2(40),
            endret_kilde_navn varchar2(40));
