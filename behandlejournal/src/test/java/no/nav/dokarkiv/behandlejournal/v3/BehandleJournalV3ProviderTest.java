package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseResponse;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test class for BehandleJournalProvider.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class BehandleJournalV3ProviderTest {
	private static final Long JOURNALPOST_ID = 1L;
	private static final Long DOKUMENT_ID = 1L;
	private static final String FEIL_AARSAK = "feilAarsak";
	private static final String FEIL_KILDE = "feilKilde";
	private static final String EXCEPTION_MESSAGE = "Exception message";

	@Mock
	private BehandleJournalV3Pep behandleJournalV3PepMock;
	@Mock
	private BehandleJournalV3ServiceBi behandleJournalServiceMock;
	@Mock
	private BehandleJournalV3FaultInfoPopulator behandleJournalV3FaultInfoPopulatorMock;
	@Mock
	private JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapperMock;
	@Mock
	private JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapperMock;

	@InjectMocks
	private BehandleJournalV3Provider behandleJournalV3Provider;

	@BeforeEach
	public void setUp() {
		DateProvider.configure(true, DateProvider.getDate(new Date()));
	}

	@AfterEach
	public void tearDown() {
		DateProvider.configure(false, null);
	}

	@Test
	public void shouldDelegateToJournalfoerNotatHenvendelseServiceAndReturnResponse() throws Exception {
		JournalfoerNotatRequest wsRequest = new JournalfoerNotatRequest();
		wsRequest
				.setJournalpost(new no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoernotat.Journalpost());
		JournalfoerNotatResponse wsResponse = new JournalfoerNotatResponse();
		wsResponse.setJournalpostId(String.valueOf(JOURNALPOST_ID));
		JournalfoerNotatHenvendelseRequest domainRequest = new JournalfoerNotatHenvendelseRequest(
				new no.nav.dokarkiv.core.domain.entities.Journalpost());
		JournalfoerNotatHenvendelseResponse domainResponse = new JournalfoerNotatHenvendelseResponse(
				JOURNALPOST_ID);

		when(journalfoerNotatHenvendelseRequestMapperMock.map(wsRequest)).thenReturn(domainRequest);
		when(journalfoerNotatHenvendelseResponseMapperMock.map(domainResponse)).thenReturn(wsResponse);
		when(behandleJournalServiceMock.journalfoerNotatHenvendelse(domainRequest)).thenReturn(
				domainResponse);

		JournalfoerNotatResponse response = behandleJournalV3Provider.journalfoerNotat(wsRequest);

		verify(behandleJournalServiceMock).journalfoerNotatHenvendelse(domainRequest);
		assertThat(response.getJournalpostId(), is(String.valueOf(JOURNALPOST_ID)));
	}

	private JournalpostIkkeFunnet createJournalpostIkkeFunnet() {
		JournalpostIkkeFunnet journalpostIkkeFunnet = new JournalpostIkkeFunnet();
		journalpostIkkeFunnet.setFeilaarsak(FEIL_AARSAK);
		journalpostIkkeFunnet.setFeilkilde(FEIL_KILDE);
		journalpostIkkeFunnet.setFeilmelding(EXCEPTION_MESSAGE);
		journalpostIkkeFunnet.setTidspunkt(getXmlTimestamp());
		return journalpostIkkeFunnet;
	}

	private XMLGregorianCalendar getXmlTimestamp() {
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
