package no.nav.dokarkiv.journalfoerinngaaende.v1.service.support;

import static no.nav.dokarkiv.journalfoerinngaaende.v1.service.support.JournalpostValidator.validateJournalpostStatuser;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.service.support.JournalpostValidator.validateJournalpostStrukturOgPaakrevdeAttributter;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.createJournalpostDokumentinfoRelasjon1;
import static no.nav.dokarkiv.journalfoerinngaaende.v1.util.TestUtils.createJournalpostForOppdatering;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentUnderRedigeringException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeMidlertidigException;
import no.nav.dokarkiv.core.exceptions.KunneIkkeEndeligJournalfoereException;
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

        expectExceptionWithMessage(JournalpostIkkeInngaaendeException.class);
	}

	@Test
	public void shouldFailHvisJournalpostIkkeErMidlertidigJournalfoert() {
		journalpost.setJournalstatus(JournalStatusCode.J);

        expectExceptionWithMessage(JournalpostIkkeMidlertidigException.class);
	}

	@Test
	public void shouldFailHvisSaksrelasjonErFeilregistrert() {
		journalpost.getSaksrelasjon().setFeilregistrert(Boolean.TRUE);

        expectExceptionWithMessage(JournalpostIkkeMidlertidigException.class);
	}

	@Test
	public void shouldFailWhenDokumentinfoErUnderRedigering() {
		journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

        expectExceptionWithMessage(DokumentUnderRedigeringException.class);
	}

	@Test
	public void shouldFailHvisJournalpostIkkeInneholderNoenHoveddokument() {
		journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

        expectExceptionWithMessage(KunneIkkeEndeligJournalfoereException.class);
	}

	@Test
	public void shouldFailHvisJournalpostIkkeInneholderKunEttHoveddokument() {
		journalpost.addJournalpostDokumentInfoRelasjon(createJournalpostDokumentinfoRelasjon1());

        expectExceptionWithMessage(KunneIkkeEndeligJournalfoereException.class);
	}

	@Test
	public void shouldFailHvisFildetaljerManglerVariantFormatArkiv() {
		journalpost.findAllFilDetaljer().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.SLADDET));

        expectExceptionWithMessage(KunneIkkeEndeligJournalfoereException.class);
	}

	@Test
	public void shouldFailHvisFildetaljerHarSammeVariantFormat() {
		journalpost.findAllFilDetaljer().forEach(filDetaljer -> filDetaljer.setVariantFormat(VariantFormatCode.ARKIV));

        expectExceptionWithMessage(KunneIkkeEndeligJournalfoereException.class);
    }


    private void expectExceptionWithMessage(Class exceptionClass) {
        expectedException.expect(exceptionClass);

		validateJournalpostStatuser(journalpost);
		validateJournalpostStrukturOgPaakrevdeAttributter(journalpost);
	}
}