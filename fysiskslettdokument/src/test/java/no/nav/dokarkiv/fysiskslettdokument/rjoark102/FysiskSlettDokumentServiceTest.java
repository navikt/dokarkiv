package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.DOKUMENT_INFO_ID_TEST_VEDLEGG;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.HJEMMEL_VEDLEGG;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.createRequest;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettOgReturnerVedleggRelasjonForEnhetstest;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Unit test for {@link FysiskSlettDokumentValidator}
 */
@RunWith(MockitoJUnitRunner.class)
public class FysiskSlettDokumentServiceTest {

	@InjectMocks
	private FysiskSlettDokumentService service;

	@Mock
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	@Mock
	private JoarkDeleteRepository deleteRepository;

	@Mock
	private FysiskSlettDokumentValidator validator;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	// Kontrollere slettefunksjonenene en og en.

	//Test av slettFilBeholdDokumentInfo
//	@Test
//	public void shouldSlettFilBeholdDokumentInfo(){
//		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(true);
//		FysiskSlettDokumentRequestTo requestTo = createRequest(vedleggRelasjon);
//
//		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
//		relasjonList.add(vedleggRelasjon);
//
//		validator.validerFysiskSlettEtVedleggKnyttetEnJP(relasjonList, requestTo);
//
//		service.slettDokumentFysisk(requestTo);
//	}


	// fysiskSlettEtVedleggKnyttetEnJP ---------------------------------------------

	@Test
	public void shouldfysiskSlettEtVedleggKnyttetEnJP() {
		JournalpostDokumentInfoRelasjon vedleggRelasjon = opprettOgReturnerVedleggRelasjonForEnhetstest(true);
		List<JournalpostDokumentInfoRelasjon> relasjonList = new ArrayList<>();
		relasjonList.add(vedleggRelasjon);
		Optional<List<JournalpostDokumentInfoRelasjon>> optionalRelList = Optional.of(relasjonList);

		when(journalpostDokumentInfoRelasjonRepository.findByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG))
				.thenReturn(optionalRelList);

		Journalpost vedleggJp = vedleggRelasjon.getJournalpost();
		DokumentInfo vedleggDokInfo = vedleggRelasjon.getDokumentInfo();

		FysiskSlettDokumentRequestTo requestTo = createRequest(vedleggJp.getJournalpostId(), vedleggDokInfo.getDokumentInfoId(), HJEMMEL_VEDLEGG);

//		doNothing(deleteRepository.deleteDokumentFilByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG));
//
//		when(deleteRepository.deleteFilDetaljerByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG)).thenCallRealMethod(vedleggRelasjon.getDokumentInfo().clearFildetaljerListe());
//
//		when(deleteRepository.deleteDokInfoTilleggByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG))
//				.then(vedleggRelasjon.getDokumentInfo().getTilleggsopplysninger().clear());
//		when(deleteRepository.deleteDokInfoByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG)).then(v"Slettet dokumentInfo");
//
//		when(deleteRepository.deleteDokInfoJPRelByDokumentInfoId(DOKUMENT_INFO_ID_TEST_VEDLEGG))
//				.then(vedleggRelasjon.getDokumentInfo()."Slettet journalpostDokumentInfoRelasjon");
//
		service.slettDokumentFysisk(requestTo);

//		assertNull(vedleggRelasjon.getDokumentInfo().getFildetaljerListe());
//		assertNull(vedleggRelasjon.getDokumentInfo());
//		assertNull(vedleggRelasjon);
//		assertNotNull(vedleggRelasjon.getJournalpost());
	}

}
