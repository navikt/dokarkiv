package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;

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
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for {@link HentFerdigstilteDokumenterValidator}.
 *
 * @author Stig Strøm
 */
@RunWith(MockitoJUnitRunner.class)
public class HentFerdigstilteDokumenterValidatorTest {
		
	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 2L;
	private static final JournalStatusCode INVALID_JOURNALSTATUS = JournalStatusCode.D;
	private static final JournalStatusCode VALID_JOURNALSTATUS = JournalStatusCode.FS;
	private static final FilTypeCode VALID_FILTYPE = FilTypeCode.PDF;
	private static final FilTypeCode INVALID_FILTYPE = FilTypeCode.XML;
	private static final DokumentStatusCode INVALID_DOKUMENT_STATUS = DokumentStatusCode.UNDER_REDIGERING;
	private static final DokumentStatusCode VALID_DOKUMENT_STATUS = DokumentStatusCode.FERDIGSTILT;
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private HentFerdigstilteDokumenterValidator validator = new HentFerdigstilteDokumenterValidator();
	
	
	@Test
	public void shouldValidateJournalpost() throws Exception {
		Journalpost journalpost = getJournalpostBuilder().journalStatus(VALID_JOURNALSTATUS).build();
		validator.validateJournalpost(JOURNALPOST_ID, journalpost);
	}

	@Test
	public void shouldThrowException_journalpostIsNull() throws Exception {
		expected.expect(JournalpostNotFoundException.class);
		expected.expectMessage("journalpostId=" + JOURNALPOST_ID + " eksisterer ikke");
		validator.validateJournalpost(JOURNALPOST_ID, null);
	}
	
	@Test
	public void shouldThrowException_journalstatusIsNotFS() throws Exception {
		expected.expect(IllegalJournalStatusException.class);
		expected.expectMessage("journalpostId=" + JOURNALPOST_ID
				+ " forventet JournalStatus FS, men har journalStatus=" + INVALID_JOURNALSTATUS);
		Journalpost journalpost = getJournalpostBuilder().journalStatus(INVALID_JOURNALSTATUS).build();
		
		validator.validateJournalpost(JOURNALPOST_ID, journalpost);
	}

	@Test
	public void shouldValidateDokumentInfo() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().dokumentstatus(VALID_DOKUMENT_STATUS).build();
		validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, dokumentInfo);
	}
	
	@Test
	public void shouldThrowException_dokumentInfoIsNull() throws Exception {
		expected.expect(DokumentInfoNotFoundException.class);
		expected.expectMessage("dokumentInfoId=" + DOKUMENT_INFO_ID
					+ " hører ikke til journalpost med journalpostId=" + JOURNALPOST_ID);
		validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, null);
	}
	
	@Test
	public void shouldThrowException_dokumentInfoIsNotFerdigstilt() throws Exception {
		expected.expect(IllegalDokumentstatusException.class);
		expected.expectMessage("dokumentInfoId=" + DOKUMENT_INFO_ID + " som tilhører journalpostId=" + JOURNALPOST_ID
				+ " er ikke ferdigstilt");
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().dokumentstatus(INVALID_DOKUMENT_STATUS).build();
		validator.validateDokumentInfo(JOURNALPOST_ID, DOKUMENT_INFO_ID, dokumentInfo);

	}
	
	
	@Test
	public void shouldValidateFildetaljer() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder().filtype(VALID_FILTYPE).build();
		validator.validateFildetaljer(DOKUMENT_INFO_ID, filDetaljer);
	}
	
	@Test
	public void shouldThrowException_filDetaljerIsNull() throws Exception {
		expected.expect(IllegalVariantFormatException.class);
		expected.expectMessage("dokumentInfoId=" + DOKUMENT_INFO_ID + " mangler variantformat arkiv");
		validator.validateFildetaljer(DOKUMENT_INFO_ID, null);
	}
	
	@Test
	public void shouldThrowException_dokumentIsNotAPdf() throws Exception {
		expected.expect(IllegalVariantFormatException.class);
		expected.expectMessage("er ikke av type PDF/PDFA");
		FilDetaljer filDetaljer = getFilDetaljerBuilder().filtype(INVALID_FILTYPE).build();
		validator.validateFildetaljer(DOKUMENT_INFO_ID, filDetaljer);
	}
	
}
