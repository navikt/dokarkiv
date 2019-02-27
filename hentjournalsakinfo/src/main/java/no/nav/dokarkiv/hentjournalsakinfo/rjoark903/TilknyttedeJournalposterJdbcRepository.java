package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterSql.GJENBRUKTE_JOURNALPOSTER_SQL;

import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
class TilknyttedeJournalposterJdbcRepository {
	private static final ResultSetExtractor<List<TilknyttetJournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "tilleggsopplysninger_nokkel", "dokumenter_dokumentinfoid", "dokumenter_logiske_tittel", "dokumenter_varianter_variantf")
			.newResultSetExtractor(TilknyttetJournalpostDto.class);
	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	TilknyttedeJournalposterJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	List<TilknyttetJournalpostDto> findGjenbrukteJournalposter(final Long dokumentInfoId) {
		MapSqlParameterSource namedParams = buildNamedParams(dokumentInfoId);
		return jdbcTemplate.query(GJENBRUKTE_JOURNALPOSTER_SQL, namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);
	}

	List<TilknyttetJournalpostDto> findSplittedeJournalposter(final Long dokumentInfoId) {
		return new ArrayList<>();
	}

	private MapSqlParameterSource buildNamedParams(final Long dokumentInfoId) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("dokumentInfoId", dokumentInfoId);
		return namedParams;
	}
}
