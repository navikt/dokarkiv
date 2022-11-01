package no.nav.dokarkiv.hentjournalsakinfo;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.AbstractRestIT;
import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SakRepository;
import no.nav.dokarkiv.core.security.BasicAuthRestInterceptor;
import no.nav.dokarkiv.core.security.LdapConfig;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.util.Base64Utils;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@SpringBootTest(
		webEnvironment = RANDOM_PORT,
		classes = {CoreConfig.class, HentJournalsakinfoConfig.class, LdapConfig.class,
				AbstractHentjournalsakinfoItest.Config.class, TokenGeneratorConfiguration.class},
		properties = {"spring.main.allow-bean-definition-overriding=true"}
)
@ActiveProfiles({"itest", "wiremock", "ldap"})
public abstract class AbstractHentjournalsakinfoItest extends AbstractRestIT {

	@Configuration
	static class Config {
		@Bean
		HandlerInterceptor basicAuthReadAccessRestInterceptor(LdapTemplate ldapTemplate,
															  CacheManager cacheManager,
															  @Value("${ldap.basedn}") String baseDn,
															  @Value("${ldap.serviceuser.basedn}") String serviceuserBaseDn) {
			// kan ikke teste gruppemedlemskap pga embedded unboundid ldap server ikke støtter det.
			return new BasicAuthRestInterceptor(baseDn, serviceuserBaseDn, null, ldapTemplate, cacheManager);
		}

		@Bean
		public AzureAdGraphService azureAdGraphService() {
			AzureAdGraphService azureAdGraphServiceMock = mock(AzureAdGraphService.class);
			when(azureAdGraphServiceMock.hentFulltNavn(any())).thenReturn("Username");
			return azureAdGraphServiceMock;
		}
	}

	protected static final String USERNAME = "srvsaf";
	protected static final String PASSWORD = "hemmelig";

	@Autowired
	protected TestRestTemplate restTemplate;

	@Autowired
	protected JoarkRepository joarkRepository;

	@Autowired
	protected SakRepository sakRepository;

	@Autowired
	protected DokumentinfoRepository dokumentInfoRepository;

	@Autowired
	protected DokumentFilRepository dokumentFilRepository;

	@Autowired
	protected ObjectMapper objectMapper;

	@BeforeEach
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
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, basicAuthHeader);
		return headers;
	}
}
