package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark900.FinnJournalpostSqlGenerator.feilregistrertSelectionSql;

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
public class FinnJournalposterSpringJdbcRepository {
	private static final ResultSetExtractor<List<JournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "dokumenter_dokumentinfoid", "dokumenter_logiske_tittel")
			.newResultSetExtractor(JournalpostDto.class);
	private static final List<String> NOT_USED = Collections.singletonList("notused");
	private static final List<String> ALL_JOURNALSTATUS = Stream.of(JournalStatusCode.values()).map(Enum::name).collect(Collectors.toList());
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Collections.singletonList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	private static final String GSAK_IDS_PARAM = "gsakIds";
	private static final String PSAK_IDS_PARAM = "psakIds";

	private final GsakCteMapper gsakCteMapper;
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	public FinnJournalposterSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.gsakCteMapper = new GsakCteMapper();
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<JournalpostDto> hentJournalposter(final List<String> gsakIds,
												  final List<String> psakIds,
												  JournalpostFilter journalpostFilter) {
		List<String> cteAliases = new ArrayList<>();
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		GsakCteMapper.GsakCte gsakCte = gsakCteMapper.mapCte(gsakIds, journalpostFilter.isKunFeilregistrerte());
		namedParams.addValues(gsakCte.getGsakIdParams());
		if (gsakCte.isGsakerExists()) {
			cteAliases.add("gsaksaker");
		}
		if (psakIds == null || psakIds.isEmpty()) {
			namedParams.addValue(PSAK_IDS_PARAM, NOT_USED);
		} else {
			namedParams.addValue(PSAK_IDS_PARAM, psakIds);
			cteAliases.add("psaksaker");
		}
		if (journalpostFilter.isInkluderMidlertidigeJournalposter()) {
			namedParams.addValue("alleIdenter", journalpostFilter.getAlleIdenter());
			cteAliases.add("midlertidige");
		} else {
			namedParams.addValue("alleIdenter", NOT_USED);
		}
		if (journalpostFilter.isKunFeilregistrerte()) {
			namedParams.addValue("inkluderJournalStatus", NOT_USED);
		} else {
			namedParams.addValue("inkluderJournalStatus", journalpostFilter.getInkluderJournalStatus());
		}

		namedParams.addValue("fraDato", Timestamp.valueOf(journalpostFilter.getFraDato().atStartOfDay()));
		namedParams.addValue("inkluderTema", journalpostFilter.getInkluderTema());
		namedParams.addValue("inkluderJournalpostType", journalpostFilter.getInkluderJournalpostType());
		namedParams.addValue("allJournalStatus", ALL_JOURNALSTATUS);
		namedParams.addValue("visFeilregistrert", journalpostFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST);
		namedParams.addValue("antallRader", journalpostFilter.getAntallRader());
		namedParams.addValue("journalpostIdPeker", journalpostFilter.getJournalpostIdPeker());

		if (cteAliases.isEmpty()) {
			return new ArrayList<>();
		}

		return jdbcTemplate.query(finnJournalposterSql(journalpostFilter, cteAliases, gsakCte.getCteSql()), namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);
	}

	private String finnJournalposterSql(JournalpostFilter journalpostFilter, List<String> cteAliases, String gsakCte) {
		return "WITH psaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'PEN' AND s.sak_nr_fk IN (:psakIds))\n" +
				"          AND " + feilregistrertSelectionSql(journalpostFilter.isKunFeilregistrerte()) + "\n" +
				"       ),\n" +
				gsakCte +
				"     midlertidige AS (SELECT b.journalpost_id\n" +
				"                      FROM t_bruker b\n" +
				"                             JOIN t_journalpost tj ON b.journalpost_id = tj.journalpost_id\n" +
				"                      WHERE b.bruker_id IN (:alleIdenter)\n" +
				"                        AND tj.k_journal_s IN ('M', 'MO')\n" +
				"     ),\n" +
				"     relevantedata AS (SELECT j.journalpost_id      AS journalpostid,\n" +
				"                              j.journalf_enhet      AS journalforendeenhetid,\n" +
				"                              j.innhold             AS innhold,\n" +
				"                              j.k_fagomrade         AS fagomrade,\n" +
				"                              j.k_journal_s         AS journalstatus,\n" +
				"                              j.avsend_mottaker     AS avsendermottakernavn,\n" +
				"                              j.journalfort_av_navn AS journalfortavnavn,\n" +
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
				"                              s.sak_nr_fk           AS saksrelasjon_sakid,\n" +
				"                              s.feilregistrert      AS saksrelasjon_feilregistrert,\n" +
				"                              s.k_fagsystem         AS saksrelasjon_fagsystem,\n" +
				"                              d.dokument_info_id    AS dokumenter_dokumentinfoid,\n" +
				"                              rel.k_tilkn_jp_som    AS dokumenter_tilknyttetsom,\n" +
				"                              d.k_dokument_s        AS dokumenter_dokumentstatus,\n" +
				"                              d.brev_kode           AS dokumenter_brevkode,\n" +
				"                              d.tittel              AS dokumenter_tittel,\n" +
				"                              tsi.vedlegg_innhold   AS dokumenter_logiske_tittel\n" +
				"                       FROM t_journalpost j\n" +
				"                              LEFT JOIN t_saksrelasjon s ON s.journalpost_id = j.journalpost_id\n" +
				"                              JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"                              JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"                              LEFT JOIN t_skannet_innhold tsi ON d.dokument_info_id = tsi.dokument_info_id)\n" +
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
				"                       FROM (" + generateCteUnionSql(cteAliases) + ") jps\n" +
				"                              JOIN t_journalpost j ON jps.journalpost_id = j.journalpost_id\n" +
				"                              LEFT JOIN t_saksrelasjon ts ON j.journalpost_id = ts.journalpost_id\n" +
				"\n" +
				"                       WHERE j.k_fagomrade IN (:inkluderTema)\n" +
				"                         AND j.k_journalpost_t IN (:inkluderJournalpostType)\n" +
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
				"                WHERE " + paginateSql(journalpostFilter.getSlice()) +
				"              ) t\n" +
				"         WHERE rownum <= :antallRader\n" +
				"       ) journalposter ON journalposter.journalpost_id = r.journalpostid\n" +
				"ORDER BY journalpostid DESC, dokumenter_tilknyttetsom ASC";
	}

	private String paginateSql(JournalpostFilter.Slice slice) {
		switch (slice) {
			case FOERSTE:
				return "p.journalpost_id < :journalpostIdPeker " +
						"ORDER BY p.journalpost_id DESC";
			case SISTE:
				return "p.journalpost_id > :journalpostIdPeker " +
						"ORDER BY p.journalpost_id ASC";
			default:
				return "";
		}
	}

	private String generateCteUnionSql(List<String> cteAliases) {
		return cteAliases.stream().map(cteAlias -> "SELECT journalpost_id FROM " + cteAlias).collect(Collectors.joining(" UNION ALL "));
	}
}
