package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import no.nav.dokarkiv.hentjournalsakinfo.JournalpostFilter;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark904.FinnJournalposterStatusSql.finnJournalposterStatusSql;

@Repository
class FinnJournalposterStatusJdbcRepository {
	private static final ResultSetExtractor<List<JournalpostDto>> RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "tilleggsopplysninger_nokkel", "dokumenter_dokumentinfoid", "dokumenter_logiske_vedleggid", "dokumenter_varianter_variantf")
			.newResultSetExtractor(JournalpostDto.class);
	private final NamedParameterJdbcTemplate jdbcTemplate;

	FinnJournalposterStatusJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	List<JournalpostDto> finnJournalposterStatus(JournalpostFilter journalpostFilter) {
		MapSqlParameterSource namedParams = buildNamedParams(journalpostFilter);
		String finnJournalposterStatusSql = finnJournalposterStatusSql();
		return jdbcTemplate.query(finnJournalposterStatusSql, namedParams, RESULT_SET_EXTRACTOR);
	}

	private MapSqlParameterSource buildNamedParams(JournalpostFilter journalpostFilter) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("inkluderJournalStatus", journalpostFilter.getInkluderJournalStatus());
		namedParams.addValue("fraDato", Timestamp.valueOf(journalpostFilter.getFraDato().atStartOfDay()));
		namedParams.addValue("inkluderJournalpostType", journalpostFilter.getInkluderJournalpostType());
		namedParams.addValue("antallRader", journalpostFilter.getAntallRader());
		namedParams.addValue("journalpostIdPeker", journalpostFilter.getJournalpostIdPeker());
		return namedParams;
	}
}
