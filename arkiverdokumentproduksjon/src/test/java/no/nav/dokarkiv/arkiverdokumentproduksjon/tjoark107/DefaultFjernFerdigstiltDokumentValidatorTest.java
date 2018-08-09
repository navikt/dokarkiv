package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DefaultFjernFerdigstiltDokumentValidatorTest {
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final Long UNKNOWN_DOKUMENTINFO_ID = 2L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	private FjernFerdigstiltDokumentValidator validator = new DefaultFjernFerdigstiltDokumentValidator();

	@Test
	public void shouldValidateOk() throws Exception {
		validator.validate(createJournalpost(ARKIV), createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldValidateInputOk() throws Exception {
		validator.validateInputRequest(createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_missingJournalpostIdInRequest() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("JournalpostId");
		validator.validateInputRequest(createRequest(0L, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_missingDokumentInfIdInRequest() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("DokumentInfoId");
		validator.validateInputRequest(createRequest(JOURNALPOST_ID, 0L, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_missingEndretAvInRequest() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("EndretAv");
		validator.validateInputRequest(createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ""));
	}

	@Test
	public void shouldThrowException_invalidJournalStatus() throws Exception {
		expectedException.expect(UgyldigJournalStatusVerdiException.class);
		Journalpost journalpostWithIllegalStatus = createJournalpost(ARKIV);
		journalpostWithIllegalStatus.setJournalstatus(JournalStatusCode.J);

		validator.validate(journalpostWithIllegalStatus, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_dokumentInfoNotFound() throws Exception {
		expectedException.expect(NoDokumentInfoFoundException.class);

		validator.validate(createJournalpost(ARKIV), createRequest(JOURNALPOST_ID, UNKNOWN_DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_dokumentIsAlreayUnderRedigering() throws Exception {
		expectedException.expect(UgyldigDokumentStatusVerdiException.class);
		expectedException.expectMessage("Under redigering");
		Journalpost journalpostUnderRedigering = createJournalpost(ARKIV);
		journalpostUnderRedigering.getDokumentInfoFromJpDokInfoRelasjoner(0).setDokumentstatus(UNDER_REDIGERING);

		validator.validate(journalpostUnderRedigering, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_dokumentIsAlreadyAvbrutt() throws Exception {
		expectedException.expect(UgyldigDokumentStatusVerdiException.class);
		expectedException.expectMessage("Avbrutt");
		Journalpost journalpostAvbrutt = createJournalpost(ARKIV);
		journalpostAvbrutt.getDokumentInfoFromJpDokInfoRelasjoner(0).setDokumentstatus(DokumentStatusCode.AVBRUTT);

		validator.validate(journalpostAvbrutt, createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowException_dokumentIsMissingArkivFildetaljer() throws Exception {
		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Cannot find a Fildetaljer with VariantFormat ARKIV");

		validator.validate(createJournalpost(PRODUKSJON), createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
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