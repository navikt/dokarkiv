package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.hentjournalsakinfo.JournalpostFilter;

import java.util.List;
import java.util.stream.Collectors;

import static no.nav.dokarkiv.hentjournalsakinfo.common.SqlProjections.RELEVANTE_DATA;

final class FinnJournalpostSqlGenerator {
	private FinnJournalpostSqlGenerator() {
		//ikke instansier
	}

	static String feilregistrertSelectionSql(boolean kunFeilregistrerte) {
		if (kunFeilregistrerte) {
			return "(s.feilregistrert = 1)";
		} else {
			return "(s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))";
		}
	}

	// Spørringen finner journalposter tilknyttet sak og midlertidige journalposter.
	// Den inkluderer felt som behøves for SAF tilgangsmodell og visningsmodell.
	// Aliasnavn kan ikke være lengre enn 30 tegn i Oracle
	static String finnJournalposterSql(JournalpostFilter journalpostFilter, List<String> cteAliases, String gsakCte, Boolean parallell) {
		return "WITH psaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'PEN' AND s.sak_id IN (:psakIds))\n" +
				"          AND " + feilregistrertSelectionSql(journalpostFilter.isKunFeilregistrerte()) + "\n" +
				"       ),\n" +
				gsakCte +
				"     midlertidige AS (SELECT b.journalpost_id\n" +
				"                      FROM t_bruker b\n" +
				"                               JOIN t_journalpost tj ON b.journalpost_id = tj.journalpost_id\n" +
				"                               LEFT JOIN t_saksrelasjon s ON tj.journalpost_id = s.journalpost_id\n" +
				"                      WHERE b.bruker_id IN (:alleIdenter)\n" +
				"                        AND tj.k_journal_s IN ('M', 'MO', 'D')\n" +
				"                        AND " + feilregistrertSelectionSql(journalpostFilter.isKunFeilregistrerte()) + "\n" +
				"     ),\n" +
				"     relevantedata AS (SELECT " + RELEVANTE_DATA +
				"                       FROM t_journalpost j\n" +
				"                              LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
				"                              LEFT JOIN t_utsendings_info ut ON ut.journalpost_id = j.journalpost_id\n" +
				"                              LEFT JOIN sak sa ON sa.id = s.sak_id\n" +
				"                              LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id\n" +
				"                              LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema\n" +
				"                              LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id\n" +
				"                              JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"                              JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"                              LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND fd.k_variant_format IN ('ARKIV', 'SLADDET', 'PRODUKSJON', 'FULLVERSJON', 'ORIGINAL')\n" +
				"                              LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id" +
				"     )\n" +
				"SELECT " + (parallell ? "/*+ PARALLEL(8) */" : "") + " r.*,\n" +
				"       journalposter.prevjournalpostid,\n" +
				"       journalposter.nextjournalpostid,\n" +
				"       journalposter.totaltAntall\n" +
				"FROM relevantedata r\n" +
				"       JOIN\n" +
				"       (\n" +
				"         SELECT *\n" +
				"         FROM (\n" +
				"                SELECT *\n" +
				"                FROM (\n" +
				"                       SELECT " + (parallell ? "/*+ PARALLEL(8) */" : "") + " j.journalpost_id,\n" +
				"                              LEAD(j.journalpost_id) OVER (ORDER BY j.journalpost_id) AS prevjournalpostid,\n" +
				"                              LAG(j.journalpost_id) OVER (ORDER BY j.journalpost_id)  AS nextjournalpostid,\n" +
				"                              COUNT(*) OVER ()  AS totaltAntall\n" +
				"                       FROM (" + generateCteUnionSql(cteAliases) + ") jps\n" +
				"                              JOIN t_journalpost j ON jps.journalpost_id = j.journalpost_id\n" +
				"                              LEFT JOIN t_saksrelasjon ts ON j.journalpost_id = ts.journalpost_id\n" +
				"\n" +
				"                       WHERE j.k_journalpost_t IN (:inkluderJournalpostType)\n" +
				"                         AND trunc(j.dato_opprettet) >= :fraDato\n" +
				generateTilDato(journalpostFilter) +
				"                         AND (\n" +
				"                           (ts.feilregistrert = 1 AND\n" +
				"                            j.k_journal_s IN (:allJournalStatus))\n" +
				"                           OR (ts.feilregistrert IS NULL AND\n" +
				"                               j.k_journal_s IN (:inkluderJournalStatus))\n" +
				"                           OR (ts.feilregistrert = 0 AND\n" +
				"                               j.k_journal_s IN (:inkluderJournalStatus))\n" +
				"                         )\n" +
				"                     ) p\n" +
				"                WHERE p.journalpost_id < :journalpostIdPeker ORDER BY p.journalpost_id DESC" +
				"              ) t\n" +
				"         WHERE rownum <= :antallRader\n" +
				"       ) journalposter ON journalposter.journalpost_id = r.journalpostid\n" +
				"ORDER BY journalpostid DESC, dokumenter_tilknyttetsom ASC, dokumenter_jprelasjonid ASC";
	}

	static String generateCteUnionSql(List<String> cteAliases) {
		return cteAliases.stream().map(cteAlias -> "SELECT journalpost_id FROM " + cteAlias).collect(Collectors.joining(" UNION "));
	}

	private static String generateTilDato(JournalpostFilter journalpostFilter) {
		return journalpostFilter.getTilDato() == null ? "" : " AND trunc(j.dato_opprettet) <= :tilDato\n";
	}
}
