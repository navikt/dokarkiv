package no.nav.dokarkiv.core.consumers.saf;

import com.google.gson.GsonBuilder;
import no.nav.dokarkiv.core.consumers.saf.exceptions.saf.SafJournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.consumers.saf.graphql.GraphQLRequest;
import no.nav.dokarkiv.core.consumers.saf.graphql.SafGraphqlConsumer;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJsonJournalpost;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.cache.annotation.Cacheable;


import static java.util.Collections.singletonMap;
import static no.nav.dokarkiv.core.cache.CacheConfig.SAF_JOURNALPOST_QUERY_CACHE;

@Component
public class SafJournalpostQueryService {

	private static final String JOURNALPOST_QUERY = """
			query journalpost($queryJournalpostId: String!) {
				  journalpost(journalpostId: $queryJournalpostId) {
					dokumenter {
					  dokumentInfoId
					  dokumentvarianter {
						saksbehandlerHarTilgang
						variantformat
					  }
					}
				  }
				}
				""";
	private final SafGraphqlConsumer safGraphqlConsumer;

	public SafJournalpostQueryService(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	@Cacheable(value = SAF_JOURNALPOST_QUERY_CACHE)
	public SafJournalpostTo hentJournalpost(long journalpostId) {

		ResponseEntity<String> response = safGraphqlConsumer.performQuery(GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(singletonMap("queryJournalpostId", String.valueOf(journalpostId)))
				.build());

		return convertJsonToSafJsonJournalpost(response.getBody(), journalpostId).getJournalpost();
	}

	private SafJsonJournalpost convertJsonToSafJsonJournalpost(String jsonBody, long journalpostId) {
			SafJsonJournalpost journalpost = new GsonBuilder().create().fromJson(jsonBody, SafJsonJournalpost.class);
			if (journalpost == null || journalpost.getData() == null || journalpost.getJournalpost() == null) {
				throw new SafJournalpostIkkeFunnetException(String.format("Ingen journalpost ble funnet for journalpostId=%s", journalpostId));
			}
		return journalpost;
	}
}