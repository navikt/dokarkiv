package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DefaultFjernFerdigstiltDokumentValidatorTest {
	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final Long UNKNOWN_DOKUMENTINFO_ID = 2L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	private final FjernFerdigstiltDokumentValidator validator = new DefaultFjernFerdigstiltDokumentValidator();

	@Test
	public void shouldValidateOk() {
		validator.validate(createJournalpost(ARKIV), createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldValidateInputOk() {
		validator.validateInputRequest(createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_missingJournalpostIdInRequest() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(createRequest(0L, DOKUMENTINFO_ID, ENDRET_AV_NAVN)),
				"JournalpostId");
	}

	@Test
	public void shouldThrowException_missingDokumentInfIdInRequest() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(createRequest(JOURNALPOST_ID, 0L, ENDRET_AV_NAVN)),
				"DokumentInfoId");
	}

	@Test
	public void shouldThrowException_missingEndretAvInRequest() {
		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, "")),
				"EndretAv");
	}

	@Test
	public void shouldThrowException_invalidJournalStatus() {
		Journalpost journalpostWithIllegalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalStatus.setJournalstatus(JournalStatusCode.J);

		assertThrows(UgyldigJournalStatusVerdiException.class,
				() -> validator.validate(journalpostWithIllegalStatus, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN)));
	}

	@Test
	public void shouldThrowException_dokumentInfoNotFound() {
		assertThrows(NoDokumentInfoFoundException.class,
				() -> validator.validate(createJournalpost(ARKIV), createRequest(JOURNALPOST_ID, UNKNOWN_DOKUMENTINFO_ID, ENDRET_AV_NAVN)));
	}

	@Test
	public void shouldThrowException_dokumentIsAlreayUnderRedigering() {
		Journalpost journalpostUnderRedigering = createJournalpost(ARKIV);
		journalpostUnderRedigering.getDokumentInfoFromJpDokInfoRelasjoner(0).setDokumentstatus(UNDER_REDIGERING);

		assertThrows(UgyldigDokumentStatusVerdiException.class,
				() -> validator.validate(journalpostUnderRedigering, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN)),
				"Under redigering");
	}

	@Test
	public void shouldThrowException_dokumentIsAlreadyAvbrutt() {
		Journalpost journalpostAvbrutt = createJournalpost(ARKIV);
		journalpostAvbrutt.getDokumentInfoFromJpDokInfoRelasjoner(0).setDokumentstatus(DokumentStatusCode.AVBRUTT);

		assertThrows(UgyldigDokumentStatusVerdiException.class,
				() -> validator.validate(journalpostAvbrutt, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN)),
				"Avbrutt");
	}

	@Test
	public void shouldThrowException_dokumentIsMissingArkivFildetaljer() {
		assertThrows(ApplicationException.class,
				() -> validator.validate(createJournalpost(PRODUKSJON), createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN)),
				"Cannot find a Fildetaljer with VariantFormat ARKIV");
	}

	private FjernFerdigstiltDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId, String endretAv) {
		return new FjernFerdigstiltDokumentRequestTo(journalpostId, dokumentInfoId, endretAv);
	}


	private Journalpost createJournalpost(VariantFormatCode variantFormat) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalStatus(JournalStatusCode.D)
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

}