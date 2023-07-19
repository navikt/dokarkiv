package no.nav.dokarkiv.safintern.hentdokument;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.SafinternConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_PDF;

public class HentDokumentIT extends AbstractSafinternTest {
	private static final String UUID = FilDetaljer.generateUuid();
	private static final String TEST_FILE_CONTENT = "testfilecontent";

	@Test
	public void shouldGetDocument() {
		Journalpost journalpost = buildPersistJournalpostAndDokument();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(OK));
		assertThat(responseEntity.getBody(), is(TEST_FILE_CONTENT.getBytes()));
		assertThat(responseEntity.getHeaders().getContentType(), is(APPLICATION_PDF));
	}

	@Test
	public void shouldGetNotFoundWhenDokumentInfoNotExists() {
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(123456L), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
	}

	@Test
	public void shouldGetNotFoundWhenDokumentFilNotExists() {
		Journalpost journalpost = buildAndPersistJournalpost();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
	}

	@Test
	public void shouldGetUnauthorizedWhenSafinternRoleClaimNotPresented() {
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(123456L), HttpMethod.GET, createHeaderEntityUtenTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(UNAUTHORIZED));
	}

	String hentDokumentPath(Long dokumentInfoId) {
		return SafinternConstants.BASE_PATH + "/hentdokument/%d/ARKIV".formatted(dokumentInfoId);
	}

	private Journalpost buildPersistJournalpostAndDokument() {
		buildAndPersistDokument();
		return buildAndPersistJournalpost();
	}

	private Journalpost buildAndPersistJournalpost() {
		Journalpost journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.fagomrade(FagomradeCode.FOR)
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetAvNavn("testuser")
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn("test")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
										.opprettetKildeNavn("test")
										.filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
												.filUuid(UUID)
												.filtype(FilTypeCode.PDF)
												.variantFormat(ARKIV)
												.opprettetKildeNavn("test")
												.build())
										.build())
								.build())
				.build();
		journalpostTestRepository.persist(journalpost);
		return journalpost;
	}

	private void buildAndPersistDokument() {
		DokumentFilTestRepository.persist(getDokumentFilBuilder()
				.filUuid(UUID)
				.fil(TEST_FILE_CONTENT.getBytes())
				.opprettetKildeNavn("test")
				.build());
	}
}

