package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Unit tests for {@link DefaultAvbrytVedleggValidator}
 *
 * @author Roar Bjurstrom, Visma Consulting
 */
public class DefaultAvbrytVedleggValidatorTest {

	private static final Long JOURNALPOST_ID = 42L;
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final String ENDRET_AV_NAVN = "endret_av";

	private DefaultAvbrytVedleggValidator validator = new DefaultAvbrytVedleggValidator();

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void shouldValidateRequest() throws Exception {
		validator.validateInputRequest(createRequest());
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsNull() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("JournalpostId cannot be empty or missing");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setJournalpostId(null);

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsZero() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("JournalpostId cannot be empty or missing");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setJournalpostId(0L);

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIdIsNull() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("DokumentInfoId cannot be empty or missing");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setDokumentInfoId(null);

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIdIsZero() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("DokumentInfoId cannot be empty or missing");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setDokumentInfoId(0L);

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsNull() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("EndretAvNavn cannot be empty or missing.");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setEndretAvNavn(null);

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsEmpty() {
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("EndretAvNavn cannot be empty or missing.");

		AvbrytVedleggRequestTo requestTo = createRequest();
		requestTo.setEndretAvNavn("");

		validator.validateInputRequest(requestTo);
	}

	@Test
	public void shouldValidateJournalpost() throws Exception {
		validator.validateJournalpost(createJournalpost(JournalStatusCode.D), JOURNALPOST_ID);
	}

	@Test
	public void shouldThrowNoJournalpostFoundException() throws Exception {
		thrown.expect(NoJournalpostFoundException.class);
		thrown.expectMessage("journalpostid=" + JOURNALPOST_ID + " does not exist");

		validator.validateJournalpost(null, JOURNALPOST_ID);
	}

	@Test
	public void shouldThrowUgyldigJournalStatusVerdiException() throws Exception {
		thrown.expect(UgyldigJournalStatusVerdiException.class);
		thrown.expectMessage("Invalid JournalStatus for journalpostid=" + JOURNALPOST_ID);

		validator.validateJournalpost(createJournalpost(JournalStatusCode.A), JOURNALPOST_ID);
	}

	@Test
	public void shouldValidateDokumentInfo() throws Exception {
		validator.validateDokumentInfo(createDokumentInfo(DokumentStatusCode.UNDER_REDIGERING), DOKUMENTINFO_ID);
	}

	@Test
	public void shouldThrowNoDokumentInfoFoundException() throws Exception {
		thrown.expect(NoDokumentInfoFoundException.class);
		thrown.expectMessage("Journalpost missing DokumentInfo with dokumentinfoid=" + DOKUMENTINFO_ID);

		validator.validateDokumentInfo(null, DOKUMENTINFO_ID);
	}

	@Test
	public void shouldThrowUgyldigDokumentstatisVerdiException() throws Exception {
		thrown.expect(UgyldigDokumentStatusVerdiException.class);
		thrown.expectMessage("dokumentinfoid=" + DOKUMENTINFO_ID + " is already Avbrutt");

		validator.validateDokumentInfo(createDokumentInfo(DokumentStatusCode.AVBRUTT), DOKUMENTINFO_ID);
	}

	@Test
	public void shouldValidateJournalpostDokumentInfoRelasjon() throws Exception {
		validator.validateJournalpostDokumentInfoRelasjon(createJournalpostDokumentInfoRelasjon(VEDLEGG));
	}

	@Test
	public void shouldUgyldigTilknyttetJournalpostSomVerdiException() throws Exception {
		thrown.expect(UgyldigTilknyttetJournalpostSomVerdiException.class);
		thrown.expectMessage("tilknyttetjournalpostsom=" + HOVEDDOKUMENT + " is not Vedlegg");

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = createJournalpostDokumentInfoRelasjon(HOVEDDOKUMENT);
		new Journalpost().addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
		validator.validateJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);
	}

	private JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon(TilknyttetJournalpostSomCode code) {
		return JournalpostDokumentInfoRelasjonBuilder
				.getJournalpostDokumentInfoRelasjonBuilder()
				.tilknyttetJournalpostSom(code)
				.dokumentInfo(getDokumentInfoBuilder().build())
				.build();
	}

	private DokumentInfo createDokumentInfo(DokumentStatusCode dokumentStatusCode) {
		return getDokumentInfoBuilder()
				.dokumentInfoId(DOKUMENTINFO_ID)
				.dokumentstatus(dokumentStatusCode)
				.build();
	}

	private Journalpost createJournalpost(JournalStatusCode journalStatusCode) {
		return getJournalpostBuilder()
				.journalStatus(journalStatusCode)
				.build();
	}

	private AvbrytVedleggRequestTo createRequest() {
		return new AvbrytVedleggRequestTo(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN);
	}
}
