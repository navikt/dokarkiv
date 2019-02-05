package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.dokarkiv.core.repository.JournalpostDokumentInfoRelasjonRepository;
import no.nav.dokarkiv.skjermarkivenhet.SkjermArkivenhetHeader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class skjermArkivenhetServiceTest {

	@Mock
	private SkjermingService skjermingService;
	@Mock
	private JoarkRepository joarkRepository;
	@Mock
	private JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private SkjermArkivenhetService skjermArkivenhetService;

	@Before
	public void setUp() {
		skjermArkivenhetService = new SkjermArkivenhetService(skjermingService);

	}

	@Test
	public void skallSkjermArkivenhet_medJournalpost() {
		Journalpost journalpost = createJournalpost();
		SkjermArkivenhetHeader header = SkjermArkivenhetHeader.builder()
				.journalpostId(journalpost.getJournalpostId())
				.arkivenhet(ArkivenhetCode.JOURNALPOST)
				.skjerming(SkjermingTypeCode.POL)
				.build();

		when(joarkRepository.findById(anyLong())).thenReturn(Optional.of(journalpost));

		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(header);
		assertThat(response.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertNull(response.getDokumentInfoId());
	}

//	@Test
//	public void skallIkkeSkjermArkivenhet_medUgyldigJournalpost(){
//		thrown.expect(JournalpostIkkeFunnetException.class);
//		thrown.expectMessage("Kan ikke finne journalpost med journalpostId=1");
//
////		Journalpost journalpost = createJournalpost();
//		SkjermArkivenhetHeader header = SkjermArkivenhetHeader.builder()
//				.journalpostId(null)
//				.arkivenhet(ArkivenhetCode.JOURNALPOST)
//				.skjerming(SkjermingTypeCode.POL)
//				.build();
//
//		when(joarkRepository.findById(anyLong())).thenReturn(null);//Throw(new JournalpostIkkeFunnetException("Kan ikke finne journalpost med journalpostId=1"));
//
//		skjermArkivenhetService.skjermArkivenhet(header);
////		assertThat(response.getJournalpostId(), is(journalpost.getJournalpostId()));
////		assertNull(response.getDokumentInfoId());
//	}

	@Test
	public void skallSkjermArkivenhet_medJournalpostDokumentInfoRelasjon() {
		Journalpost journalpost = createJournalpost();
		JournalpostDokumentInfoRelasjon rel = journalpost.findHoveddokumentDokumentInfoRelasjon();
		Long dokumentInfoId = rel.getDokumentInfo().getDokumentInfoId();
		SkjermArkivenhetHeader header = SkjermArkivenhetHeader.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(dokumentInfoId)
				.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
				.skjerming(SkjermingTypeCode.POL)
				.build();

		when(journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpost.getJournalpostId(), dokumentInfoId)).thenReturn(Optional.of(rel));

		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(header);
		assertThat(response.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(response.getDokumentInfoId(), is(dokumentInfoId));
	}

	@Test
	public void skallSkjermArkivenhet_medVariantFormat() {
		Journalpost journalpost = createJournalpost();
		JournalpostDokumentInfoRelasjon rel = journalpost.findHoveddokumentDokumentInfoRelasjon();
		DokumentInfo dokumentInfo = rel.getDokumentInfo();
		SkjermArkivenhetHeader header = SkjermArkivenhetHeader.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.variant(VariantFormatCode.ARKIV)
				.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
				.skjerming(SkjermingTypeCode.POL)
				.build();

		when(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())).thenReturn(Optional.of(dokumentInfo));

		SkjermArkivenhetResponse response = skjermArkivenhetService.skjermArkivenhet(header);
		assertThat(response.getDokumentInfoId(), is(dokumentInfo.getDokumentInfoId()));
		assertNull(response.getJournalpostId());
	}


	/**
	 * 1: skjermJournalpost - tidligere logiskSlettDokument(HOVEDDOKUMENT)
	 *
	 * (POST,
	 * /rest/skjermArkivenhet/pol/journalpost/{journalpostId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.skjermArkivenhet(pol, journalpost, journalpostId, null, null);
	 * }
	 *
	 *
	 * 2:	opphevSkjermJournalpost - tidligere angreLogiskSlettDokument(HOVEDDOKUMENT)
	 *
	 * (DELETE,
	 * /rest/skjermArkivenhet/pol/journalpost/{journalpostId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.opphevSkjermArkivenhet(pol, journalpost, journalpostId, null, null);
	 * }
	 *
	 *
	 * 3: skjermJournalpostDokumentInfoRelasjon - tidligere logiskSlettDokument(VEDLEGG)
	 *
	 * (POST,
	 * /rest/skjermArkivenhet/pol/journalpost_dokument/{journalpostId}/{dokumentInfoId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.skjermArkivenhet(pol, journalpost_dokument, journalpostId, dokumentInfoId, null);
	 * }
	 *
	 *
	 * 4: opphevSkjermJournalpostDokumentInfoRelasjon - tidligere angreLogiskSlettDokument(VEDLEGG)
	 *
	 * (DELETE,
	 * /rest/skjermArkivenhet/pol/journalpost_dokument/{journalpostId}/{dokumentInfoId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.opphevSkjermArkivenhet(pol, journalpost_dokument, journalpostId, dokumentInfoId, null);
	 * }
	 *
	 *
	 * 5: skjermDokumentObjekt - tidligere logiskTidligKassasjon
	 *
	 * (POST,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, null);
	 * }
	 *
	 *
	 * 6: opphevSkjermDokumentObjekt - tidligere angreLogiskTidligKassasjon
	 *
	 * (DELETE,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, null);
	 * }
	 *
	 *
	 * 7: skjermArkivvariant
	 * 	- usikker på om dette skal være ett kall her eller vi skal ha tilgant til skjermArkivenhetService
	 * 	  i ArkiverKorrigertDokument modulen og skjøte alt derifra.
	 *
	 * Hvis kall:
	 * (POST,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId}/{arkivVariant},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 * }
	 *
	 * Hvis del av ArkiverKorrigertDokument:
	 * 	skjermArkivenhetService.skjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 *
	 *
	 * 8: opphevSkjermDokumentObjekt(Arkivvariant)
	 * 	- usikker på om dette skal være ett kall her eller vi skal ha tilgant til skjermArkivenhetService
	 * 	  i ArkiverKorrigertDokument modulen og skjøte alt derifra.
	 *
	 * Hvis kall:
	 * (DELETE,
	 * /rest/skjermArkivenhet/pol/dokument_objekt/{dokumentInfoId}/{arkivVariant},
	 * header(aksjonslogg)){
	 * 	skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 * }
	 *
	 * Hvis del av ArkiverKorrigertDokument:
	 * 	skjermArkivenhetService.opphevSkjermArkivenhet(pol, dokument_objekt, dokumentInfoId, arkivVariant);
	 *
	 */
}
