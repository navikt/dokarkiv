package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import static no.nav.dokarkiv.hentjournalsakinfo.common.SqlProjections.RELEVANTE_DATA;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
final class TilknyttedeJournalposterSql {
	private TilknyttedeJournalposterSql() {
		// ingen instansiering
	}

	static final String GJENBRUKTE_JOURNALPOSTER_SQL =
			"WITH gjenbrukte_journalposter(journalpostid) AS (\n" +
					"  SELECT journalpost_id\n" +
					"  FROM t_jp_dok_info_rel\n" +
					"  WHERE dokument_info_id = :dokumentInfoId\n" +
					"),\n" +
					"     saf_visning_tilgang AS (\n" +
					"       SELECT " + RELEVANTE_DATA +
					"       FROM t_journalpost j\n" +
					"              LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
					"              LEFT JOIN sak sa ON sa.id = to_number(s.sak_nr_fk)\n" +
					"              LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id\n" +
					"              LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema\n" +
					"              LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id\n" +
					"              JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
					"              JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
					"              LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND\n" +
					"                                             fd.k_variant_format IN ('ARKIV', 'SLADDET', 'PRODUKSJON_DLF', 'FULLVERSJON', 'ORIGINAL')\n" +
					"              LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id\n" +
					"       ORDER BY j.journalpost_id DESC, dokumenter_tilknyttetsom ASC, dokumenter_jprelasjonid ASC\n" +
					"     )\n" +
					"SELECT *\n" +
					"FROM saf_visning_tilgang svt\n" +
					"WHERE svt.journalpostid IN (\n" +
					"  SELECT journalpostid\n" +
					"  FROM gjenbrukte_journalposter\n" +
					")";
}
