package no.nav.dokarkiv.journalfoerInngaaende.v1.service.support;

import static no.nav.dokarkiv.journalfoerInngaaende.v1.service.support.JournalpostValidator.validateJournalpostStatuser;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.service.support.JournalpostValidator.validateJournalpostStrukturOgPaakrevdeAttributter;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.createJournalpostDokumentinfoRelasjon1;
import static no.nav.dokarkiv.journalfoerInngaaende.v1.util.TestUtils.createJournalpostForOppdatering;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokarkivRestFunctionalException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class JournalpostValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private Journalpost journalpost;

	@Before
	public void setUp() throws Exception {
		journalpost = createJournalpostForOppdatering();
	}

	@Test
	public void happyPath() {
		validateJournalpostStatuser(journalpost);
		validateJournalpostStrukturOgPaakrevdeAttributter(journalpost);
	}

	@Test
	public void shouldFailHvisJournalpostTypeIkkeErInngaaende() {
		journalpost.setJournalposttype(JournalpostTypeCode.U);

		expectExceptionWithMessage("Journalpost er ikke av type Inngaaende");
	}

	@Test
	public void shouldFailHvisJournalpostIkkeErMidlertidigJournalfoert() {
		journalpost.setJournalstatus(JournalStatusCode.J);

		expectExceptionWithMessage("Journalposten er ikke midlertidig journalført");
	}

	@Test
	public void shouldFailHvisSaksrelasjonErFeilregistrert() {
		journalpost.getSaksrelasjon().setFeilregistrert(Boolean.TRUE);

		expectExceptionWithMessage("Journalposten er ikke midlertidig journalført");
	}

	@Test
	public void shouldFailWhenDokumentinfoErUnderRedigering() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		expectExceptionWithMessage("Ett eller flere av dokumentene som forsøkes oppdatert er ikke ferdigstilt");
	}

	@Test
	public void shouldFailHvisJournalpostIkkeInneholderNoenHoveddokument() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		expectExceptionWithMessage("Journalpost inneholder ikke ett hoveddokument");
	}

	@Test
	public void shouldFailHvisJournalpostIkkeInneholderKunEttHoveddokument() {
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon1());

		expectExceptionWithMessage("Journalpost inneholder ikke ett hoveddokument");
	}

	@Test
	public void shouldFailHvisFildetaljerManglerVariantFormatArkiv() {
		journalpost.findAllFilDetaljer().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.SLADDET));

		expectExceptionWithMessage("Det mangler arkivvariant, dette er påkrevd for å ferdigstille journalposter");
	}

	@Test
	public void shouldFailHvisFildetaljerHarSammeVariantFormat() {
		journalpost.findAllFilDetaljer().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.ARKIV));
		
		expectExceptionWithMessage("Journalpost inneholder flere fildetaljer med samme variantformat");
	}

	private void expectExceptionWithMessage(String message) {
		expectedException.expect(DokarkivRestFunctionalException.class);
		expectedException.expectMessage(message);

		validateJournalpostStatuser(journalpost);
		validateJournalpostStrukturOgPaakrevdeAttributter(journalpost);
	}
}