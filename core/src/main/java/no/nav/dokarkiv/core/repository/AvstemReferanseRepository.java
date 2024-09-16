package no.nav.dokarkiv.core.repository;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class AvstemReferanseRepository {
	private static final int MAX_PARTITION_SIZE_IN_CLAUSE = 1000;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	public AvstemReferanseRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
	}

	public Set<String> findKanalReferanseIdsNotMatchedInDB(List<String> kanalReferanseIds) {
		List<String> results = new ArrayList<>();
		for (int i = 0; i < kanalReferanseIds.size(); i += MAX_PARTITION_SIZE_IN_CLAUSE) {
			results.addAll(findKanalReferanseIdsNotMatchedInDBPart(
					kanalReferanseIds.subList(i, Math.min(i + MAX_PARTITION_SIZE_IN_CLAUSE, kanalReferanseIds.size()))
			));
		}
		return new HashSet<>(results);
	}

	public List<String> findKanalReferanseIdsNotMatchedInDBPart(List<String> kanalReferanseIdsSegment) {
		return (namedParameterJdbcTemplate.query("SELECT kanal_referanse_id " +
												 "FROM t_journalpost jp " +
												 "WHERE jp.kanal_referanse_id in (:kanalReferanseIdsSegment)",
				Map.of("kanalReferanseIdsSegment", kanalReferanseIdsSegment),
				(rs, i) -> rs.getString(1)));
	}
}
