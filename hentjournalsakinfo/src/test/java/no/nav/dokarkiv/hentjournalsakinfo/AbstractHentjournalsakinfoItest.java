package no.nav.dokarkiv.hentjournalsakinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.security.BasicAuthRestInterceptor;
import no.nav.dokarkiv.core.security.LdapConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.util.Base64Utils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreConfig.class, HentJournalsakinfoConfig.class, LdapConfig.class,
				AbstractHentjournalsakinfoItest.Config.class, TestToolsAutoConfig.class, TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles({"itest", "wiremock", "ldap", "oidc"})
public abstract class AbstractHentjournalsakinfoItest extends AbstractRestIT {

	@Configuration
	static class Config {
		@Bean
		@Named("basicAuthReadAccessRestInterceptor")
		HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
															  CacheManager cacheManager,
															  @Value("${ldap.basedn}") String baseDn,
															  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn) {
			// kan ikke teste gruppemedlemskap pga embedded unboundid ldap server ikke støtter det.
			return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn,  null, ldapTemplate, cacheManager);
		}
	}

	protected static final String USERNAME = "srvsaf";
	protected static final String ***passord=gammelt_passord***";

	@Inject
	protected TestRestTemplate restTemplate;

	@Inject
	protected JoarkRepository joarkRepository;

	@Inject
	protected SakRepository sakRepository;

	@Inject
	protected DokumentinfoRepository dokumentInfoRepository;

	@Inject
	protected DokumentFilRepository dokumentFilRepository;

	@Inject
	protected JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Inject
	protected ObjectMapper objectMapper;

	@Before
	public void setUpItest() {
		entityManager.createNativeQuery("DROP ALIAS IF EXISTS TO_NUMBER; " +
				"CREATE ALIAS TO_NUMBER AS " +
				"'Long to_number(String s) {" +
				"    return s != null ? Long.valueOf(s) : null;" +
				"}'").executeUpdate();
		entityManager.createNativeQuery("ALTER SEQUENCE seq_sak RESTART WITH 1").executeUpdate();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
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
}
