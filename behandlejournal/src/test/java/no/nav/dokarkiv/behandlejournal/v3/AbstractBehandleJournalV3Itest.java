package no.nav.dokarkiv.behandlejournal.v3;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.ForretningsmessigUnntak;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.util.GregorianCalendar;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, BehandleJournalV3Config.class})
@ActiveProfiles("itest,wiremock")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@AutoConfigureWireMock(port = 0)
@Transactional
public abstract class AbstractBehandleJournalV3Itest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected BehandleJournalV3 behandleJournalV3Provider;
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected DokumentinfoRepository dokumentinfoRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected BidragMellomlagringRepository bidragMellomlagringRepository;

	@Before
	public void setUpItest() {
		joarkRepository.deleteAll();
		dokumentFilRepository.deleteAll();
		bidragMellomlagringRepository.deleteAll();
		RequestContextSetter.setRequestContext(new SimpleRequestContext.Builder()
				.userId("itestuser")
				.componentId("itest")
				.build());
	}

	/**
	 * Utility assert method for MOD checked exceptions
	 *
	 * @param expectedExceptionClass
	 * @param expectedFaultInfo
	 */
	protected void assertForretningsmessigUnntak(Class<? extends Exception> expectedExceptionClass,
												 ForretningsmessigUnntak expectedFaultInfo) {
		expectedException.expect(expectedExceptionClass);
		expectedException.expectMessage(expectedFaultInfo.getFeilmelding());
		expectedException.expect(hasProperty("faultInfo", instanceOf(ForretningsmessigUnntak.class)));
		expectedException.expect(hasProperty("faultInfo",
				hasProperty("feilaarsak", containsString(expectedFaultInfo.getFeilaarsak()))));
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilkilde", is(expectedFaultInfo.getFeilkilde()))));
		expectedException.expect(hasProperty("faultInfo", hasProperty("feilmelding", is(expectedFaultInfo.getFeilmelding()))));
		expectedException.expect(hasProperty("faultInfo", hasProperty("tidspunkt", is(expectedFaultInfo.getTidspunkt()))));
	}

	/**
	 * A testable XMLGregorianCalendar. Uses DateProvider to configure dates.
	 *
	 * @return
	 */
	protected XMLGregorianCalendar getXmlTimestamp() {
		GregorianCalendar calendar = new GregorianCalendar();
		// Setting the date explicitly to make it testable
		calendar.setTime(DateProvider.getToday());
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			throw new ApplicationException("Unable to create XMLGregorianCalendar", e);
		}
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value()).withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).withBodyFile("abac/abac-permit.json")));
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}
}
