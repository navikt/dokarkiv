package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark910.DokumentoversiktBrukerSqlGenerator.dokumentoversiktBrukerSql;

@Repository
class DokumentoversiktBrukerSpringJdbcRepository {
	private static final ResultSetExtractor<List<JournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "tilleggsopplysninger_nokkel", "dokumenter_dokumentinfoid", "dokumenter_logiske_vedleggid", "dokumenter_varianter_variantf")
			.newResultSetExtractor(JournalpostDto.class);
	private static final List<String> NOT_USED = Collections.singletonList("notused");
	private static final List<String> ALL_JOURNALSTATUS = Stream.of(JournalStatusCode.values()).map(Enum::name).collect(Collectors.toList());
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Collections.singletonList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	private static final String PSAK_IDS_PARAM = "psakIds";
	static final String CTE_ALIAS_AKTOERID = "journalpostid_aktoerId";
	static final String CTE_ALIAS_ORGNR = "journalpostid_orgnr";
	static final String CTE_ALIAS_PSAKSAKER = "journalpostid_psak";
	static final String CTE_ALIAS_MIDLERTIDIGE = "journalpostid_midlertidige";
	private static final String AKTOERID_PARAM = "aktoerId";
	private static final String ORGNR_PARAM = "orgnr";
	private static final String ALLE_IDENTER_PARAM = "alleIdenter";
	private static final String INKLUDER_JOURNAL_STATUS_PARAM = "inkluderJournalStatus";
	private static final String INKLUDER_JOURNALPOST_TYPE_PARAM = "inkluderJournalpostType";
	private static final String FRA_DATO_PARAM = "fraDato";
	private static final String ALL_JOURNAL_STATUS_PARAM = "allJournalStatus";
	private static final String VIS_FEILREGISTRERT_PARAM = "visFeilregistrert";
	private static final String ANTALL_RADER_PARAM = "antallRader";
	private static final String JOURNALPOST_ID_PEKER_PARAM = "journalpostIdPeker";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public DokumentoversiktBrukerSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	List<JournalpostDto> hentDokumentoversiktBruker(final DokumentoversiktBrukerFilter dokumentoversiktBrukerFilter) {
		List<String> cteAliases = buildCteAliases(dokumentoversiktBrukerFilter);
		if (cteAliases.isEmpty()) {
			return new ArrayList<>();
		}
		MapSqlParameterSource namedParams = buildNamedParams(dokumentoversiktBrukerFilter);
		String dokumentoversiktBrukerSql = dokumentoversiktBrukerSql(dokumentoversiktBrukerFilter, cteAliases);
		return jdbcTemplate.query(dokumentoversiktBrukerSql, namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);
	}

	private MapSqlParameterSource buildNamedParams(final DokumentoversiktBrukerFilter dokumentoversiktBrukerFilter) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		if(dokumentoversiktBrukerFilter.isBrukerPerson()) {
			namedParams.addValue(AKTOERID_PARAM, dokumentoversiktBrukerFilter.getAktoerId());
			namedParams.addValue(ORGNR_PARAM, NOT_USED);
		} else if(dokumentoversiktBrukerFilter.isBrukerOrganisasjon()) {
			namedParams.addValue(ORGNR_PARAM, dokumentoversiktBrukerFilter.getOrgnr());
			namedParams.addValue(AKTOERID_PARAM, NOT_USED);
		} else {
			throw new InvalidDokumentoversiktBrukerRequestException("Kan ikke legge til CTE for person eller organisasjon. Mangler aktørId eller orgnr i request.");
		}
		if (dokumentoversiktBrukerFilter.containsPsakSaker()) {
			namedParams.addValue(PSAK_IDS_PARAM, dokumentoversiktBrukerFilter.getPsakIds());
		} else {
			namedParams.addValue(PSAK_IDS_PARAM, NOT_USED);
		}
		if (dokumentoversiktBrukerFilter.isInkluderMidlertidigeJournalposter()) {
			namedParams.addValue(ALLE_IDENTER_PARAM, dokumentoversiktBrukerFilter.getAlleIdenter());
		} else {
			namedParams.addValue(ALLE_IDENTER_PARAM, NOT_USED);
		}
		if (dokumentoversiktBrukerFilter.isKunFeilregistrerte()) {
			namedParams.addValue(INKLUDER_JOURNAL_STATUS_PARAM, NOT_USED);
		} else {
			namedParams.addValue(INKLUDER_JOURNAL_STATUS_PARAM, dokumentoversiktBrukerFilter.getInkluderJournalStatus());
		}

		namedParams.addValue(FRA_DATO_PARAM, Timestamp.valueOf(dokumentoversiktBrukerFilter.getFraDato().atStartOfDay()));
		namedParams.addValue(INKLUDER_JOURNALPOST_TYPE_PARAM, dokumentoversiktBrukerFilter.getInkluderJournalpostType());
		namedParams.addValue(ALL_JOURNAL_STATUS_PARAM, ALL_JOURNALSTATUS);
		namedParams.addValue(VIS_FEILREGISTRERT_PARAM, dokumentoversiktBrukerFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST);
		namedParams.addValue(ANTALL_RADER_PARAM, dokumentoversiktBrukerFilter.getAntallRader());
		namedParams.addValue(JOURNALPOST_ID_PEKER_PARAM, dokumentoversiktBrukerFilter.getJournalpostIdPeker());
		return namedParams;
	}

	private List<String> buildCteAliases(final DokumentoversiktBrukerFilter dokumentoversiktBrukerFilter) {
		List<String> cteAliases = new ArrayList<>();

		if(dokumentoversiktBrukerFilter.isBrukerPerson()) {
			cteAliases.add(CTE_ALIAS_AKTOERID);
		} else if(dokumentoversiktBrukerFilter.isBrukerOrganisasjon()) {
			cteAliases.add(CTE_ALIAS_ORGNR);
		} else {
			throw new InvalidDokumentoversiktBrukerRequestException("Kan ikke legge til CTE for person eller organisasjon. Mangler aktørId eller orgnr i request.");
		}
		if (dokumentoversiktBrukerFilter.containsPsakSaker()) {
			cteAliases.add(CTE_ALIAS_PSAKSAKER);
		}
		if (dokumentoversiktBrukerFilter.isInkluderMidlertidigeJournalposter()) {
			cteAliases.add(CTE_ALIAS_MIDLERTIDIGE);
		}
		return cteAliases;
	}
}
