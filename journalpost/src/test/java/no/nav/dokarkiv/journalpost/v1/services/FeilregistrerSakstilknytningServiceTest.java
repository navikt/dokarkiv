package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.exceptions.FeilregistreringAlleredeOpphevetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeKnyttetTilSakException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.SaksrelasjonRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FeilregistrerSakstilknytningServiceTest {

    @Mock
    private SaksrelasjonRepository saksrelasjonRepository;

    @Mock
    private JoarkRepository joarkRepository;

    @InjectMocks
    private FeilregistrerSakstilknytningService feilregistrerSakstilknytningService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(saksrelasjonRepository.findSaksrelasjonByJournalpostId(any(String.class))).thenThrow(
                new JournalpostIkkeKnyttetTilSakException("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak"));
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void feilregistrerSakstilknytningKanIkkeFinneJournalpost() {
        Long journalpostId = 1L;
        when(joarkRepository.existsById(journalpostId)).thenThrow(
                new JournalpostIkkeFunnetException("Kunne ikke finne journalpost med journalpostId=1 i joark"));
        expectedException.expect(JournalpostIkkeFunnetException.class);
        expectedException.expectMessage("Kunne ikke finne journalpost med journalpostId=1 i joark");
        feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId));
    }

    @Test
    public void feilregistrerSakstilknytningKanIkkeFinneSakstilknytning() {
        Long journalpostId = 1L;
        when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
        expectedException.expect(JournalpostIkkeKnyttetTilSakException.class);
        expectedException.expectMessage("Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak");
        feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId));
    }

    @Test
    public void feilregistrerSakstilknytning() {
        Long journalpostId = 1L;
        when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
        when(saksrelasjonRepository.findSaksrelasjonByJournalpostId(any(String.class))).thenReturn(
                Optional.of(Saksrelasjon.builder().build()));

        List<ArkivElementEndringTO> arkivElementEndringTOS = feilregistrerSakstilknytningService.feilregistrerSakstilknytning(String.valueOf(journalpostId));

        assertThat(arkivElementEndringTOS.size(), is(1));
        assertThat(arkivElementEndringTOS.get(0).getFraVerdi(), is("false"));
        assertThat(arkivElementEndringTOS.get(0).getTilVerdi(), is("true"));
        assertThat(arkivElementEndringTOS.get(0).getArkivElement(), is("Journalpost.Saksrelasjon.feilregistrert"));
    }

    @Test
    public void kanIkkeOpphevFeilregistrertSakstilknytningForJournalpostUtenFeilregistrering() {
        Long journalpostId = 1L;
        when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
        when(saksrelasjonRepository.findSaksrelasjonByJournalpostId(any(String.class))).thenReturn(
                Optional.of(Saksrelasjon.builder().build()));
        expectedException.expect(FeilregistreringAlleredeOpphevetException.class);
        expectedException.expectMessage("Feilregistreringen er allerede opphevet");

        feilregistrerSakstilknytningService.opphevFeilregistrertSakstilknytning(String.valueOf(journalpostId));
    }

    @Test
    public void opphevFeilregistrertSakstilknytning() {
        Long journalpostId = 1L;
        when(joarkRepository.existsById(journalpostId)).thenReturn(Boolean.TRUE);
        when(saksrelasjonRepository.findSaksrelasjonByJournalpostId(any(String.class))).thenReturn(
                Optional.of(Saksrelasjon.builder().feilregistrert(true).build()));

        feilregistrerSakstilknytningService.opphevFeilregistrertSakstilknytning(String.valueOf(journalpostId));
    }

}