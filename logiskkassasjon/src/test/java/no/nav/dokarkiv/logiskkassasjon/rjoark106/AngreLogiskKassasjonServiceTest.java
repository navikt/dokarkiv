package no.nav.dokarkiv.logiskkassasjon.rjoark106;

import static no.nav.dokarkiv.logiskkassasjon.util.TestUtils.opprettHoveddokumentForEnhetstest;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.BegrensningIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.repository.BegrensningRepository;
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
public class AngreLogiskKassasjonServiceTest {

	private static final Long DOKUMENTINFO_ID = 2000000L;

	@Mock
	private DokumentinfoRepository dokumentinfoRepository;
	@Mock
	private BegrensningRepository begrensningRepository;
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private AngreLogiskKassasjonService angreLogiskKassasjonService;
	private Journalpost journalpost;
	private DokumentInfo dokumentInfo;
	private static final Begrensning begrensning =
			Begrensning.builder()
					.dokumentInfoId(DOKUMENTINFO_ID)
					.begrensningType(BegrensningTypeCode.KASSERT)
					.build();

	@Before
	public void setUp() {
		angreLogiskKassasjonService = new AngreLogiskKassasjonService(dokumentinfoRepository, begrensningRepository);
		journalpost = opprettHoveddokumentForEnhetstest();
		dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
	}

	@Test()
	public void skalIkkeAngreLogiskKassereDokument_hvisDokumentInfoIdIkkeFinnesIRepoet() {
		thrown.expect(DokumentInfoIkkeFunnetException.class);
		thrown.expectMessage("Kan ikke finne dokumentInfo med dokumentInfoId=" + DOKUMENTINFO_ID);

		when(dokumentinfoRepository.findByDokumentInfoId(anyLong())).thenReturn(Optional.empty());

		LogiskKassasjonResponse response = angreLogiskKassasjonService.angreLogiskKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test()
	public void skalIkkeAngreLogiskKassereDokument_hvisDokumentInfoIkkeErBegrensetSomKassert() {
		thrown.expect(BegrensningIkkeFunnetException.class);
		thrown.expectMessage(String.format(
				"Fant ikke forventet begrensning for dokument med dokumentInfoId=%s og begrensningsType=%s",
				DOKUMENTINFO_ID,
				BegrensningTypeCode.KASSERT));

		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.empty());

		LogiskKassasjonResponse response = angreLogiskKassasjonService.angreLogiskKassasjonAvDokument(DOKUMENTINFO_ID);
	}

	@Test()
	public void skalAngreLogiskKassereDokument_utenKastAvExceptions() {
		when(dokumentinfoRepository.findByDokumentInfoId(DOKUMENTINFO_ID)).thenReturn(Optional.of(dokumentInfo));
		when(begrensningRepository.findByDokumentInfoIdAndBegrensningType(DOKUMENTINFO_ID, BegrensningTypeCode.KASSERT))
				.thenReturn(Optional.of(begrensning));

		LogiskKassasjonResponse response = angreLogiskKassasjonService.angreLogiskKassasjonAvDokument(DOKUMENTINFO_ID);
	}

}
