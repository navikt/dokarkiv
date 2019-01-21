package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103;

import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.repository.JoarkRepositorySkjermet;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

/**
 * Test of DefaultAvbrytJournalpostService
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultAvbrytJournalpostServiceTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final String ENDRET_AV_NAVN = "Saksbehandler1";

	private Journalpost journalpost;

	@Rule
	public ExpectedException expected = ExpectedException.none();
	@Mock
    private JoarkRepositorySkjermet repositoryMock;
	@Mock
	private AvbrytJournalpostValidator avbrytJournalpostValidator;
	@Mock
	private AvbrytJournalpostUpdater avbrytJournalpostUpdater;
	@InjectMocks
	DefaultAvbrytJournalpostService service;

	@Before
	public void setUp() {
		RequestContextSetter.setRequestContextForUnitTest();
	}

	@Test
	public void shouldRunAvbrytJournalpost() throws Exception {
		journalpost = createJournalpost();
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.of(journalpost));
		when(avbrytJournalpostUpdater.updateJournalpost(journalpost, ENDRET_AV_NAVN)).thenReturn(journalpost);

		service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN));

		verify(avbrytJournalpostValidator).validate(journalpost);
		verify(avbrytJournalpostUpdater).updateJournalpost(journalpost, ENDRET_AV_NAVN);
		verifyNoMoreInteractions(avbrytJournalpostValidator, avbrytJournalpostUpdater);
	}

	@Test
	public void shouldThrowExceptionIfRequestToIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("Request cannot be empty or missing");
		service.avbrytJournalpost(null);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("JournalpostId cannot be empty or missing");
		service.avbrytJournalpost(new AvbrytJournalpostRequestTo(null, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsNull() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage("EndretAvNavn cannot be empty or missing");
		service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, null));
	}

	@Test
	public void shouldThrowFunctionalExceptionIfJournalpostDoesNotExist() throws Exception {
		when(repositoryMock.findById(JOURNALPOST_ID)).thenReturn(Optional.ofNullable(null));
		expected.expect(NoJournalpostFoundException.class);
		expected.expectMessage("Journalpost with id");
		service.avbrytJournalpost(new AvbrytJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN));
		verifyNoMoreInteractions(avbrytJournalpostValidator, avbrytJournalpostUpdater);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.build();
	}
}