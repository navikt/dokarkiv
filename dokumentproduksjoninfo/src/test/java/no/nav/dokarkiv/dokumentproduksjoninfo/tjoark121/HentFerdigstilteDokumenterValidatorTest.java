package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.JournalpostNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link HentFerdigstilteDokumenterValidator}.
 *
 * @author Stig Strøm
 */
@ExtendWith(MockitoExtension.class)
public class HentFerdigstilteDokumenterValidatorTest {

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 2L;
	private static final JournalStatusCode INVALID_JOURNALSTATUS = JournalStatusCode.D;
	private static final JournalStatusCode VALID_JOURNALSTATUS = JournalStatusCode.FS;
	private static final FilTypeCode VALID_FILTYPE = FilTypeCode.PDF;
	private static final FilTypeCode INVALID_FILTYPE = FilTypeCode.XML;
	private static final DokumentStatusCode INVALID_DOKUMENT_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final DokumentStatusCode VALID_DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;

	private HentFerdigstilteDokumenterValidator validator = new HentFerdigstilteDokumenterValidator();


	@Test
	public void shouldValidateJournalpost() {
		Journalpost journalpost = getJournalpostBuilder().journalStatus(VALID_JOURNALSTATUS).build();
		validator.validateJournalpost(JOURNALPOST_ID, journalpost);
	}

	@Test
	public void shouldThrowException_journalpostIsNull() {
		assertThrows(JournalpostNotFoundException.class,
				() -> validator.validateJournalpost(JOURNALPOST_ID, null),
				"journalpostId=" + JOURNALPOST_ID + " eksisterer ikke");
	}

	@Test
	public void shouldThrowException_journalstatusIsNotFS() {
		Journalpost journalpost = getJournalpostBuilder().journalStatus(INVALID_JOURNALSTATUS).build();

		assertThrows(IllegalJournalStatusException.class,
				() -> validator.validateJournalpost(JOURNALPOST_ID, journalpost),
				"journalpostId=" + JOURNALPOST_ID
						+ " forventet JournalStatus FS, men har journalStatus=" + INVALID_JOURNALSTATUS);
	}

	@Test
	public void shouldValidateDokumentInfo() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().dokumentstatus(VALID_DOKUMENT_STATUS).build();
		validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, dokumentInfo);
	}

	@Test
	public void shouldThrowException_dokumentInfoIsNull() {
		assertThrows(DokumentInfoNotFoundException.class,
				() -> validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, null),
				"dokumentInfoId=" + DOKUMENT_INFO_ID
						+ " hører ikke til journalpost med journalpostId=" + JOURNALPOST_ID);
	}

	@Test
	public void shouldThrowException_dokumentInfoIsNotFerdigstilt() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().dokumentstatus(INVALID_DOKUMENT_STATUS).build();
		assertThrows(IllegalDokumentstatusException.class,
				() -> validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, dokumentInfo),
				"dokumentInfoId=" + DOKUMENT_INFO_ID + " som tilhører journalpostId=" + JOURNALPOST_ID
						+ " er ikke ferdigstilt");
	}

	@Test
	public void shouldValidateFildetaljer() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder().filtype(VALID_FILTYPE).build();
		validator.validateFildetaljer(DOKUMENT_INFO_ID, filDetaljer);
	}

	@Test
	public void shouldThrowException_filDetaljerIsNull() {
		assertThrows(IllegalVariantFormatException.class,
				() -> validator.validateFildetaljer(DOKUMENT_INFO_ID, null),
				"dokumentInfoId=" + DOKUMENT_INFO_ID + " mangler variantformat arkiv");
	}

	@Test
	public void shouldThrowException_dokumentIsNotAPdf() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder().filtype(INVALID_FILTYPE).build();

		assertThrows(IllegalVariantFormatException.class,
				() -> validator.validateFildetaljer(DOKUMENT_INFO_ID, filDetaljer),
				"er ikke av type PDF/PDFA");
	}

}
