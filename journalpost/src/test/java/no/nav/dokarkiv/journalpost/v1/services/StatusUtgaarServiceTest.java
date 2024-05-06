package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.LagreAksjonsLoggService;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.UgyldigJournalStatusException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.journalpost.v1.services.UtgaarService.FIKK_UTGAAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatusUtgaarServiceTest {

	@Mock
	private JournalpostRepository journalpostRepositoryMock;
	@Mock
	private LagreAksjonsLoggService aksjonsLoggService;
	@InjectMocks
	private UtgaarService utgaarService;

	@Test
	public void HappyPathTest() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(OD)
				.journalposttype(I)
				.build();
		when(journalpostRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		String response = utgaarService.settStatusUtgaar("38");

		assertEquals(FIKK_UTGAAR, response);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIsmissingJournalStatusCode() {
		Journalpost journalpost = Journalpost.builder().build();

		when(journalpostRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () -> utgaarService.settStatusUtgaar("38"));
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsUtgaaende() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(D)
				.build();

		when(journalpostRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () -> utgaarService.settStatusUtgaar("38"));
	}

	@Test
	public void shouldThrowExceptionWhenJournalStatusCodeIsAvbryt() {
		Journalpost journalpost = Journalpost.builder()
				.journalstatus(A)
				.build();

		when(journalpostRepositoryMock.findById(any(Long.class))).thenReturn(Optional.of(journalpost));

		assertThrows(UgyldigJournalStatusException.class, () -> utgaarService.settStatusUtgaar("38"));
	}
}