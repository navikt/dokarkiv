package no.nav.dokarkiv.hentjournalsakinfo;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.security.BasicAuthRestInterceptor;
import no.nav.dokarkiv.core.security.LdapConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, HentJournalsakinfoConfig.class, LdapConfig.class, AbstractHentjournalsakinfoItest.Config.class})
@ActiveProfiles("itest,ldap")
@AutoConfigureDataLdap
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractHentjournalsakinfoItest {

	@Configuration
	static class Config {
		@Bean
		@Named("basicAuthReadAccessRestInterceptor")
		HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
															  CacheManager cacheManager,
															  @Value("${ldap.basedn}") String baseDn,
															  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn) {
			// kan ikke teste gruppemedlemskap pga embedded unboundid ldap server ikke støtter det.
			return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, null, ldapTemplate, cacheManager);
		}
	}

	protected static final String USERNAME = "srvsaf";
	protected static final String ***passord=gammelt_passord***";

	@Inject
	protected TestRestTemplate restTemplate;

	@Inject
	protected JoarkRepository joarkRepository;

	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Before
	public void setUpItest() {
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());

	}

	@After
	public void cleanUp() {
		TestTransaction.end();
		joarkRepository.deleteAll();
	}

	protected HttpEntity createHeaderEntity() {
		return new HttpEntity(createDefaultHeaders());
	}

	protected HttpHeaders createDefaultHeaders() {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD).getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		return headers;
	}

	protected void persist(Journalpost... journalposts) {
		joarkRepository.saveAll(Arrays.asList(journalposts));
	}
}
