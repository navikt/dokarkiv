package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DefaultSettDatoSendtRequestMapperTest {
	private static final String ENDRET_AV_NAVN = "Jebediah";
	private static final Long JOURNALPOSTID_1 = 100L;
	private static final Long JOURNALPOSTID_2 = 200L;

	private SettDatoSendtRequestMapper settDatoSendtRequestMapper = new SettDatoSendtRequestMapper();

	@BeforeEach
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
		SettDatoSendtRequest request = createRequest();
		request.setDatoSendt(null);

		assertThrows(ApplicationException.class,
				() -> settDatoSendtRequestMapper.map(request),
				"datoSendt has not been provided");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdListeIsEmpty() throws Exception {
		SettDatoSendtRequest request = createRequest();
		request.getJournalpostIdListe().clear();

		assertThrows(ApplicationException.class,
				() -> settDatoSendtRequestMapper.map(request),
				"journalpostIdListe is empty");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdListeHasNullElement() throws Exception {
		SettDatoSendtRequest request = createRequest();
		request.getJournalpostIdListe().clear();
		request.getJournalpostIdListe().add(JOURNALPOSTID_1);
		request.getJournalpostIdListe().add(null);

		assertThrows(ApplicationException.class,
				() -> settDatoSendtRequestMapper.map(request),
				"journalpostIdListe has an element with a null value.");
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