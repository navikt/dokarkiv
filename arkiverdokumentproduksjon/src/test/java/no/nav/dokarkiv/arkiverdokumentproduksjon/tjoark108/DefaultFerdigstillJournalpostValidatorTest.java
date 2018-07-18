package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.collection.IsIn.isIn;
import static org.junit.Assume.assumeThat;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

/**
 * Test for {@link DefaultFerdigstillJournalpostValidator}
 *
 * @author Stig Strøm
 * @author Thomas Kåsene, Visma Consulting AS
 */
@RunWith(Theories.class)
public class DefaultFerdigstillJournalpostValidatorTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	private FerdigstillJournalpostValidator validator = new DefaultFerdigstillJournalpostValidator();

	@DataPoints
	public static JournalStatusCode[] journalStatusCodes = JournalStatusCode.values();

	@Test
	public void shouldValidateInputRequest() throws Exception {
		validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN,
				UtsendingsKanalCode.ALTINN));
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenJournalpostIdIsMissing() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostId");
		validator.validateInputRequest(new FerdigstillJournalpostRequestTo(null, ENDRET_AV_NAVN, UtsendingsKanalCode.ALTINN));
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenEndretAvNavnIsMissing() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("EndretAvNavn");
		validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, null, UtsendingsKanalCode.ALTINN));
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenUtsendingsKanalIsMissing() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("Utsendingskanal");
		validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN, null));
	}

	@Test
	public void shouldValidateJournalpost() throws Exception {
		validator.validate(createJournalpost(ARKIV));
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionWhenMissingHoveddokument() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Cannot find hoveddokument");
		Journalpost journalpost = createJournalpost(ARKIV);
		JournalpostDokumentInfoRelasjon hovedDokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hovedDokumentRelasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		validator.validate(journalpost);
	}

	@Theory
	public void validateJournalpost_shouldPassIfJournalstatusIsAccepted(JournalStatusCode journalStatus) throws Exception {
		JournalStatusCode[] acceptedJournalStatusCodes = {D, FS, FL};
		assumeThat(journalStatus, isIn(acceptedJournalStatusCodes));

		Journalpost journalpostWithIllegalJournalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalJournalStatus.setJournalstatus(journalStatus);

		validator.validate(journalpostWithIllegalJournalStatus);
	}

	@Theory
	public void validateJournalpost_shouldThrowExceptionIfJournalstatusIsWrong(JournalStatusCode journalStatus) throws Exception {
		assumeThat(journalStatus, is(not(D)));
		assumeThat(journalStatus, is(not(FS)));
		assumeThat(journalStatus, is(not(FL)));

		Journalpost journalpostWithIllegalJournalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalJournalStatus.setJournalstatus(journalStatus);

		expectedException.expect(UgyldigJournalStatusVerdiException.class);
		expectedException.expectMessage("Expected one of Journalstatus.D or Journalstatus.FS or Journalstatus.FL");

		validator.validate(journalpostWithIllegalJournalStatus);
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionIfADokumentIsUnderRedigering() throws Exception {
		expectedException.expect(UgyldigDokumentStatusVerdiException.class);
		expectedException.expectMessage("Illegal dokument status for dokumentInfoId=" + DOKUMENTINFO_ID);
		Journalpost journalpost = addVedlegg(createJournalpost(ARKIV));

		validator.validate(journalpost);
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionIfMissingVariantFormatArkiv() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Found Fildetaljer without VariantFormat ARKIV for journalpostId=" + JOURNALPOST_ID);


		validator.validate(createJournalpost(PRODUKSJON));
	}

	private Journalpost createJournalpost(VariantFormatCode variantFormat) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalStatus(D)
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(variantFormat).build()).build())
								.build()).build();
	}

	private Journalpost addVedlegg(Journalpost journalpost) {
		JournalpostDokumentInfoRelasjon vedlegg = getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.dokumentInfo(
						getDokumentInfoBuilder()
								.dokumentInfoId(DOKUMENTINFO_ID)
								.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
								.filDetaljerList(
										getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
												.fileContent("file".getBytes())
												.variantFormat(VariantFormatCode.ARKIV).build()).build()).build();
		journalpost.addJournalpostDokumentInfoRelasjon(vedlegg);
		return journalpost;
	}
}
