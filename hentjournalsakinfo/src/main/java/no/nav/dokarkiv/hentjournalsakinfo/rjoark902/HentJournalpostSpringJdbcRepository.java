package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import static no.nav.dokarkiv.hentjournalsakinfo.rjoark902.HentJournalpostSqlGenerator.hentJournalpostSql;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import org.simpleflatmapper.jdbc.spring.JdbcTemplateMapperFactory;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;

@Slf4j
@Repository
class HentJournalpostSpringJdbcRepository {
	private static final ResultSetExtractor<List<HentJournalpostDto>> JOURNALPOST_DTO_RESULT_SET_EXTRACTOR = JdbcTemplateMapperFactory.newInstance()
			.addKeys("journalpostid", "saksrelasjon_sakid", "dokumenter_dokumentinfoid", "dokumenter_logiske_tittel", "dokumenter_varianter_variantf")
			.newResultSetExtractor(HentJournalpostDto.class);

	private final NamedParameterJdbcTemplate jdbcTemplate;

	@Inject
	public HentJournalpostSpringJdbcRepository(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	HentJournalpostDto finnJournalposter(String journalpostId) {
		MapSqlParameterSource namedParams = buildNamedParams(journalpostId);
		String hentJournalpostSql = hentJournalpostSql();

		try {
			List<HentJournalpostDto> journalpostDtoList = jdbcTemplate.query(hentJournalpostSql, namedParams, JOURNALPOST_DTO_RESULT_SET_EXTRACTOR);

			if (journalpostDtoList == null || journalpostDtoList.isEmpty()) {
				throw new JournalpostIkkeFunnetException("JournalpostDtoList er tom eller null");
			} else {
				return journalpostDtoList.get(0);
			}

		} catch (Exception e) {
			log.warn("Journalpost med journalpostId={} ikke funnet.", journalpostId);
			throw new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet.", e);
		}
	}

	private MapSqlParameterSource buildNamedParams(String journalpostId) {
		MapSqlParameterSource namedParams = new MapSqlParameterSource();
		namedParams.addValue("journalpostId", journalpostId);
		return namedParams;
	}
}
