package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import lombok.experimental.UtilityClass;

import static no.nav.dokarkiv.hentjournalsakinfo.common.SqlProjections.RELEVANTE_DATA;

@UtilityClass
class FinnJournalposterStatusSql {
	static String finnJournalposterStatusSql() {
		return "WITH finnjournalposter_status AS (SELECT j.journalpost_id\n" +
				"                                  FROM t_journalpost j\n" +
				"                                  WHERE j.k_journal_s IN (:inkluderJournalStatus)\n" +
				"                                    AND j.k_journalpost_t IN (:inkluderJournalpostType)\n" +
				"                                    AND j.dato_opprettet > :fraDato\n" +
				"),\n" +
				"     relevantedata AS (SELECT " + RELEVANTE_DATA +
				"                       FROM t_journalpost j\n" +
				"                                LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
			    "                                LEFT JOIN sak sa ON sa.id = s.sak_id\n" +
				"               				 LEFT JOIN t_utsendings_info ut ON ut.journalpost_id = j.journalpost_id\n" +
				"                                LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id\n" +
				"                                LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema\n" +
				"                                LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id\n" +
				"                                JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"                                JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"                                LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND\n" +
				"                                                               fd.k_variant_format IN\n" +
				"                                                               ('ARKIV', 'SLADDET', 'PRODUKSJON',\n" +
				"                                                                'FULLVERSJON', 'ORIGINAL')\n" +
				"                                LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id\n" +
				"     )\n" +
				"SELECT r.*,\n" +
				"       journalposter.prevjournalpostid,\n" +
				"       journalposter.nextjournalpostid,\n" +
				"       journalposter.totaltantall\n" +
				"FROM relevantedata r\n" +
				"         JOIN\n" +
				"     (\n" +
				"         SELECT *\n" +
				"         FROM (\n" +
				"                  SELECT *\n" +
				"                  FROM (\n" +
				"                           SELECT j.journalpost_id,\n" +
				"                                  LEAD(j.journalpost_id) OVER (ORDER BY j.journalpost_id) AS prevjournalpostid,\n" +
				"                                  LAG(j.journalpost_id) OVER (ORDER BY j.journalpost_id)  AS nextjournalpostid,\n" +
				"                                  COUNT(*) OVER ()                                        AS totaltantall\n" +
				"                           FROM (SELECT journalpost_id FROM finnjournalposter_status) j\n" +
				"                       ) p\n" +
				"                  WHERE p.journalpost_id < :journalpostIdPeker\n" +
				"                  ORDER BY p.journalpost_id DESC\n" +
				"              ) t\n" +
				"         WHERE rownum <= :antallRader\n" +
				"     ) journalposter ON journalposter.journalpost_id = r.journalpostid\n" +
				"ORDER BY journalpostid DESC, dokumenter_tilknyttetsom ASC, dokumenter_jprelasjonid ASC";
	}

}
