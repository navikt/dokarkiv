package no.nav.dokarkiv.safintern.hentdokument;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.SafinternConstants;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import static no.nav.dokarkiv.core.domain.builder.DokumentFilBuilder.getDokumentFilBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.JSON;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.XML;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SKANNING_META;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.TEXT_XML;

public class HentDokumentIT extends AbstractSafinternTest {
	private static final String PDF_UUID = FilDetaljer.generateUuid();
	private static final String XML_UUID = FilDetaljer.generateUuid();
	private static final String JSON_UUID = FilDetaljer.generateUuid();
	private static final String TEST_FILE_CONTENT = "testfilecontent";
	private static final String TEST_FILE_CONTENT_XML = "<xml></xml>";
	private static final String TEST_FILE_CONTENT_JSON = """
			{"hello": "world"}
			""";

	@Test
	public void shouldGetDocument() {
		Journalpost journalpost = buildPersistJournalpostAndDokument();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId, ARKIV), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(OK));
		assertThat(responseEntity.getBody(), is(TEST_FILE_CONTENT.getBytes()));
		assertThat(responseEntity.getHeaders().getContentType(), is(APPLICATION_PDF));
	}

	@Test
	public void shouldGetXmlDocument() {
		Journalpost journalpost = buildPersistJournalpostAndDokument();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId, SKANNING_META), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(OK));
		assertThat(responseEntity.getBody(), is(TEST_FILE_CONTENT_XML.getBytes()));
		assertThat(responseEntity.getHeaders().getContentType(), is(TEXT_XML));
	}

	@Test
	public void shouldGetJsonDocument() {
		Journalpost journalpost = buildPersistJournalpostAndDokument();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId, ORIGINAL), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(OK));
		assertThat(responseEntity.getBody(), is(TEST_FILE_CONTENT_JSON.getBytes()));
		assertThat(responseEntity.getHeaders().getContentType(), is(APPLICATION_JSON));
	}

	@Test
	public void shouldGetNotFoundWhenDokumentInfoNotExists() {
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(123456L, ARKIV), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
	}

	@Test
	public void shouldGetNotFoundWhenDokumentFilNotExists() {
		Journalpost journalpost = buildAndPersistJournalpost();
		Long dokumentInfoId = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(dokumentInfoId, ARKIV), HttpMethod.GET, createHeaderEntityMedTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(NOT_FOUND));
	}

	@Test
	public void shouldGetUnauthorizedWhenSafinternRoleClaimNotPresented() {
		ResponseEntity<byte[]> responseEntity = restTemplate.exchange(hentDokumentPath(123456L, ARKIV), HttpMethod.GET, createHeaderEntityUtenTilgang(), byte[].class);

		assertThat(responseEntity.getStatusCode(), is(UNAUTHORIZED));
	}

	String hentDokumentPath(Long dokumentInfoId, VariantFormatCode original) {
		return SafinternConstants.BASE_PATH + "/hentdokument/%d/%s".formatted(dokumentInfoId, original);
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
										.filDetaljerList(getFilDetaljerBuilder()
														.filUuid(PDF_UUID)
														.filtype(PDF)
														.variantFormat(ARKIV)
														.opprettetKildeNavn("test")
														.build(),
												getFilDetaljerBuilder()
														.filUuid(XML_UUID)
														.filtype(XML)
														.variantFormat(SKANNING_META)
														.opprettetKildeNavn("test")
														.build(),
												getFilDetaljerBuilder()
														.filUuid(JSON_UUID)
														.filtype(JSON)
														.variantFormat(ORIGINAL)
														.opprettetKildeNavn("test")
														.build())
										.build())
								.build())
				.build();
		journalpostTestRepository.persist(journalpost);
		return journalpost;
	}

	private void buildAndPersistDokument() {
		dokumentFilTestRepository.persist(getDokumentFilBuilder()
				.filUuid(PDF_UUID)
				.fil(TEST_FILE_CONTENT.getBytes())
				.opprettetKildeNavn("test")
				.build());
		dokumentFilTestRepository.persist(getDokumentFilBuilder()
				.filUuid(XML_UUID)
				.fil(TEST_FILE_CONTENT_XML.getBytes())
				.opprettetKildeNavn("test")
				.build());
		dokumentFilTestRepository.persist(getDokumentFilBuilder()
				.filUuid(JSON_UUID)
				.fil(TEST_FILE_CONTENT_JSON.getBytes())
				.opprettetKildeNavn("test")
				.build());

	}
}

