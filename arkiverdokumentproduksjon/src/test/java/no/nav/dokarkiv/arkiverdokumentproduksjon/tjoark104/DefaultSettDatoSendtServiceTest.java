package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

/**
 * Unit tests for DefaultSettDatoSendtService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultSettDatoSendtServiceTest {

	private static final String ENDRET_AV_NAVN = "Bill";
	private static final Long JOURNALPOSTID_1 = 100L;
	private static final Long JOURNALPOSTID_2 = 200L;

	@Rule
	public ExpectedException thrown = ExpectedException.none();
	@Mock
	private JoarkRepository joarkRepositoryMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;
	@InjectMocks
	private DefaultSettDatoSendtService defaultSettDatoSendtService;

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-06-20T14:31:54.767");
	}

	@After
	public void tearDown() throws Exception {
		DateProvider.configure(false, null);
	}

	@Test
	public void shouldSettDatoSendt() throws Exception {
		Journalpost journalpost1 = new Journalpost(JOURNALPOSTID_1, 0L);
		Journalpost journalpost2 = new Journalpost(JOURNALPOSTID_2, 0L);
		when(joarkRepositoryMock.findById(JOURNALPOSTID_1)).thenReturn(Optional.of(journalpost1));
		when(joarkRepositoryMock.findById(JOURNALPOSTID_2)).thenReturn(Optional.of(journalpost2));

		defaultSettDatoSendtService.settDatoSendt(createValidDomainRequest());

		assertThat(journalpost1.getSendtPrintDato(), is(DateProvider.getToday()));
		assertThat(journalpost2.getSendtPrintDato(), is(DateProvider.getToday()));

		verify(sporingPopulatorMock).populateSporingInfo(journalpost1, ENDRET_AV_NAVN);
		verify(sporingPopulatorMock).populateSporingInfo(journalpost2, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostNotFound() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("Could not find Journalpost with journalpostId: " + JOURNALPOSTID_2);

		Journalpost journalpost1 = new Journalpost(JOURNALPOSTID_1, 0L);
		when(joarkRepositoryMock.findById(JOURNALPOSTID_1)).thenReturn(Optional.of(journalpost1));

		defaultSettDatoSendtService.settDatoSendt(createValidDomainRequest());
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdsIsNull() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("journalpostIds was null or empty");

		defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(null, ENDRET_AV_NAVN, DateProvider.getToday()));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdsIsEmpty() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("journalpostIds was null or empty");

		defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(new ArrayList<Long>(), ENDRET_AV_NAVN, DateProvider
				.getToday()));
	}

	@Test
	public void shouldThrowExceptionIfEndretAvNavnIsNull() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("endretAvNavn was null or empty");

		defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(Arrays.asList(JOURNALPOSTID_1), null, DateProvider.getToday()));
	}

	@Test
	public void shouldThrowExceptionIfDatoSendtPrintIsNull() throws Exception {
		thrown.expect(ApplicationException.class);
		thrown.expectMessage("datoSendtPrint was null");

		defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(Arrays.asList(JOURNALPOSTID_1, JOURNALPOSTID_2), ENDRET_AV_NAVN, null));
	}

	private SettDatoSendtRequestTo createValidDomainRequest() {
		return new SettDatoSendtRequestTo(
				Arrays.asList(JOURNALPOSTID_1, JOURNALPOSTID_2), ENDRET_AV_NAVN, DateProvider.getToday()
		);
	}
}