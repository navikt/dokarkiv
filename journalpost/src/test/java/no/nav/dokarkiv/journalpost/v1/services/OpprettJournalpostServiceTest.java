package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostServiceTest {

	@Mock
	private JournalpostRepository journalpostRepository;
	@Mock
	private OpprettJournalpostOppslagService opprettJournalpostOppslagService;
	@InjectMocks
	private OpprettJournalpostTransaksjonService opprettJournalpostTransaksjonService;

	private OpprettJournalpostService opprettJournalpostService;

	@BeforeEach
	void setUp() {
		opprettJournalpostService = new OpprettJournalpostService(opprettJournalpostOppslagService, opprettJournalpostTransaksjonService);
	}

	@Test
	public void opprettDuplikatJournalpostTest() {
		String eksternReferanseId = "testerDuplikater";
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.eksternReferanseId(eksternReferanseId)
				.kanal(MottaksKanalCode.NAV_NO.toString())
				.journalposttype(JournalpostType.INNGAAENDE)
				.build();
		Journalpost journalpostEksisterende = Journalpost.builder()
				.journalpostId(1L)
				.journalstatus(JournalStatusCode.J)
				.journalposttype(JournalpostTypeCode.I)
				.kanalReferanseId(eksternReferanseId)
				.build();
		when(journalpostRepository.existsByKanalReferanseId(eksternReferanseId)).thenReturn(true);
		when(journalpostRepository.findByKanalReferanseId(eksternReferanseId)).thenReturn(Optional.of(journalpostEksisterende));
		OpprettJournalpostResult result = opprettJournalpostService.opprettJournalpost(request);
		assertTrue(result.alleredeOpprettet());
		assertEquals(1L, result.journalpostId());
	}

	@Test
	public void opprettDuplikatJournalpostMedJournalposttypeUtgaaendeTest() {
		String eksternReferanseId = "testerDuplikater";
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.eksternReferanseId(eksternReferanseId)
				.kanal(MottaksKanalCode.NAV_NO.toString())
				.journalposttype(JournalpostType.UTGAAENDE)
				.build();
		Journalpost journalpostEksisterende = Journalpost.builder()
				.journalpostId(1L)
				.journalstatus(JournalStatusCode.J)
				.journalposttype(JournalpostTypeCode.U)
				.kanalReferanseId(eksternReferanseId)
				.build();
		when(journalpostRepository.existsByKanalReferanseId(eksternReferanseId)).thenReturn(true);
		when(journalpostRepository.findByKanalReferanseId(eksternReferanseId)).thenReturn(Optional.of(journalpostEksisterende));
		OpprettJournalpostResult result = opprettJournalpostService.opprettJournalpost(request);
		assertTrue(result.alleredeOpprettet());
		assertEquals(journalpostEksisterende.getJournalpostId(), result.journalpostId());
	}
}
