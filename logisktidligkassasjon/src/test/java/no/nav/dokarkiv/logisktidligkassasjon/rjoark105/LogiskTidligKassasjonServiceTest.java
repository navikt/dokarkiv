package no.nav.dokarkiv.logisktidligkassasjon.rjoark105;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.opprettHoveddokumentForEnhetstest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentAlleredeKassertException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class LogiskTidligKassasjonServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;
	private static final String TITTEL = "Tittel";
	private static final Begrensning begrensning =
			Begrensning.builder()
					.dokumentInfoId(DOKUMENTINFO_ID)
					.begrensningType(SkjermingTypeCode.POL)
					.build();

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private BegrensningRepository begrensningRepository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private LogiskTidligKassasjonService logiskTidligKassasjonService;

	@Before
	public void setUp() {
		logiskTidligKassasjonService = new LogiskTidligKassasjonService(dokumentinfoRepository, begrensningRepository);
	}

	@Test()
	public void skalIkkeLogiskTidligKassereDokument_hvisDokumentInfoAlleredeErKassert() {
		thrown.expect(DokumentAlleredeKassertException.class);
		thrown.expectMessage(String.format(
				"Kan ikke utføre logisk tidlig kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk tidlig kassert",
				DOKUMENTINFO_ID));

		Journalpost journalpost = opprettHoveddokumentForEnhetstest();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.of(begrensning));

		logiskTidligKassasjonService.logiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skalLogiskTidligKasserDokument_medDokumentKnyttetEnJournalpost() {
		Journalpost journalpost = opprettHoveddokumentForEnhetstest();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.empty());

		LogiskTidligKassasjonResponse response = logiskTidligKassasjonService.logiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);

		assertThat("Wrong tittel", response.getTittel(), is(TITTEL));
	}

	@Test
	public void skalLogiskTidligKasserDokument_medDokumentKnyttetFlereJournalposter() {
		Journalpost journalpost = opprettHoveddokumentForEnhetstest();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		Journalpost journalpost2 = opprettHoveddokumentForEnhetstest();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo, journalpost2);
		assertTrue(dokumentInfo.isRelatedToMultipleJournalposts());

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, SkjermingTypeCode.POL))
				.thenReturn(Optional.empty());

		logiskTidligKassasjonService.logiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}
}
