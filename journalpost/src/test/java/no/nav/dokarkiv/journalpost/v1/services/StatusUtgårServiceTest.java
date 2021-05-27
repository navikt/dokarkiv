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
import static no.nav.dokarkiv.journalpost.v1.services.StatusUtgårService.FIKK_UTGAAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


public class StatusUtgårServiceTest {


	private JoarkRepository joarkRepositoryMock = Mockito.mock(JoarkRepository.class);

	private LagreAksjonsLoggService aksjonsLoggService = Mockito.mock(LagreAksjonsLoggService.class);;

	private StatusUtgårService statusUtgårService = new StatusUtgårService(joarkRepositoryMock, aksjonsLoggService);


	@Test
	public void HappyPathTest() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(OD)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		String response = statusUtgårService.settStatusUtgår("38");

		assertEquals(FIKK_UTGAAR, response);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIsmissingJournalStatusCode() {
		Journalpost journalpost = Journalpost.builder()
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				statusUtgårService.settStatusUtgår("38")
		);
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsUtgående() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(D)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				statusUtgårService.settStatusUtgår("38")
		);
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsAvbryt() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(A)
				.build();

		when(joarkRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () ->
				statusUtgårService.settStatusUtgår("38")
		);
	}

}