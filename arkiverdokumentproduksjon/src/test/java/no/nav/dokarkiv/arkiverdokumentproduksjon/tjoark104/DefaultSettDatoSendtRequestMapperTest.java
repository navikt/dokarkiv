package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DefaultSettDatoSendtRequestMapperTest {
	private static final String ENDRET_AV_NAVN = "Jebediah";
	private static final Long JOURNALPOSTID_1 = 100L;
	private static final Long JOURNALPOSTID_2 = 200L;

	private SettDatoSendtRequestMapper settDatoSendtRequestMapper = new SettDatoSendtRequestMapper();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-06-20T14:31:54.767");
	}

	@Test
	public void shouldMap() throws Exception {
		SettDatoSendtRequestTo domainRequest = settDatoSendtRequestMapper.map(createRequest());

		assertThat(domainRequest.getDatoSendtPrint(), is(DateProvider.getToday()));
		assertThat(domainRequest.getEndretAvNavn(), is(ENDRET_AV_NAVN));
		assertThat(domainRequest.getJournalpostIds(), hasItems(JOURNALPOSTID_1, JOURNALPOSTID_2));
	}

	@Test
	public void shouldThrowExceptionIfDatoSendtIsNull() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("datoSendt has not been provided");

		SettDatoSendtRequest request = createRequest();
		request.setDatoSendt(null);

		settDatoSendtRequestMapper.map(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdListeIsEmpty() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("journalpostIdListe is empty");

		SettDatoSendtRequest request = createRequest();
		request.getJournalpostIdListe().clear();

		settDatoSendtRequestMapper.map(request);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdListeHasNullElement() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("journalpostIdListe has an element with a null value.");

		SettDatoSendtRequest request = createRequest();
		request.getJournalpostIdListe().clear();
		request.getJournalpostIdListe().add(JOURNALPOSTID_1);
		request.getJournalpostIdListe().add(null);

		settDatoSendtRequestMapper.map(request);
	}

	private SettDatoSendtRequest createRequest() throws DatatypeConfigurationException {
		SettDatoSendtRequest settDatoSendtRequest = new SettDatoSendtRequest();
		settDatoSendtRequest.setDatoSendt(xmlGregorianCalendarToday());
		settDatoSendtRequest.setEndretAvNavn(ENDRET_AV_NAVN);
		settDatoSendtRequest.getJournalpostIdListe().add(JOURNALPOSTID_1);
		settDatoSendtRequest.getJournalpostIdListe().add(JOURNALPOSTID_2);
		return settDatoSendtRequest;
	}

	public XMLGregorianCalendar xmlGregorianCalendarToday() throws DatatypeConfigurationException {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(DateProvider.getToday());
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
	}
}