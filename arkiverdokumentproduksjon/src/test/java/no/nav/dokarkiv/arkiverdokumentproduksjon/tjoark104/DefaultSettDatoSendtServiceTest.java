package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.sporing.SporingPopulator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DefaultSettDatoSendtService
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultSettDatoSendtServiceTest {

	private static final String ENDRET_AV_NAVN = "Bill";
	private static final Long JOURNALPOSTID_1 = 100L;
	private static final Long JOURNALPOSTID_2 = 200L;

	@Mock
	private JournalpostRepositorySkjermet journalpostRepositorySkjermetMock;
	@Mock
	private SporingPopulator sporingPopulatorMock;
	@InjectMocks
	private DefaultSettDatoSendtService defaultSettDatoSendtService;

	@BeforeEach
	public void setUp() {
		DateProvider.configure(true, "2018-06-20T14:31:54.767");
	}

	@AfterEach
	public void tearDown() {
		DateProvider.configure(false, null);
	}

	@Test
	public void shouldSettDatoSendt() {
		Journalpost journalpost1 = new Journalpost(JOURNALPOSTID_1, 0L);
		Journalpost journalpost2 = new Journalpost(JOURNALPOSTID_2, 0L);
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOSTID_1)).thenReturn(Optional.of(journalpost1));
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOSTID_2)).thenReturn(Optional.of(journalpost2));

		defaultSettDatoSendtService.settDatoSendt(createValidDomainRequest());

		assertThat(journalpost1.getSendtPrintDato(), is(DateProvider.getToday()));
		assertThat(journalpost2.getSendtPrintDato(), is(DateProvider.getToday()));

		verify(sporingPopulatorMock).populateSporingInfo(journalpost1, ENDRET_AV_NAVN);
		verify(sporingPopulatorMock).populateSporingInfo(journalpost2, ENDRET_AV_NAVN);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostNotFound() {
		Journalpost journalpost1 = new Journalpost(JOURNALPOSTID_1, 0L);
		when(journalpostRepositorySkjermetMock.findById(JOURNALPOSTID_1)).thenReturn(Optional.of(journalpost1));

		assertThrows(ApplicationException.class,
				() -> defaultSettDatoSendtService.settDatoSendt(createValidDomainRequest()),
				"Could not find Journalpost with journalpostId: " + JOURNALPOSTID_2);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdsIsNull() {
		assertThrows(ApplicationException.class,
				() -> defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(null, ENDRET_AV_NAVN, DateProvider.getToday())),
				"journalpostIds was null or empty");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdsIsEmpty() {
		assertThrows(ApplicationException.class,
				() -> defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(new ArrayList<Long>(), ENDRET_AV_NAVN, DateProvider
						.getToday())),
				"journalpostIds was null or empty");
	}

	@Test
	public void shouldThrowExceptionIfEndretAvNavnIsNull() {
		assertThrows(ApplicationException.class,
				() -> defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(List.of(JOURNALPOSTID_1), null, DateProvider.getToday())),
				"endretAvNavn was null or empty");
	}

	@Test
	public void shouldThrowExceptionIfDatoSendtPrintIsNull() {
		assertThrows(ApplicationException.class,
				() -> defaultSettDatoSendtService.settDatoSendt(new SettDatoSendtRequestTo(Arrays.asList(JOURNALPOSTID_1, JOURNALPOSTID_2), ENDRET_AV_NAVN, null)),
				"datoSendtPrint was null");
	}

	private SettDatoSendtRequestTo createValidDomainRequest() {
		return new SettDatoSendtRequestTo(
				Arrays.asList(JOURNALPOSTID_1, JOURNALPOSTID_2), ENDRET_AV_NAVN, DateProvider.getToday()
		);
	}
}