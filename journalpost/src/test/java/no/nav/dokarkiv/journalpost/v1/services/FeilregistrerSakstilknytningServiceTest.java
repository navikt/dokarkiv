package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.FeilregistreringAlleredeOpphevetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeKnyttetTilSakException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SaksrelasjonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeilregistrerSakstilknytningServiceTest {

	@Mock
	private SaksrelasjonRepository saksrelasjonRepository;

	@Mock
	private JoarkRepository joarkRepository;
	@Mock
	private AksjonsLoggService aksjonsLoggService;

	@Captor
	private ArgumentCaptor<List<ArkivElementEndringTO>> arkivElementEndringCaptor;

	@InjectMocks
	private FeilregistrerSakstilknytningService feilregistrerSakstilknytningService;

	@Test
	public void feilregistrerSakstilknytningKanIkkeFinneJournalpost() {
		Long journalpostId = 1L;
		when(joarkRepository.existsById(journalpostId)).thenThrow(
				new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=1 i joark"));

		assertThrows(JournalpostIkkeFunnetException.class,
				() -> feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId)),
				"Kunne ikke finne journalpost med journalpostId=1 i joark");
	}

	@Test
	public void feilregistrerSakstilknytningKanIkkeFinneSakstilknytning() {
		Long journalpostId = 1L;
		when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);

		assertThrows(JournalpostIkkeKnyttetTilSakException.class,
				() -> feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId)),
				"Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak");
	}

	@Test
	public void feilregistrerSakstilknytning() {
		Long journalpostId = 1L;
		when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
		when(saksrelasjonRepository.findByJournalpostId(eq(1L))).thenReturn(
				Optional.of(Saksrelasjon.builder().build()));

		feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId));
		verify(aksjonsLoggService).validateAndSaveAksjonsLogg(any(AksjonsLoggTO.class), arkivElementEndringCaptor.capture());

		var arkivElementEndringTOS = arkivElementEndringCaptor.getValue();
		assertThat(arkivElementEndringTOS.size(), is(1));
		assertThat(arkivElementEndringTOS.get(0).getFraVerdi(), is("false"));
		assertThat(arkivElementEndringTOS.get(0).getTilVerdi(), is("true"));
		assertThat(arkivElementEndringTOS.get(0).getArkivElement(), is("Journalpost.Saksrelasjon.feilregistrert"));
	}

	@Test
	public void kanIkkeOpphevFeilregistrertSakstilknytningForJournalpostUtenFeilregistrering() {
		Long journalpostId = 1L;
		when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
		when(saksrelasjonRepository.findByJournalpostId(eq(1L))).thenReturn(
				Optional.of(Saksrelasjon.builder().build()));

		assertThrows(FeilregistreringAlleredeOpphevetException.class,
				() -> feilregistrerSakstilknytningService.opphevFeilregistrertSakstilknytning(String.valueOf(journalpostId)),
				"Feilregistreringen er allerede opphevet");
	}

	@Test
	public void opphevFeilregistrertSakstilknytning() {
		Long journalpostId = 1L;
		when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
		when(saksrelasjonRepository.findByJournalpostId(eq(1L))).thenReturn(
				Optional.of(Saksrelasjon.builder().feilregistrert(true).build()));

		feilregistrerSakstilknytningService.opphevFeilregistrertSakstilknytning(String.valueOf(journalpostId));
	}

}