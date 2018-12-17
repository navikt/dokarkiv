package no.nav.dokarkiv.tidligkassasjon.rjoark107;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.tidligkassasjon.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.tidligkassasjon.util.TestUtil.opprettHoveddokumentForEnhetstest;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.KassasjonAvDokumentKnyttetFlereJPException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
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
public class TidligKassasjonServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;
	private static final String TITTEL = "Tittel";
	private static Journalpost journalpost = null;
	private static DokumentInfo dokumentInfo = null;
	private static final Begrensning begrensning =
			Begrensning.builder()
					.dokumentInfoId(DOKUMENTINFO_ID)
					.begrensningType(BegrensningTypeCode.KASSERT)
					.build();

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private BegrensningRepository begrensningRepository;
	@Mock
	private JoarkDeleteRepository deleteRepository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private TidligKassasjonService tidligKassasjonService;

	@Before
	public void setUp() {
		tidligKassasjonService = new TidligKassasjonService(dokumentinfoRepository, begrensningRepository, deleteRepository);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeTidligKassereDokument_hvisDokumentInfoIkkeErBegrensetSomKassert() {
		thrown.expect(BegrensningIkkeFunnetException.class);
		thrown.expectMessage(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				DOKUMENTINFO_ID,
				BegrensningTypeCode.KASSERT));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.empty());

		TidligKassasjonResponse response = tidligKassasjonService.tidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skalIkkeTidligKasserDokument_hvisDokumentErKnyttetFlereJournalposter() {
		thrown.expect(KassasjonAvDokumentKnyttetFlereJPException.class);
		thrown.expectMessage(String.format(
				"Kan ikke utføre tidlig kassasjon av dokument med dokumentInfoId=%s fordi dokumentet er knyttet til flere " +
						"journalposter og den funksjonaliteten er ikke implementert",
				DOKUMENTINFO_ID));

		Journalpost journalpost2 = opprettHoveddokumentForEnhetstest();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo, journalpost2);
		assertTrue(dokumentInfo.isRelatedToMultipleJournalposts());

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.of(begrensning));

		TidligKassasjonResponse response = tidligKassasjonService.tidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skallTidligKassereDokument_utenKastAvExceptions() {
		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.of(begrensning));

		TidligKassasjonResponse response = tidligKassasjonService.tidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

}
