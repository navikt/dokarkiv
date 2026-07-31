package no.nav.dokarkiv.internal;

import no.nav.dokarkiv.InternalConfig;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;
import org.wiremock.spring.EnableWireMock;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, InternalConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@EnableWireMock
public abstract class AbstractInternalIT extends AbstractRestIT {

	static final String INTERNAL_BASE_PATH = "/rest/internal/";
	static final String INTERNAL_JOURNALPOSTAPI_BASE_PATH = INTERNAL_BASE_PATH + "journalpostapi/v1/";
	static final String INTERNAL_JOURNALPOSTAPI_JOURNALPOST_PATH = "journalpost";

	protected static String apiInternalPath(String... path) {
		return fromPath(INTERNAL_BASE_PATH)
				.pathSegment(path)
				.build()
				.toUriString();
	}

	protected static String apiInternalJournalpostPath(String... path) {
		return fromPath(INTERNAL_JOURNALPOSTAPI_BASE_PATH)
				.pathSegment(INTERNAL_JOURNALPOSTAPI_JOURNALPOST_PATH)
				.pathSegment(path)
				.build()
				.toUriString();
	}

	protected void commitAndStartNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}
}
