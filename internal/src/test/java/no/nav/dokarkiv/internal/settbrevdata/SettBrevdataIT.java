package no.nav.dokarkiv.internal.settbrevdata;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.internal.AbstractInternalIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.util.TestdataFactory.FIL;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFerdigstiltJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestdataFactory.createReservertPensjonJournalpost;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdataController.INTERN_ROLE_BREVSERVER;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdataController.VARIANT_FORMAT_ARKIV;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdataController.VARIANT_FORMAT_PRODUKSJON;
import static no.nav.dokarkiv.internal.settbrevdata.SettBrevdataValidator.APPLICATION_RTF;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNSUPPORTED_MEDIA_TYPE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

public class SettBrevdataIT extends AbstractInternalIT {
	private static final String BREVSERVER = "itest:teamdokumenthandtering:brevserver";
	private static final byte[] OPPDATERT_FIL = "Oppdatert dokument".getBytes();
	private static final String SETT_BREVDATA_PATH = "journalpost/%s/settBrevdata";
	public static final String ENDRET_AV_BREVSERVER = "teamdokumenthandtering:brevserver";

	@Test
	void shouldSettBrevdataForArkiv() {
		String arkivFilUuid = UUID.randomUUID().toString();
		String produksjonFilUuid = UUID.randomUUID().toString();
		Journalpost journalpost = createReservertPensjonJournalpost(arkivFilUuid, produksjonFilUuid);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders());
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(CREATED);

		DokumentFil arkivDokumentFil = dokumentFilTestRepository.findByFilUuid(arkivFilUuid);
		assertThat(arkivDokumentFil.getFil()).isEqualTo(FIL);
		DokumentFil produksjonDokumentFil = dokumentFilTestRepository.findByFilUuid(produksjonFilUuid);
		assertThat(produksjonDokumentFil).isNull();
		validateJoarkOppdaterJournalLegacyMandatoryFields(journalpostId);
	}

	@Test
	void shouldSettBrevdataForProduksjon() {
		String arkivFilUuid = UUID.randomUUID().toString();
		String produksjonFilUuid = UUID.randomUUID().toString();
		Journalpost journalpost = createReservertPensjonJournalpost(arkivFilUuid, produksjonFilUuid);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders(APPLICATION_RTF));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_PRODUKSJON), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(CREATED);

		DokumentFil arkivDokumentFil = dokumentFilTestRepository.findByFilUuid(arkivFilUuid);
		assertThat(arkivDokumentFil).isNull();
		DokumentFil produksjonDokumentFil = dokumentFilTestRepository.findByFilUuid(produksjonFilUuid);
		assertThat(produksjonDokumentFil.getFil()).isEqualTo(FIL);
		validateJoarkOppdaterJournalLegacyMandatoryFields(journalpostId);
	}

	@Test
	void shouldOppdatereBrevdataForProduksjon() {
		String arkivFilUuid = UUID.randomUUID().toString();
		String produksjonFilUuid = UUID.randomUUID().toString();
		Journalpost journalpost = createReservertPensjonJournalpost(arkivFilUuid, produksjonFilUuid);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();
		FilDetaljer filDetaljer = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(PRODUKSJON);
		filDetaljer.setEndretKildeNavn("itest");
		filDetaljer.setFileContent(FIL);
		dokumentFilTestRepository.persist(filDetaljer.createDokumentFil());

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(OPPDATERT_FIL, settBrevdataHeaders(APPLICATION_RTF));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_PRODUKSJON), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		DokumentFil arkivDokumentFil = dokumentFilTestRepository.findByFilUuid(arkivFilUuid);
		assertThat(arkivDokumentFil).isNull();
		DokumentFil produksjonDokumentFil = dokumentFilTestRepository.findByFilUuid(produksjonFilUuid);
		assertThat(produksjonDokumentFil.getFil()).isEqualTo(OPPDATERT_FIL);
		validateJoarkOppdaterJournalLegacyMandatoryFields(journalpostId);
	}

	@Test
	void shouldReturnOkWhenArkivDokumentFilFinnes() {
		String arkivFilUuid = UUID.randomUUID().toString();
		String produksjonFilUuid = UUID.randomUUID().toString();
		Journalpost journalpost = createReservertPensjonJournalpost(arkivFilUuid, produksjonFilUuid);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();
		FilDetaljer filDetaljer = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV);
		filDetaljer.setEndretKildeNavn("itest");
		dokumentFilTestRepository.persist(filDetaljer.createDokumentFil());

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>("Ny ARKIV payload".getBytes(), settBrevdataHeaders());
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(OK);

		DokumentFil arkivDokumentFil = dokumentFilTestRepository.findByFilUuid(arkivFilUuid);
		assertThat(arkivDokumentFil.getFil()).isEqualTo(FIL);
		DokumentFil produksjonDokumentFil = dokumentFilTestRepository.findByFilUuid(produksjonFilUuid);
		assertThat(produksjonDokumentFil).isNull();
	}

	@Test
	void shouldReturnNotFoundWhenJournalpostIdNotFound() {
		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders());
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted("987654321"), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	void shouldReturnConflictWhenJournalStatusUgyldig() {
		Journalpost journalpost = createFerdigstiltJournalpostWithHoveddokument();
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();
		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders());
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
	}

	@Test
	void shouldReturnNotFoundWhenFilDetaljerDoesNotExist() {
		String produksjonFilUuid = UUID.randomUUID().toString();
		Journalpost journalpost = createReservertPensjonJournalpost(null, produksjonFilUuid);
		Long journalpostId = journalpostTestRepository.persist(journalpost).getJournalpostId();
		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders());
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted(journalpostId.toString()), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	@Test
	void shouldReturnUnknownMediaTypeWhenContentTypeNotPdfOrRtf() {
		var requestEntity = new HttpEntity<>(FIL, settBrevdataHeaders("application/json"));
		ResponseEntity<String> response = restTemplate.exchange(apiInternalPath(SETT_BREVDATA_PATH.formatted("9876"), VARIANT_FORMAT_ARKIV), POST, requestEntity, String.class);
		assertThat(response.getStatusCode()).isEqualTo(UNSUPPORTED_MEDIA_TYPE);
	}

	private void validateJoarkOppdaterJournalLegacyMandatoryFields(Long journalpostId) {
		Journalpost journalpost = journalpostTestRepository.findById(journalpostId).orElseThrow(JournalpostIkkeFunnetException::new);

		assertThat(journalpost.getEndretAvNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
		assertThat(journalpost.getEndretKildeNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
		assertThat(journalpost.getSaksrelasjon().getEndretAvNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
		assertThat(journalpost.getSaksrelasjon().getEndretKildeNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
		journalpost.getJournalpostDokumentInfoRelasjoner().forEach(journalpostDokumentInfoRelasjon -> {
			assertThat(journalpostDokumentInfoRelasjon.getDokumentInfo().getEndretAvNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
			assertThat(journalpostDokumentInfoRelasjon.getDokumentInfo().getEndretKildeNavn()).isEqualTo(ENDRET_AV_BREVSERVER);
		});
	}

	private HttpHeaders settBrevdataHeaders() {
		return settBrevdataHeaders(APPLICATION_PDF_VALUE);
	}

	private HttpHeaders settBrevdataHeaders(String contentType) {
		HttpHeaders headers = createHeadersWithServiceUserTokenAndRolesClaim(BREVSERVER, INTERN_ROLE_BREVSERVER);
		headers.set(CONTENT_TYPE, contentType);
		return headers;
	}
}
