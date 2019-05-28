package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
final class HentJournalpostSqlGenerator {
	private HentJournalpostSqlGenerator() {
		//ikke instansier
	}

	// Spørringen finner journalposter tilknyttet sak og midlertidige journalposter.
	static String hentJournalpostSql() {
		return "	SELECT j.journalpost_id      AS journalpostid,\n" +
				"		j.innhold             AS innhold,\n" +
				"		j.k_fagomrade         AS fagomrade,\n" +
				"		j.k_behandlingstema   AS behandlingstema,\n" +
				"		bt.dekode             AS behandlingstemanavn,\n" +
				"		j.k_journal_s         AS journalstatus,\n" +
				"       j.avsend_mottak_id    AS avsendermottakerid,\n" +
				"		j.avsend_mottaker     AS avsendermottakernavn,\n" +
				"		j.land                AS avsendermottakerland,\n" +
				"		j.journalf_enhet      AS journalforendeenhet,\n" +
				"		j.journalfort_av_navn AS journalfortavnavn,\n" +
				"		j.opprettet_av_navn   AS opprettetavnavn,\n" +
				"		j.k_mottaks_kanal     AS mottakskanal,\n" +
				"		j.k_utsendings_kanal  AS utsendingskanal,\n" +
				"		j.k_journalpost_t     AS journalposttype,\n" +
				"		j.dato_opprettet      AS datoopprettet,\n" +
				"		j.dato_ekspedert      AS ekspedertdato,\n" +
				"		j.dato_mottatt        AS mottattdato,\n" +
				"		j.dato_journal        AS journaldato,\n" +
				"		j.dato_dokument       AS dokumentdato,\n" +
				"		j.dato_avs_retur      AS avsreturdato,\n" +
				"		j.dato_sendt_print    AS sendtprintdato,\n" +
				"		j.k_skjerming_type    AS skjerming,\n" +
				"		s.sak_nr_fk           AS saksrelasjon_sakid,\n" +
				"		s.feilregistrert      AS saksrelasjon_feilregistrert,\n" +
				"		s.k_fagsystem         AS saksrelasjon_fagsystem,\n" +
				"		b.bruker_id           AS bruker_brukerid,\n" +
				"		b.k_bruker_t          AS bruker_brukeridtype,\n" +
				"		t.nokkel              AS tilleggsopplysninger_nokkel,\n" +
				"       t.verdi               AS tilleggsopplysninger_verdi,\n" +
				"		d.dokument_info_id    AS dokumenter_dokumentinfoid,\n" +
				"		rel.k_tilkn_jp_som    AS dokumenter_tilknyttetsom,\n" +
				"		d.k_dokument_s        AS dokumenter_dokumentstatus,\n" +
				"       d.dato_dok_ferdig     AS dokumenter_datoferdigstilt,\n" +
				"		d.brev_kode           AS dokumenter_brevkode,\n" +
				"       d.dokumenttype_id     AS dokumenter_dokumenttypeid,\n" +
				"		d.tittel              AS dokumenter_tittel,\n" +
				"		d.kassert             AS dokumenter_kassert,\n" +
				"		rel.k_skjerming_type  AS dokumenter_skjerming,\n" +
				"       d.orig_journalpost_id AS dokumenter_origjournalpostid,\n" +
				"		fd.k_skjerming_type   AS dokumenter_varianter_skjerming,\n" +
				"		fd.k_variant_format   AS dokumenter_varianter_variantf,\n" +
				"		fd.fil_navn           AS dokumenter_varianter_filnavn,\n" +
				"	    fd.fil_uuid           AS dokumenter_varianter_filuuid,\n" +
				"       tsi.skannet_innhold_id AS dokumenter_logiske_vedleggid,\n" +
				"		tsi.vedlegg_innhold   AS dokumenter_logiske_tittel\n" +
				"       	FROM t_journalpost j\n" +
				"				LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
				"               LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id" +
				"               LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema" +
				"               LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id" +
				"				JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"				JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"				LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND fd.k_variant_format IN ('ARKIV', 'SLADDET', 'PRODUKSJON', 'PRODUKSJON_DLF', 'FULLVERSJON')\n" +
				"				LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id\n" +
				"       		WHERE j.journalpost_id = :journalpostId\n" +
				"	ORDER BY dokumenter_tilknyttetsom ASC";
	}
}
