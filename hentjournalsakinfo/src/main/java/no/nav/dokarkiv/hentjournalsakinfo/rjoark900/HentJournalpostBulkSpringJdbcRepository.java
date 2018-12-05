package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class HentJournalpostBulkSpringJdbcRepository {
	private static final ResultSetExtractor<List<JournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "dokumenter_dokumentinfoid")
			.newResultSetExtractor(JournalpostDto.class);
	private static final List<String> NOT_USED = Collections.singletonList("notused");
	private static final List<String> ALL_JOURNALSTATUS = Stream.of(JournalStatusCode.values()).map(Enum::name).collect(Collectors.toList());
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Collections.singletonList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	public static final String GSAK_IDS_PARAM = "gsakIds";
	public static final String PSAK_IDS_PARAM = "psakIds";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	public HentJournalpostBulkSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<JournalpostDto> hentJournalposter(final List<String> gsakIds,
												  final List<String> psakIds,
												  BulkJournalposterFilter bulkJournalposterFilter) {
		List<String> cteAliases = new ArrayList<>();
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		if (gsakIds == null || gsakIds.isEmpty()) {
			namedParams.addValue(GSAK_IDS_PARAM, NOT_USED);
		} else {
			namedParams.addValue(GSAK_IDS_PARAM, gsakIds);
			cteAliases.add("gsaksaker");
		}
		if (gsakIds == null || psakIds.isEmpty()) {
			namedParams.addValue(PSAK_IDS_PARAM, NOT_USED);
		} else {
			namedParams.addValue(PSAK_IDS_PARAM, psakIds);
			cteAliases.add("psaksaker");
		}
		if (bulkJournalposterFilter.isInkluderMidlertidigeJournalposter()) {
			cteAliases.add("midlertidige");
		}
		if (bulkJournalposterFilter.isKunFeilregistrerte()) {
			namedParams.addValue("inkluderJournalStatus", NOT_USED);
		} else {
			namedParams.addValue("inkluderJournalStatus", bulkJournalposterFilter.getInkluderJournalStatus());
		}

		namedParams.addValue("alleIdenter", bulkJournalposterFilter.getAlleIdenter());
		namedParams.addValue("fraDato", Timestamp.valueOf(bulkJournalposterFilter.getFraDato().atStartOfDay()));
		namedParams.addValue("inkluderTema", bulkJournalposterFilter.getInkluderTema());
		namedParams.addValue("inkluderJournalpostType", bulkJournalposterFilter.getInkluderJournalpostType());
		namedParams.addValue("allJournalStatus", ALL_JOURNALSTATUS);
		namedParams.addValue("visFeilregistrert", bulkJournalposterFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST);
		namedParams.addValue("antallRader", bulkJournalposterFilter.getAntallRader());
		namedParams.addValue("journalpostIdPeker", bulkJournalposterFilter.getJournalpostIdPeker());

		if (cteAliases.isEmpty()) {
			return new ArrayList<>();
		}

		return jdbcTemplate.query(journalpostbulkSql(bulkJournalposterFilter, cteAliases), namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);
	}

	private String journalpostbulkSql(BulkJournalposterFilter bulkJournalposterFilter, List<String> cteAliases) {
		return "WITH psaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'PEN' AND s.sak_nr_fk IN (:psakIds))\n" +
				"          AND " + generateFeilregistrertSelectionSql(bulkJournalposterFilter) + "\n" +
				"       ),\n" +
				"     gsaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'FS22' AND s.sak_nr_fk IN (:gsakIds))\n" +
				"          AND " + generateFeilregistrertSelectionSql(bulkJournalposterFilter) + "\n" +
				"       ),\n" +
				"     midlertidige AS (SELECT b.journalpost_id\n" +
				"                      FROM t_bruker b\n" +
				"                             JOIN t_journalpost tj ON b.journalpost_id = tj.journalpost_id\n" +
				"                      WHERE b.bruker_id IN (:alleIdenter)\n" +
				"                        AND tj.k_journal_s IN ('M', 'MO')\n" +
				"     ),\n" +
				"     fellesprojeksjon AS (SELECT j.journalpost_id      AS journalpostid,\n" +
				"                                 j.journalf_enhet      AS journalforendeenhetid,\n" +
				"                                 j.innhold             AS innhold,\n" +
				"                                 j.k_fagomrade         AS fagomrade,\n" +
				"                                 j.k_journal_s         AS journalstatus,\n" +
				"                                 j.avsend_mottaker     AS avsendermottakernavn,\n" +
				"                                 j.journalfort_av_navn AS journalfortavnavn,\n" +
				"                                 j.k_mottaks_kanal     AS mottakskanal,\n" +
				"                                 j.k_utsendings_kanal  AS utsendingskanal,\n" +
				"                                 j.k_journalpost_t     AS journalposttype,\n" +
				"                                 j.dato_opprettet      AS datoopprettet,\n" +
				"                                 j.dato_ekspedert      AS ekspedertdato,\n" +
				"                                 j.dato_mottatt        AS mottattdato,\n" +
				"                                 j.dato_journal        AS journaldato,\n" +
				"                                 j.dato_sendt_print    AS sendtprintdato,\n" +
				"                                 s.sak_nr_fk           AS saksrelasjon_sakid,\n" +
				"                                 s.feilregistrert      AS saksrelasjon_feilregistrert,\n" +
				"                                 s.k_fagsystem         AS saksrelasjon_fagsystem,\n" +
				"                                 rel.k_tilkn_jp_som    AS tilknyttet_som,\n" +
				"                                 d.dokument_info_id    AS dokumenter_dokumentinfoid,\n" +
				"                                 d.k_dokument_s        AS dokumenter_dokumentstatus,\n" +
				"                                 d.brev_kode           AS dokumenter_brevkode,\n" +
				"                                 d.tittel              AS dokumenter_tittel\n" +
				"                          FROM t_saksrelasjon s\n" +
				"                                 JOIN t_journalpost j ON s.journalpost_id = j.journalpost_id\n" +
				"                                 JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"                                 JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id)\n" +
				"SELECT t.journalpostid,\n" +
				"       t.journalforendeenhetid,\n" +
				"       t.innhold,\n" +
				"       t.fagomrade,\n" +
				"       t.journalstatus,\n" +
				"       t.avsendermottakernavn,\n" +
				"       t.journalfortavnavn,\n" +
				"       t.mottakskanal,\n" +
				"       t.utsendingskanal,\n" +
				"       t.journalposttype,\n" +
				"       t.datoopprettet,\n" +
				"       t.ekspedertdato,\n" +
				"       t.mottattdato,\n" +
				"       t.journaldato,\n" +
				"       t.sendtprintdato,\n" +
				"       t.saksrelasjon_sakid,\n" +
				"       t.saksrelasjon_feilregistrert,\n" +
				"       t.saksrelasjon_fagsystem,\n" +
				"       t.dokumenter_dokumentinfoid,\n" +
				"       t.dokumenter_dokumentstatus,\n" +
				"       t.dokumenter_brevkode,\n" +
				"       t.dokumenter_tittel " +
				"FROM (\n" +
				"       SELECT *\n" +
				"       FROM fellesprojeksjon fpj\n" +
				"       WHERE fpj.journalpostid IN (" + generateCteUnionSql(cteAliases) + ")\n" +
				"         AND fpj.fagomrade IN (:inkluderTema)\n" +
				"         AND fpj.journalposttype IN (:inkluderJournalpostType)\n" +
				"         AND fpj.datoopprettet > :fraDato\n" +
				"         AND (\n" +
				"           (\n" +
				"               fpj.saksrelasjon_feilregistrert = 1 AND\n" +
				"               fpj.journalstatus IN (:allJournalStatus))\n" +
				"           OR (fpj.saksrelasjon_feilregistrert IS NULL AND fpj.journalstatus IN (:inkluderJournalStatus))\n" +
				"           OR (fpj.saksrelasjon_feilregistrert = 0 AND fpj.journalstatus IN (:inkluderJournalStatus))\n" +
				"         )\n" +
				"       ORDER BY fpj.journalpostid DESC, tilknyttet_som ASC\n" +
				"     ) t\n" +
				"WHERE t.journalpostid < :journalpostIdPeker\n" +
				"  AND rownum <= :antallRader\n";
	}

	private String generateCteUnionSql(List<String> cteAliases) {
		return cteAliases.stream().map(cteAlias -> "SELECT journalpost_id FROM " + cteAlias).collect(Collectors.joining(" UNION ALL "));
	}

	private String generateFeilregistrertSelectionSql(BulkJournalposterFilter bulkJournalposterFilter) {
		if (bulkJournalposterFilter.isKunFeilregistrerte()) {
			return "(s.feilregistrert = 1)";
		} else {
			return "(s.feilregistrert IS NULL OR (s.feilregistrert IN (:visFeilregistrert)))";
		}
	}
}
