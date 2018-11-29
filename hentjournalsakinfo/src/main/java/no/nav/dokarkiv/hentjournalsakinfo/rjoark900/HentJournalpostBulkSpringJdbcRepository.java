package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class HentJournalpostBulkSpringJdbcRepository {
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Arrays.asList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);

	private static final ResultSetExtractor<List<JournalpostDto>> resultSetExtractor = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostId")
			.newResultSetExtractor(JournalpostDto.class);
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	public HentJournalpostBulkSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@SuppressWarnings("unchecked")
	public List<JournalpostDto> hentJournalposter(final List<String> gsakIds,
												  final List<String> psakIds,
												  BulkJournalposterFilter bulkJournalposterFilter) {

//		MapSqlParameterSource namedParams = new MapSqlParameterSource();
//		namedParams.addValue("","");
		return jdbcTemplate.query("WITH pensjonssaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'PEN' AND s.sak_nr_fk IN ('14893692', '21821225', '22891594'))\n" +
				"          AND (s.feilregistrert IS NULL OR s.feilregistrert = 0)\n" +
				"       ),\n" +
				"     gsaksaker AS\n" +
				"       (SELECT s.journalpost_id\n" +
				"        FROM t_saksrelasjon s\n" +
				"        WHERE (s.k_fagsystem = 'FS22' AND s.sak_nr_fk IN ('120475919',\n" +
				"                                                          '129657551',\n" +
				"                                                          '129664717',\n" +
				"                                                          '129665871',\n" +
				"                                                          '129680208',\n" +
				"                                                          '131492323',\n" +
				"                                                          '135082383'))\n" +
				"          AND (s.feilregistrert IS NULL OR s.feilregistrert = 0)\n" +
				"       ),\n" +
				"     midlertidige AS (SELECT b.journalpost_id\n" +
				"                      FROM t_bruker b\n" +
				"                      WHERE b.bruker_id IN ('***gammelt_fnr***')\n" +
				"     )\n" +
				"SELECT j.journalpost_id      AS journalpostid,\n" +
				"       j.journalf_enhet      AS journalforendeenhetid,\n" +
				"       j.dato_journal        AS journaldato,\n" +
				"       j.dato_sendt_print    AS sendtprintdato,\n" +
				"       j.innhold             AS innhold,\n" +
				"       j.k_fagomrade         AS fagomrade,\n" +
				"       j.k_journal_s         AS journalstatus,\n" +
				"       j.dato_dokument       AS dokumentdato,\n" +
				"       j.avsend_mottaker     AS avsendermottakernavn,\n" +
				"       j.journalfort_av_navn AS journalfortavnavn,\n" +
				"       j.dato_mottatt        AS mottattdato,\n" +
				"       j.k_mottaks_kanal     AS mottakskanal,\n" +
				"       j.k_utsendings_kanal  AS utsendingskanal,\n" +
				"       j.dato_ekspedert      AS ekspedertdato,\n" +
				"       j.dato_lest           AS lestdato,\n" +
				"       j.mottatt_adressat    AS mottattadressatdato,\n" +
				"       j.k_journalpost_t     AS journalposttype,\n" +
				"       j.dato_opprettet      AS datoopprettet,\n" +
				"       s.sak_nr_fk           AS saksrelasjon_sakid,\n" +
				"       s.feilregistrert      AS saksrelasjon_feilregistrert,\n" +
				"       s.k_fagsystem         AS saksrelasjon_fagsystem,\n" +
				"       d.dokument_info_id    AS dokumenter_dokumentinfoid,\n" +
				"       d.k_dokument_s        AS dokumenter_dokumentstatus,\n" +
				"       d.brev_kode           AS dokumenter_brevkode,\n" +
				"       d.tittel              AS dokumenter_tittel\n" +
				"FROM t_saksrelasjon s\n" +
				"       JOIN t_journalpost j ON s.journalpost_id = j.journalpost_id\n" +
				"       JOIN t_jp_dok_info_rel rel ON j.journalpost_id = rel.journalpost_id\n" +
				"       JOIN t_dokument_info d ON rel.dokument_info_id = d.dokument_info_id\n" +
				"WHERE j.journalpost_id IN (SELECT journalpost_id\n" +
				"                           FROM pensjonssaker\n" +
				"                           UNION\n" +
				"                           SELECT journalpost_id\n" +
				"                           FROM gsaksaker\n" +
				"                           UNION\n" +
				"                           SELECT journalpost_id\n" +
				"                           FROM midlertidige)\n" +
				"  AND (s.feilregistrert IS NULL OR s.feilregistrert IN (1))\n" +
				"  AND j.k_fagomrade IN ('UFO', 'OPP', 'PEN', 'GEN', 'OMS', 'STO', 'AGR')\n" +
				"  AND j.k_journalpost_t IN ('I', 'U', 'N')\n" +
				"  AND j.k_journal_s IN ('J', 'U', 'D', 'R', 'FS', 'FL', 'E', 'A', 'UB', 'OD', 'M', 'MO')\n" +
				"  AND j.dato_opprettet > DATE '2017-11-10'\n" +
				"ORDER BY datoopprettet DESC", resultSetExtractor);
	}

//	private static String
}
