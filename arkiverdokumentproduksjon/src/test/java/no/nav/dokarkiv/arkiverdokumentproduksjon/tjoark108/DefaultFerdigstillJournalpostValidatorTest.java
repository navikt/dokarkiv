package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108;

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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link DefaultFerdigstillJournalpostValidator}
 *
 * @author Stig Strøm
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class DefaultFerdigstillJournalpostValidatorTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	private FerdigstillJournalpostValidator validator = new DefaultFerdigstillJournalpostValidator();

	@Test
	public void shouldValidateInputRequest() {
		validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN,
				UtsendingsKanalCode.EESSI));
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenJournalpostIdIsMissing() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(new FerdigstillJournalpostRequestTo(null, ENDRET_AV_NAVN, UtsendingsKanalCode.EESSI)),
				"JournalpostId");
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenEndretAvNavnIsMissing() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, null, UtsendingsKanalCode.EESSI)),
				"EndretAvNavn");
	}

	@Test
	public void validateInputRequest_shouldThrowExceptionWhenUtsendingsKanalIsMissing() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(new FerdigstillJournalpostRequestTo(JOURNALPOST_ID, ENDRET_AV_NAVN, null)),
				"Utsendingskanal");
	}

	@Test
	public void shouldValidateJournalpost() {
		validator.validate(createJournalpost(ARKIV));
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionWhenMissingHoveddokument() {
		Journalpost journalpost = createJournalpost(ARKIV);
		JournalpostDokumentInfoRelasjon hovedDokumentRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		hovedDokumentRelasjon.setTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Cannot find hoveddokument");
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, names = {"D", "FS", "FL"})
	public void validateJournalpost_shouldPassIfJournalstatusIsAccepted(JournalStatusCode journalStatus) {
		Journalpost journalpostWithIllegalJournalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalJournalStatus.setJournalstatus(journalStatus);

		validator.validate(journalpostWithIllegalJournalStatus);
	}

	@ParameterizedTest
	@EnumSource(value = JournalStatusCode.class, mode = EnumSource.Mode.EXCLUDE, names = {"D", "FS", "FL", "A"})
	public void validateJournalpost_shouldThrowExceptionIfJournalstatusIsWrong(JournalStatusCode journalStatus) {
		Journalpost journalpostWithIllegalJournalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalJournalStatus.setJournalstatus(journalStatus);

		assertThrows(UgyldigJournalStatusVerdiException.class,
				() -> validator.validate(journalpostWithIllegalJournalStatus),
				"Expected one of Journalstatus.D or Journalstatus.FS or Journalstatus.FL");
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionIfADokumentIsUnderRedigering() {
		Journalpost journalpost = addVedlegg(createJournalpost(ARKIV));

		assertThrows(UgyldigDokumentStatusVerdiException.class,
				() -> validator.validate(journalpost),
				"Illegal dokument status for dokumentInfoId=" + DOKUMENTINFO_ID);
	}

	@Test
	public void validateJournalpost_shouldThrowExceptionIfMissingVariantFormatArkiv() {
		assertThrows(ApplicationException.class,
				() -> validator.validate(createJournalpost(PRODUKSJON)),
				"Found Fildetaljer without VariantFormat ARKIV for journalpostId=" + JOURNALPOST_ID);
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
