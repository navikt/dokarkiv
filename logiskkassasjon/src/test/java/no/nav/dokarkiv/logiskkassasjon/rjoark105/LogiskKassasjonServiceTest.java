package no.nav.dokarkiv.logiskkassasjon.rjoark105;

import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForEnhetstest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
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
public class LogiskKassasjonServiceTest {

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
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private LogiskKassasjonService logiskKassasjonService;

	@Before
	public void setUp() {
		logiskKassasjonService = new LogiskKassasjonService(dokumentinfoRepository, begrensningRepository);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeLogiskKassereDokument_hvisDokumentInfoAlleredeErKassert() {
		thrown.expect(DokumentAlleredeKassertException.class);
		thrown.expectMessage(String.format(
				"Kan ikke utføre logisk kassasjon av dokument med dokumentInfoId=%s. Dokumentet er allerede logisk kassert",
				DOKUMENTINFO_ID));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.of(begrensning));

		LogiskKassasjonResponse response = logiskKassasjonService.logiskKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test
	public void skalLogiskKasserDokument_medDokumentKnyttetEnJournalpost() {
		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.empty());

		LogiskKassasjonResponse response = logiskKassasjonService.logiskKassasjonAvDokument(DOKUMENTINFO_ID);

		assertThat("Wrong tittel", response.getTittel(), is(TITTEL));
	}

	@Test
	public void skalLogiskKasserDokument_medDokumentKnyttetFlereJournalposter() {
		Journalpost journalpost2 = opprettHoveddokumentForEnhetstest();
		knyttDokumentInfoSomVedleggTilJournalpostForIT(dokumentInfo, journalpost2);
		assertTrue(dokumentInfo.isRelatedToMultipleJournalposts());

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.empty());

		LogiskKassasjonResponse response = logiskKassasjonService.logiskKassasjonAvDokument(DOKUMENTINFO_ID);
	}
}
