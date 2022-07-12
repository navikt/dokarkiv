package no.nav.dokarkiv.core.consumers.saf;

import no.nav.dokarkiv.core.consumers.saf.graphql.GraphQLRequest;
import no.nav.dokarkiv.core.consumers.saf.graphql.SafGraphqlConsumer;
import no.nav.dokarkiv.core.consumers.saf.journalpost.SafJournalpostTo;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class SafJournalpostQueryService {

	private static final String JOURNALPOST_QUERY =
			"query journalpost($queryJournalpostId: String!) {\n" +
					"  journalpost(journalpostId: $queryJournalpostId) {\n" +
					"    dokumenter {\n" +
					"      dokumentInfoId\n" +
					"      dokumentvarianter {\n" +
					"        saksbehandlerHarTilgang\n" +
					"        variantformat\n" +
					"      }\n" +
					"    }\n" +
					"  }\n" +
					"}\n";
	private final SafGraphqlConsumer safGraphqlConsumer;

	public SafJournalpostQueryService(SafGraphqlConsumer safGraphqlConsumer) {
		this.safGraphqlConsumer = safGraphqlConsumer;
	}

	public SafJournalpostTo hentJournalpost(String journalpostid, String authorizationHeader) {

		return safGraphqlConsumer.performQuery(GraphQLRequest.builder()
				.query(JOURNALPOST_QUERY)
				.operationName("journalpost")
				.variables(Collections.singletonMap("queryJournalpostId", journalpostid))
				.build(), authorizationHeader, journalpostid);
	}
}