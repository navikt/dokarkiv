package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import static no.nav.dokarkiv.core.util.TestDataUtils.createJournalpost;

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
import no.nav.dokarkiv.skjermarkivenhet.SkjermArkivenhetRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
		SkjermArkivenhetRequest request = SkjermArkivenhetRequest.builder()
				.journalpostId(journalpost.getJournalpostId())
				.arkivenhet(ArkivenhetCode.JOURNALPOST)
				.skjerming(SkjermingTypeCode.POL)
				.build();

		skjermArkivenhetService.skjermJournalpost(request.getJournalpostId(), request.getSkjerming());
	}

	@Test
	public void skallSkjermArkivenhet_medJournalpostDokumentInfoRelasjon() {
		Journalpost journalpost = createJournalpost();
		JournalpostDokumentInfoRelasjon rel = journalpost.findHoveddokumentDokumentInfoRelasjon();
		Long dokumentInfoId = rel.getDokumentInfo().getDokumentInfoId();
		SkjermArkivenhetRequest request = SkjermArkivenhetRequest.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(dokumentInfoId)
				.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
				.skjerming(SkjermingTypeCode.POL)
				.build();


		skjermArkivenhetService.skjermDokumentInfo(request.getJournalpostId(), request.getDokumentInfoId(), request
				.getSkjerming());

	}

	@Test
	public void skallSkjermArkivenhet_medVariantFormat() {
		Journalpost journalpost = createJournalpost();
		JournalpostDokumentInfoRelasjon rel = journalpost.findHoveddokumentDokumentInfoRelasjon();
		DokumentInfo dokumentInfo = rel.getDokumentInfo();
		SkjermArkivenhetRequest request = SkjermArkivenhetRequest.builder()
				.journalpostId(journalpost.getJournalpostId())
				.dokumentInfoId(dokumentInfo.getDokumentInfoId())
				.variant(VariantFormatCode.ARKIV)
				.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
				.skjerming(SkjermingTypeCode.POL)
				.build();

		skjermArkivenhetService.skjermDokumentFil(request.getDokumentInfoId(), request.getVariant(), request.getSkjerming());

	}
}
