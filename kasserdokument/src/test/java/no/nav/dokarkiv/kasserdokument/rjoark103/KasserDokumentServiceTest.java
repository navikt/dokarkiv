package no.nav.dokarkiv.kasserdokument.rjoark103;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.createKasserDokumentRequest;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.kasserdokument.util.TestUtil.opprettHoveddokumentForEnhetstest;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.core.repository.JoarkDeleteRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class KasserDokumentServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;
	private static final String TITTEL = "Tittel";
	private static Journalpost journalpost = null;
	private static DokumentInfo dokumentInfo = null;

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private JoarkDeleteRepository deleteRepository;
	@Mock
	private SkjermingService skjermingService;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private KasserDokumentService kasserDokumentService;

	@Before
	public void setUp() {
		kasserDokumentService = new KasserDokumentService(dokumentinfoRepository, deleteRepository, skjermingService);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeTidligKassereDokument_hvisDokumentInfoIkkeErBegrensetSomKassert() {
		thrown.expect(SkjermingIkkeFunnetException.class);
		thrown.expectMessage(String.format(
				"Fildetaljene for dokumentInfoId=%s er ikke skjermet, kan ikke kassere dokumentet",
				DOKUMENTINFO_ID));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));

		kasserDokumentService.kasserDokument(createKasserDokumentRequest(DOKUMENTINFO_ID));
	}

	@Test
	public void skalTidligKasserDokument_medDokumentKnyttetFlereJournalposter() {
		Journalpost journalpost2 = opprettHoveddokumentForEnhetstest();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo, journalpost2);
		assertTrue(dokumentInfo.isRelatedToMultipleJournalposts());

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(skjermingService.isDokumentInfoKassert(dokumentInfo)).thenReturn(true);

		kasserDokumentService.kasserDokument(createKasserDokumentRequest(DOKUMENTINFO_ID));
	}

	@Test
	public void skallTidligKassereDokument_medDokumentKnyttetEnJournalpost() {
		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(skjermingService.isDokumentInfoKassert(dokumentInfo)).thenReturn(true);

		kasserDokumentService.kasserDokument(createKasserDokumentRequest(DOKUMENTINFO_ID));
	}
}
