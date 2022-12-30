package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Test of DefaultAvbrytJournalpostService
 *
 * @author Stig Strøm
 */
@ExtendWith(MockitoExtension.class)
public class DefaultAvbrytJournalpostServiceTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final String ENDRET_AV_NAVN = "Saksbehandler1";

	private Journalpost journalpost;

	@Mock
	private JournalpostRepositorySkjermet repositoryMock;
	@Mock
	private AvbrytJournalpostValidator avbrytJournalpostValidator;
	@Mock
	private AvbrytJournalpostUpdater avbrytJournalpostUpdater;
	@InjectMocks
	DefaultAvbrytJournalpostService service;

	@BeforeEach
	public void setUp() {
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldRunAvbrytJournalpost() {
		journalpost = createJournalpost();
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(avbrytJournalpostUpdater.updateJournalpost(journalpost, ENDRET_AV_NAVN)).thenReturn(journalpost);

		service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN));

		verify(avbrytJournalpostValidator).validate(journalpost);
		verify(avbrytJournalpostUpdater).updateJournalpost(journalpost, ENDRET_AV_NAVN);
		verifyNoMoreInteractions(avbrytJournalpostValidator, avbrytJournalpostUpdater);
	}

	@Test
	public void shouldThrowExceptionIfRequestToIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> service.avbrytJournalpost(null),
				"Request cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> service.avbrytJournalpost(new AvbrytJournalpostRequestTo(null, ENDRET_AV_NAVN)),
				"JournalpostId cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, null)),
				"EndretAvNavn cannot be empty or missing");
	}

	@Test
	public void shouldThrowFunctionalExceptionIfJournalpostDoesNotExist() {
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.empty());

		assertThrows(NoJournalpostFoundException.class,
				() -> service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN)),
				"Journalpost with id");

		verifyNoMoreInteractions(avbrytJournalpostValidator, avbrytJournalpostUpdater);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.build();
	}
}