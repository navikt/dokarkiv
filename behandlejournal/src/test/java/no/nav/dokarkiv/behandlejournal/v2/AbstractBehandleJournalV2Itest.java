package no.nav.dokarkiv.behandlejournal.v2;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import no.nav.dokarkiv.core.CoreConfig;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.BidragMellomlagringRepository;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.core.stelvio.SimpleRequestContext;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.ForretningsmessigUnntak;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
		classes = {CoreConfig.class, BehandleJournalV2Config.class})
@ActiveProfiles("itest")
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
public abstract class AbstractBehandleJournalV2Itest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Inject
	protected BehandleJournalV2 behandleJournalProvider;
	@Inject
	protected JoarkRepository joarkRepository;
	@Inject
	protected DokumentFilRepository dokumentFilRepository;
	@Inject
	protected BidragMellomlagringRepository bidragMellomlagringRepository;

	@Inject
	protected TransactionTemplate transactionTemplate;

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
}
