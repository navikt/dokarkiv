package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.dokarkiv.hentjournalsakinfo.common.SqlProjections.RELEVANTE_DATA;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerSpringJdbcRepository.CTE_ALIAS_AKTOERID;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerSpringJdbcRepository.CTE_ALIAS_MIDLERTIDIGE;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerSpringJdbcRepository.CTE_ALIAS_ORGNR;
import static no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerSpringJdbcRepository.CTE_ALIAS_PSAKSAKER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentoversiktBrukerSqlGeneratorTest {
	@Test
	public void shouldGenerateSqlWhenFeilregistrertWanted() {
		String sql = DokumentoversiktBrukerSqlGenerator.feilregistrertSelectionSql(true);
		assertThat(sql, is("(s.feilregistrert = 1)"));
	}

	@Test
	public void shouldGenerateSqlWhenFeilregistrertNotWanted() {
		String sql = DokumentoversiktBrukerSqlGenerator.feilregistrertSelectionSql(false);
		assertThat(sql, is("(s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))"));
	}

	@Test
	public void shouldGenerateCteUnionSql() {
		String sql = DokumentoversiktBrukerSqlGenerator.generateCteUnionSql(Arrays.asList(CTE_ALIAS_AKTOERID, CTE_ALIAS_ORGNR, CTE_ALIAS_PSAKSAKER, CTE_ALIAS_MIDLERTIDIGE));
		assertThat(sql, is("SELECT journalpost_id FROM journalpostid_aktoerId " +
				"UNION SELECT journalpost_id FROM journalpostid_orgnr " +
				"UNION SELECT journalpost_id FROM journalpostid_psak " +
				"UNION SELECT journalpost_id FROM journalpostid_midlertidige"));
	}

	@Test
	public void shouldGenerateDokumentoversiktBrukerSql() {
		DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo = new DokumentoversiktBrukerRequestTo();
		dokumentoversiktBrukerRequestTo.setFraDato(LocalDate.ofYearDay(1, 1));
		dokumentoversiktBrukerRequestTo.setFoerste(1);
		dokumentoversiktBrukerRequestTo.setInkluderJournalStatus(Collections.singletonList(JournalStatusCode.J));
		dokumentoversiktBrukerRequestTo.setInkluderJournalpostType(Collections.singletonList(JournalpostTypeCode.I));
		dokumentoversiktBrukerRequestTo.setPsakSakIds(Arrays.asList("P1", "P2"));
		dokumentoversiktBrukerRequestTo.setAktoerId("10000000000");
		dokumentoversiktBrukerRequestTo.setAlleIdenter(Collections.singletonList("00000000000"));
		DokumentoversiktBrukerFilter dokumentoversiktBrukerFilter = new DokumentoversiktBrukerFilter(dokumentoversiktBrukerRequestTo);
		String sql = DokumentoversiktBrukerSqlGenerator.dokumentoversiktBrukerSql(dokumentoversiktBrukerFilter, Arrays.asList(CTE_ALIAS_AKTOERID, CTE_ALIAS_PSAKSAKER, CTE_ALIAS_MIDLERTIDIGE));
		assertThat(sql, is(
				"WITH journalpostid_psak AS\n" +
						"         (SELECT s.journalpost_id\n" +
						"          FROM t_saksrelasjon s\n" +
						"          WHERE (s.k_fagsystem = 'PEN' AND s.sak_nr_fk IN (:psakIds))\n" +
						"            AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"         ),\n" +
						"     journalpostid_aktoerid AS\n" +
						"         (SELECT s.journalpost_id\n" +
						"          FROM sak sa\n" +
						"                   JOIN t_saksrelasjon s ON to_char(sa.id) = s.sak_nr_fk AND s.k_fagsystem = 'FS22'\n" +
						"          WHERE sa.aktoerid = :aktoerId\n" +
						"            AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"         ),\n" +
						"     journalpostid_orgnr AS\n" +
						"         (SELECT s.journalpost_id\n" +
						"          FROM sak sa\n" +
						"                   JOIN t_saksrelasjon s ON to_char(sa.id) = s.sak_nr_fk AND s.k_fagsystem = 'FS22'\n" +
						"          WHERE sa.orgnr = :orgnr\n" +
						"            AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"         ),\n" +
						"     journalpostid_midlertidige AS (SELECT b.journalpost_id\n" +
						"                                    FROM t_bruker b\n" +
						"                                             JOIN t_journalpost tj ON b.journalpost_id = tj.journalpost_id\n" +
						"                                             LEFT JOIN t_saksrelasjon s ON tj.journalpost_id = s.journalpost_id\n" +
						"                                    WHERE b.bruker_id IN (:alleIdenter)\n" +
						"                                      AND tj.k_journal_s IN ('M', 'MO', 'D')\n" +
						"                                      AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"     ),\n" +
						"     relevantedata AS (SELECT " + RELEVANTE_DATA +
						"                       FROM t_journalpost j\n" +
						"                                LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
						"                                LEFT JOIN sak sa ON sa.id = to_number(s.sak_nr_fk)\n" +
						"                                LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id\n" +
						"                                LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema\n" +
						"                                LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id\n" +
						"                                JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
						"                                JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
						"                                LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND\n" +
						"                                                               fd.k_variant_format IN\n" +
						"                                                               ('ARKIV', 'SLADDET', 'PRODUKSJON', 'PRODUKSJON_DLF',\n" +
						"                                                                'FULLVERSJON', 'ORIGINAL')\n" +
						"                                LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id)\n" +
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
						"                           FROM (SELECT journalpost_id FROM journalpostid_aktoerId UNION SELECT journalpost_id FROM journalpostid_psak UNION SELECT journalpost_id FROM journalpostid_midlertidige) jps\n" +
						"                                    JOIN t_journalpost j ON jps.journalpost_id = j.journalpost_id\n" +
						"                                    LEFT JOIN t_saksrelasjon ts ON j.journalpost_id = ts.journalpost_id\n" +
						"\n" +
						"                           WHERE j.k_journalpost_t IN (:inkluderJournalpostType)\n" +
						"                             AND j.dato_opprettet > :fraDato\n" +
						"                             AND (\n" +
						"                                   (ts.feilregistrert = 1 AND\n" +
						"                                    j.k_journal_s IN (:allJournalStatus))\n" +
						"                                   OR (ts.feilregistrert IS NULL AND\n" +
						"                                       j.k_journal_s IN (:inkluderJournalStatus))\n" +
						"                                   OR (ts.feilregistrert = 0 AND\n" +
						"                                       j.k_journal_s IN (:inkluderJournalStatus))\n" +
						"                               )\n" +
						"                       ) p\n" +
						"                  WHERE p.journalpost_id < :journalpostIdPeker\n" +
						"                  ORDER BY p.journalpost_id DESC\n" +
						"              ) t\n" +
						"         WHERE rownum <= :antallRader\n" +
						"     ) journalposter ON journalposter.journalpost_id = r.journalpostid\n" +
						"ORDER BY journalpostid DESC, dokumenter_tilknyttetsom ASC, dokumenter_jprelasjonid ASC"));
	}
}