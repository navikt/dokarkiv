package no.nav.dokarkiv.hentjournalsakinfo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.LdapConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, HentJournalsakinfoConfig.class, LdapConfig.class})
@ActiveProfiles("itest,ldap")
@AutoConfigureDataLdap
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractHentjournalsakinfoItest {

	protected static final String USERNAME = "srvdokarkiv";
	protected static final String ***passord=gammelt_passord***";

	@Inject
	protected TestRestTemplate restTemplate;

	@Inject
	protected JoarkRepository joarkRepository;

	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@PersistenceContext
	protected EntityManager entityManager;

	@Before
	public void setUpItest() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());

	}

	protected HttpEntity createHeaders() {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
				.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);

		return new HttpEntity(headers);
	}


}
