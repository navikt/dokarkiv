package no.nav.dokarkiv.fysisktidligkassasjon.rjoark107;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.fysisktidligkassasjon.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysisktidligkassasjon.util.TestUtil.opprettHoveddokumentForEnhetstest;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.SkjermingIkkeFunnetException;
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
public class FysiskTidligKassasjonServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;
	private static final String TITTEL = "Tittel";
	private static Journalpost journalpost = null;
	private static DokumentInfo dokumentInfo = null;
	private static final Begrensning begrensning =
			Begrensning.builder()
					.dokumentInfoId(DOKUMENTINFO_ID)
					.begrensningType(SkjermingTypeCode.POL)
					.build();

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private BegrensningRepository begrensningRepository;
	@Mock
	private JoarkDeleteRepository deleteRepository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private FysiskTidligKassasjonService fysiskTidligKassasjonService;

	@Before
	public void setUp() {
		fysiskTidligKassasjonService = new FysiskTidligKassasjonService(dokumentinfoRepository, begrensningRepository, deleteRepository);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeTidligKassereDokument_hvisDokumentInfoIkkeErBegrensetSomKassert() {
		thrown.expect(SkjermingIkkeFunnetException.class);
		thrown.expectMessage(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				DOKUMENTINFO_ID,
				SkjermingTypeCode.POL));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.empty());

		FysiskTidligKassasjonResponse response = fysiskTidligKassasjonService.fysiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skalTidligKasserDokument_medDokumentKnyttetFlereJournalposter() {
		Journalpost journalpost2 = opprettHoveddokumentForEnhetstest();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo, journalpost2);
		assertTrue(dokumentInfo.isRelatedToMultipleJournalposts());

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.of(begrensning));

		FysiskTidligKassasjonResponse response = fysiskTidligKassasjonService.fysiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skallTidligKassereDokument_medDokumentKnyttetEnJournalpost() {
		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.of(begrensning));

		FysiskTidligKassasjonResponse response = fysiskTidligKassasjonService.fysiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}
}
