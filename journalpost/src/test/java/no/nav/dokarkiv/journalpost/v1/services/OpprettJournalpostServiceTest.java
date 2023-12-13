package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OpprettJournalpostServiceTest {

	@Mock
	private JournalpostRepository journalpostRepository;
	@InjectMocks
	private OpprettJournalpostService opprettJournalpostService;

	@Test
	public void opprettDuplikatJournalpostTest() {
		String eksternReferanseId = "testerDuplikater";
		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.eksternReferanseId(eksternReferanseId)
				.kanal(MottaksKanalCode.NAV_NO.toString())
				.journalposttype(JournalpostType.INNGAAENDE)
				.build();
		Journalpost journalpostEksisterende = Journalpost.builder()
				.journalposttype(JournalpostTypeCode.I)
				.kanalReferanseId(eksternReferanseId)
				.build();
		when(journalpostRepository.existsByKanalReferanseId(eksternReferanseId)).thenReturn(true);
		when(journalpostRepository.findByKanalReferanseId(eksternReferanseId)).thenReturn(Optional.of(journalpostEksisterende));
		OpprettJournalpostResult result = opprettJournalpostService.opprettJournalpost(request);
		assertTrue(result.isAlreadyOpprettet());
		assertEquals(result.getJournalpost(), journalpostEksisterende);
	}

	@Test
	public void opprettDuplikatJournalpostMedJournalposttypeUtgaaendeTest() {
		String eksternReferanseId = "testerDuplikater";
		OpprettJournalpostRequest request = getOpprettJournalpostRequest(eksternReferanseId);
		Journalpost journalpostEksisterende = Journalpost.builder()
				.journalposttype(JournalpostTypeCode.U)
				.kanalReferanseId(eksternReferanseId)
				.build();
		when(journalpostRepository.existsByKanalReferanseId(eksternReferanseId)).thenReturn(true);
		when(journalpostRepository.findByKanalReferanseId(eksternReferanseId)).thenReturn(Optional.of(journalpostEksisterende));
		OpprettJournalpostResult result = opprettJournalpostService.opprettJournalpost(request);
		assertTrue(result.isAlreadyOpprettet());
		assertEquals(result.getJournalpost(), journalpostEksisterende);
	}

	@Test
	public void skalIkkeOppretteJournalpostMedFeilIEksternReferanseId() {
		String forLangEksternReferanseId = "bj5bzAng3tvvY7ao0A15Kj8lq3RuN78rPTDYQp9lz416At7egwxVKw3klqZngX39eYdwqDIs6KUbGurS97R78Mz25WO3r7ththg8QVf2HY1col7713VLSSFHvQKHzftl2aKIXF48pnftmwbNX201aX2msQDb8G8nd31gyzfvzZvYX0hcPeU9g5nm5NeV43RLRaKyR1BLG";
		String ugyldigeTegnReferanseId = "ØÆÅhører og mellomrom hører ikke hjemme i url og dermed i eksternReferanseId";
		OpprettJournalpostRequest forLangRequest = getOpprettJournalpostRequest(forLangEksternReferanseId);
		OpprettJournalpostRequest ugyldigeTegnRequest = getOpprettJournalpostRequest(ugyldigeTegnReferanseId);

		assertThrows(InputValideringFeiletException.class,
				() -> opprettJournalpostService.opprettJournalpost(forLangRequest),
				"EksternReferanseId kan ikke være over 200 tegn"
		);
		assertThrows(InputValideringFeiletException.class,
				() -> opprettJournalpostService.opprettJournalpost(ugyldigeTegnRequest),
				"EksternReferanseId kan bare inneholde annet enn alfanumeriske tegn"
		);
	}

	private static OpprettJournalpostRequest getOpprettJournalpostRequest(String eksternReferanseId) {
		return OpprettJournalpostRequest.builder()
				.eksternReferanseId(eksternReferanseId)
				.kanal(MottaksKanalCode.NAV_NO.toString())
				.journalposttype(JournalpostType.UTGAAENDE)
				.build();
	}
}
