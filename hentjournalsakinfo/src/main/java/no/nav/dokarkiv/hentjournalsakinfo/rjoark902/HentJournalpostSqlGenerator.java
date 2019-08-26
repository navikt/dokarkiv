package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import static no.nav.dokarkiv.hentjournalsakinfo.common.SqlProjections.RELEVANTE_DATA;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
final class HentJournalpostSqlGenerator {
	private HentJournalpostSqlGenerator() {
		//ikke instansier
	}

	// Spørringen finner journalposter tilknyttet sak og midlertidige journalposter.
	static String hentJournalpostSql() {
		return "	SELECT" + RELEVANTE_DATA +
				"       	FROM t_journalpost j\n" +
				"				LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
				"               LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id" +
				"               LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema" +
				"               LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id" +
				"				JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"				JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"				LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND fd.k_variant_format IN ('ARKIV', 'SLADDET', 'PRODUKSJON', 'PRODUKSJON_DLF', 'FULLVERSJON', 'ORIGINAL')\n" +
				"				LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id\n" +
				"       		WHERE j.journalpost_id = :journalpostId\n" +
				"	ORDER BY dokumenter_tilknyttetsom ASC, dokumenter_jprelasjonid ASC";
	}
}
