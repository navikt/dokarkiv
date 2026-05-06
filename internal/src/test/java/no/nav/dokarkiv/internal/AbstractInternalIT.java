package no.nav.dokarkiv.internal;

import no.nav.dokarkiv.InternalConfig;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.TestTransaction;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, InternalConfig.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock"})
@AutoConfigureWireMock(port = 0)
public abstract class AbstractInternalIT extends AbstractRestIT {

	static final String JOURNALPOSTAPI_BASE_PATH = "/rest/journalpostapi/v1/";
	static final String INTERNAL_JOURNALPOSTAPI_BASE_PATH = "/rest/internal/journalpostapi/v1/";
	static final String INTERNAL_JOURNALPOSTAPI_JOURNALPOST_PATH = "journalpost";

	protected static String apiPath(String path) {
		return fromPath(JOURNALPOSTAPI_BASE_PATH).path(path).build().toUriString();
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
