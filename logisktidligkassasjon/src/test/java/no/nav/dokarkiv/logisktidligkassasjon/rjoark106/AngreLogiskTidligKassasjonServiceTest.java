package no.nav.dokarkiv.logisktidligkassasjon.rjoark106;

import static no.nav.dokarkiv.logisktidligkassasjon.util.TestUtils.opprettHoveddokumentForEnhetstest;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.DokumentinfoRepository;
import no.nav.dokarkiv.logiskkassasjon.rjoark105.LogiskKassasjonResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class AngreLogiskTidligKassasjonServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private BegrensningService begrensningService;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private AngreLogiskTidligKassasjonService angreLogiskTidligKassasjonService;
	private static final Begrensning begrensning =
			Begrensning.builder()
					.dokumentInfoId(DOKUMENTINFO_ID)
					.begrensningType(SkjermingTypeCode.POL)
					.build();
	private AngreLogiskKassasjonService angreLogiskKassasjonService;
	private Journalpost journalpost;
	private DokumentInfo dokumentInfo;

	@Before
	public void setUp() {
		angreLogiskKassasjonService = new AngreLogiskKassasjonService(dokumentinfoRepository, begrensningService);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeAngreLogiskTidligKassereDokument_hvisDokumentInfoIdIkkeFinnesIRepoet() {
		thrown.expect(DokumentInfoIkkeFunnetException.class);
		thrown.expectMessage("Kan ikke finne dokumentInfo med dokumentInfoId=" + DOKUMENTINFO_ID);

		when(dokumentinfoRepository.findByDokumentInfoId(anyLong())).thenReturn(Optional.empty());

		angreLogiskTidligKassasjonService.angreLogiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test()
	public void skalIkkeAngreLogiskTidligKassereDokument_hvisDokumentInfoIkkeErBegrensetSomKassert() {
		thrown.expect(SkjermingIkkeFunnetException.class);
		thrown.expectMessage(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				DOKUMENTINFO_ID,
				SkjermingTypeCode.POL));

		Journalpost journalpost = opprettHoveddokumentForEnhetstest();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
				BegrensningTypeCode.POL));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningService.isDokumentInfoIdKassert(DOKUMENTINFO_ID))
				.thenReturn(false);

		angreLogiskTidligKassasjonService.angreLogiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test()
	public void skalAngreLogiskTidligKassereDokument_utenKastAvExceptions() {
		Journalpost journalpost = opprettHoveddokumentForEnhetstest();
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningService.isDokumentInfoIdKassert(DOKUMENTINFO_ID))
				.thenReturn(true);


		angreLogiskTidligKassasjonService.angreLogiskTidligKassasjonAvDokument(DOKUMENTINFO_ID);
	}

}
