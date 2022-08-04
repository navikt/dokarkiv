package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106;

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
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

	@Test
	public void shouldValidateRequest() {
		validator.validateInputRequest(createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, ENDRET_AV_NAVN));
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsNull() {
		AvbrytVedleggRequestTo requestTo = createRequest(null, DOKUMENTINFO_ID, ENDRET_AV_NAVN);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"JournalpostId cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfJournalpostIdIsZero() {
		AvbrytVedleggRequestTo requestTo = createRequest(0L, DOKUMENTINFO_ID, ENDRET_AV_NAVN);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"JournalpostId cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIdIsNull() {
		AvbrytVedleggRequestTo requestTo = createRequest(JOURNALPOST_ID, null, ENDRET_AV_NAVN);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"DokumentInfoId cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfDokumentInfoIdIsZero() {
		AvbrytVedleggRequestTo requestTo = createRequest(JOURNALPOST_ID, 0L, ENDRET_AV_NAVN);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"DokumentInfoId cannot be empty or missing");
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsNull() {
		AvbrytVedleggRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, null);

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"EndretAvNavn cannot be empty or missing.");
	}

	@Test
	public void shouldThrowExceptionIfEndretAvIsEmpty() {
		AvbrytVedleggRequestTo requestTo = createRequest(JOURNALPOST_ID, DOKUMENTINFO_ID, "");

		assertThrows(IllegalArgumentException.class,
				() -> validator.validateInputRequest(requestTo),
				"EndretAvNavn cannot be empty or missing.");
	}

	@Test
	public void shouldValidateJournalpost() {
		validator.validateJournalpost(createJournalpost(JournalStatusCode.D), JOURNALPOST_ID);
	}

	@Test
	public void shouldThrowNoJournalpostFoundException() {
		assertThrows(NoJournalpostFoundException.class,
				() -> validator.validateJournalpost(null, JOURNALPOST_ID),
				"journalpostid=" + JOURNALPOST_ID + " does not exist");
	}

	@Test
	public void shouldThrowUgyldigJournalStatusVerdiException() {
		assertThrows(UgyldigJournalStatusVerdiException.class,
				() -> validator.validateJournalpost(createJournalpost(JournalStatusCode.A), JOURNALPOST_ID),
				"Invalid JournalStatus for journalpostid=" + JOURNALPOST_ID);
	}

	@Test
	public void shouldValidateDokumentInfo() {
		validator.validateDokumentInfo(createDokumentInfo(DokumentStatusCode.UNDER_REDIGERING), DOKUMENTINFO_ID);
	}

	@Test
	public void shouldThrowNoDokumentInfoFoundException() {
		assertThrows(NoDokumentInfoFoundException.class,
				() -> validator.validateDokumentInfo(null, DOKUMENTINFO_ID),
				"Journalpost missing DokumentInfo with dokumentinfoid=" + DOKUMENTINFO_ID);
	}

	@Test
	public void shouldThrowUgyldigDokumentstatisVerdiException() {
		assertThrows(UgyldigDokumentStatusVerdiException.class,
				() -> validator.validateDokumentInfo(createDokumentInfo(DokumentStatusCode.AVBRUTT), DOKUMENTINFO_ID),
				"dokumentinfoid=" + DOKUMENTINFO_ID + " is already Avbrutt");
	}

	@Test
	public void shouldValidateJournalpostDokumentInfoRelasjon() {
		validator.validateJournalpostDokumentInfoRelasjon(createJournalpostDokumentInfoRelasjon(VEDLEGG));
	}

	@Test
	public void shouldUgyldigTilknyttetJournalpostSomVerdiException() {
		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = createJournalpostDokumentInfoRelasjon(HOVEDDOKUMENT);
		new Journalpost().addJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon);

		assertThrows(UgyldigTilknyttetJournalpostSomVerdiException.class,
				() -> validator.validateJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon),
				"tilknyttetjournalpostsom=" + HOVEDDOKUMENT + " is not Vedlegg");
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

	private AvbrytVedleggRequestTo createRequest(Long journalpostId, Long dokumentInfoId, String endretAvNavn) {
		return new AvbrytVedleggRequestTo(journalpostId, dokumentInfoId, endretAvNavn);
	}
}
