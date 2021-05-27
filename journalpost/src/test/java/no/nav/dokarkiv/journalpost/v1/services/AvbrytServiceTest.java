package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.journalpost.v1.services.AvbrytService.FIKK_AVBRUTT;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AvbrytServiceTest {

	private JoarkRepository joarkRepositoryMock = Mockito.mock(JoarkRepository.class);

	private LagreAksjonsLoggService aksjonsLoggService = Mockito.mock(LagreAksjonsLoggService.class);;

	private AvbrytService avbrytService = new AvbrytService(joarkRepositoryMock, aksjonsLoggService);


	@Test
	public void HappyPathTest() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(D)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		String response = avbrytService.avbryt("38");

		assertEquals(FIKK_AVBRUTT, response);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIsmissingJournalStatusCode() {
		Journalpost journalpost = Journalpost.builder()
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				avbrytService.avbryt("38")
		);
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsInngaaende() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(OD)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				avbrytService.avbryt("38")
		);
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsAvbryt() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(A)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				avbrytService.avbryt("38")
		);
	}

}