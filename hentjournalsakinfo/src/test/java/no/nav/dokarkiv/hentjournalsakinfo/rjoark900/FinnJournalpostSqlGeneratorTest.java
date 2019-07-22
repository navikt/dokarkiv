package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class FinnJournalpostSqlGeneratorTest {
	@Test
	public void shouldGenerateSqlWhenFeilregistrertWanted() {
		String sql = FinnJournalpostSqlGenerator.feilregistrertSelectionSql(true);
		assertThat(sql, is("(s.feilregistrert = 1)"));
	}

	@Test
	public void shouldGenerateSqlWhenFeilregistrertNotWanted() {
		String sql = FinnJournalpostSqlGenerator.feilregistrertSelectionSql(false);
		assertThat(sql, is("(s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))"));
	}

	@Test
	public void shouldGenerateCteUnionSql() {
		String sql = FinnJournalpostSqlGenerator.generateCteUnionSql(Arrays.asList("gsaker", "psaker"));
		assertThat(sql, is("SELECT journalpost_id FROM gsaker UNION ALL SELECT journalpost_id FROM psaker"));
	}

	@Test
	public void shouldGenerateOrderingWhenSliceFoerste() {
		String sql = FinnJournalpostSqlGenerator.paginateSql(JournalpostFilter.Slice.FOERSTE);
		assertThat(sql, is("p.journalpost_id < :journalpostIdPeker ORDER BY p.journalpost_id DESC"));
	}

	@Test
	public void shouldGenerateOrderingWhenSliceSiste() {
		String sql = FinnJournalpostSqlGenerator.paginateSql(JournalpostFilter.Slice.SISTE);
		assertThat(sql, is("p.journalpost_id > :journalpostIdPeker ORDER BY p.journalpost_id ASC"));
	}

	@Test
	public void shouldGenerateFinnJournalposterSql() {
		FinnJournalposterRequestTo finnJournalposterRequestTo = new FinnJournalposterRequestTo();
		finnJournalposterRequestTo.setFraDato("0000-01-01");
		finnJournalposterRequestTo.setFoerste(1);
		finnJournalposterRequestTo.setInkluderJournalStatus(Collections.singletonList(JournalStatusCode.J));
		finnJournalposterRequestTo.setInkluderJournalpostType(Collections.singletonList(JournalpostTypeCode.I));
		finnJournalposterRequestTo.setPsakSakIds(Arrays.asList("P1", "P2"));
		finnJournalposterRequestTo.setGsakSakIds(Collections.singletonList("G1"));
		finnJournalposterRequestTo.setAlleIdenter(Collections.singletonList("***gammelt_fnr***"));
		JournalpostFilter journalpostFilter = new JournalpostFilter(finnJournalposterRequestTo);
		String sql = FinnJournalpostSqlGenerator.finnJournalposterSql(journalpostFilter, Arrays.asList("gsaker", "psaker"), "");
		assertThat(sql, is(
				"WITH psaksaker AS\n" +
						"       (SELECT s.journalpost_id\n" +
						"        FROM t_saksrelasjon s\n" +
						"        WHERE (s.k_fagsystem = 'PEN' AND s.sak_nr_fk IN (:psakIds))\n" +
						"          AND (s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))\n" +
						"       ),\n" +
						"     midlertidige AS (SELECT b.journalpost_id\n" +
						"                      FROM t_bruker b\n" +
						"                             JOIN t_journalpost tj ON b.journalpost_id = tj.journalpost_id\n" +
						"                      WHERE b.bruker_id IN (:alleIdenter)\n" +
						"                        AND tj.k_journal_s IN ('M', 'MO', 'D')\n" +
						"     ),\n" +
						"     relevantedata AS (SELECT j.journalpost_id      AS journalpostid,\n" +
						"                              j.innhold             AS innhold,\n" +
						"                              j.k_fagomrade         AS fagomrade,\n" +
						"                              j.k_behandlingstema   AS behandlingstema,\n" +
						"                              bt.dekode             AS behandlingstemanavn,\n" +
						"                              j.k_journal_s         AS journalstatus,\n" +
						"                              j.avsend_mottak_id    AS avsendermottakerid,\n" +
						"                              j.k_avsend_mottak_id_t    AS avsendermottakeridtype,\n" +
						"                              j.avsend_mottaker     AS avsendermottakernavn,\n" +
						"                              j.land                AS avsendermottakerland,\n" +
						"                              j.journalf_enhet      AS journalforendeenhet,\n" +
						"                              j.journalfort_av_navn AS journalfortavnavn,\n" +
						"                              j.opprettet_av_navn   AS opprettetavnavn,\n" +
						"                              j.k_mottaks_kanal     AS mottakskanal,\n" +
						"                              j.k_utsendings_kanal  AS utsendingskanal,\n" +
						"                              j.k_journalpost_t     AS journalposttype,\n" +
						"                              j.dato_opprettet      AS datoopprettet,\n" +
						"                              j.dato_ekspedert      AS ekspedertdato,\n" +
						"                              j.dato_mottatt        AS mottattdato,\n" +
						"                              j.dato_journal        AS journaldato,\n" +
						"                              j.dato_dokument       AS dokumentdato,\n" +
						"                              j.dato_avs_retur      AS avsreturdato,\n" +
						"                              j.dato_sendt_print    AS sendtprintdato,\n" +
						"                              j.k_skjerming_type    AS skjerming,\n" +
						"                              s.sak_nr_fk           AS saksrelasjon_sakid,\n" +
						"                              s.feilregistrert      AS saksrelasjon_feilregistrert,\n" +
						"                              s.k_fagsystem         AS saksrelasjon_fagsystem,\n" +
						"                              b.bruker_id           AS bruker_brukerid,\n" +
						"                              b.k_bruker_t          AS bruker_brukeridtype,\n" +
						"                              t.nokkel              AS tilleggsopplysninger_nokkel,\n" +
						"                              t.verdi               AS tilleggsopplysninger_verdi,\n" +
						"                              d.dokument_info_id    AS dokumenter_dokumentinfoid,\n" +
						"                              rel.k_tilkn_jp_som    AS dokumenter_tilknyttetsom,\n" +
						"                              d.k_dokument_s        AS dokumenter_dokumentstatus,\n" +
						"                              d.dato_dok_ferdig     AS dokumenter_datoferdigstilt,\n" +
						"                              d.brev_kode           AS dokumenter_brevkode,\n" +
						"                              d.dokumenttype_id     AS dokumenter_dokumenttypeid,\n" +
						"                              d.tittel              AS dokumenter_tittel,\n" +
						"							   d.kassert             AS dokumenter_kassert,\n" +
						"                              rel.k_skjerming_type  AS dokumenter_skjerming,\n" +
						"                              d.orig_journalpost_id AS dokumenter_origjournalpostid,\n" +
						"                              fd.k_skjerming_type   AS dokumenter_varianter_skjerming,\n" +
						"                              fd.k_variant_format   AS dokumenter_varianter_variantf,\n" +
						"	                           fd.fil_navn           AS dokumenter_varianter_filnavn,\n" +
						"	                           fd.fil_uuid           AS dokumenter_varianter_filuuid,\n" +
						"                              tsi.skannet_innhold_id AS dokumenter_logiske_vedleggid,\n" +
						"                              tsi.vedlegg_innhold   AS dokumenter_logiske_tittel\n" +
						"                       FROM t_journalpost j\n" +
						"                              LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
						"                              LEFT JOIN t_jp_tillegg t ON j.journalpost_id = t.journalpost_id\n" +
						"                              LEFT JOIN t_k_behandlingstema bt ON j.k_behandlingstema = bt.k_behandlingstema\n" +
						"                              LEFT JOIN t_bruker b ON j.journalpost_id = b.journalpost_id\n" +
						"                              JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
						"                              JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
						"                              LEFT JOIN t_fil_detaljer fd ON d.dokument_info_id = fd.dokument_info_id AND fd.k_variant_format IN ('ARKIV', 'SLADDET', 'PRODUKSJON', 'PRODUKSJON_DLF', 'FULLVERSJON', 'ORIGINAL')\n" +
						"                              LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id" +
						"     )\n" +
						"SELECT r.*,\n" +
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
						"                       SELECT j.journalpost_id,\n" +
						"                              LEAD(j.journalpost_id) OVER (ORDER BY j.journalpost_id) AS prevjournalpostid,\n" +
						"                              LAG(j.journalpost_id) OVER (ORDER BY j.journalpost_id)  AS nextjournalpostid,\n" +
						"                              COUNT(*) OVER ()  AS totaltAntall\n" +
						"                       FROM (SELECT journalpost_id FROM gsaker UNION ALL SELECT journalpost_id FROM psaker) jps\n" +
						"                              JOIN t_journalpost j ON jps.journalpost_id = j.journalpost_id\n" +
						"                              LEFT JOIN t_saksrelasjon ts ON j.journalpost_id = ts.journalpost_id\n" +
						"\n" +
						"                       WHERE j.k_journalpost_t IN (:inkluderJournalpostType)\n" +
						"                         AND j.dato_opprettet > :fraDato\n" +
						"                         AND (\n" +
						"                           (ts.feilregistrert = 1 AND\n" +
						"                            j.k_journal_s IN (:allJournalStatus))\n" +
						"                           OR (ts.feilregistrert IS NULL AND\n" +
						"                               j.k_journal_s IN (:inkluderJournalStatus))\n" +
						"                           OR (ts.feilregistrert = 0 AND\n" +
						"                               j.k_journal_s IN (:inkluderJournalStatus))\n" +
						"                         )\n" +
						"                     ) p\n" +
						"                WHERE p.journalpost_id < :journalpostIdPeker ORDER BY p.journalpost_id DESC              ) t\n" +
						"         WHERE rownum <= :antallRader\n" +
						"       ) journalposter ON journalposter.journalpost_id = r.journalpostid\n" +
						"ORDER BY journalpostid DESC, dokumenter_tilknyttetsom ASC"

		));
	}
}