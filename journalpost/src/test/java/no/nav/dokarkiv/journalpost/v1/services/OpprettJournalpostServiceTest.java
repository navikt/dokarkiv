package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.consumer.pdl.IdentConsumer;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.DokumentFilRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.core.sporing.DefaultSporingPopulator;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResult;
import no.nav.dokarkiv.journalpost.v1.mappers.OpprettJournalpostApiRequestMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OpprettJournalpostServiceTest {

	@Mock
	private JoarkRepository joarkRepository;
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
		Journalpost journalpostEksisterende = Journalpost
				.builder()
				.journalposttype(JournalpostTypeCode.I)
				.kanalReferanseId(eksternReferanseId)
				.build();
		when(joarkRepository.findJournalpostAllByKanalReferanseId(eksternReferanseId)).thenReturn(List.of(journalpostEksisterende));
		OpprettJournalpostResult result = opprettJournalpostService.opprettJournalpost(request);
		assertFalse(result.isAlreadyOpprettet());
		assertEquals(result.getJournalpost(), journalpostEksisterende);
	}
}
