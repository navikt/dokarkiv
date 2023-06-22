package no.nav.dokarkiv.journalpost.v1.itest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.MottaDokumentUtgaaendeSkanningRequest;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MottaDokumentUtgaaendeSkanningServiceIT extends AbstractJournalpostIT {
	private static final String GYLDIG_CONSUMER = "srvskanmotutgaaende";
	private static final String KILDE = "skanmotutgaaende";

	private final Date mockDate = new Date(Date.UTC(100, Calendar.NOVEMBER, 10, 0, 0, 0)); // aar 2000

	private final String mockMottaksKanal = MottaksKanalCode.SKAN_NETS.toString();
	private final List<Tilleggsopplysning> mockTilleggsopplysninger = List.of(new Tilleggsopplysning("mockNoekkel",
			"mockVerdi"));
	private final String mockBatchnavn = "mockBatchnavn";
	private final byte[] mockData = "mockData".getBytes();
	private final String mockFilnavn = "mockFilnavn";

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void mottaDokumentHappy() {
		Journalpost journalpost = generateTestJournalpost(
				JournalpostTypeCode.U,
				JournalStatusCode.R,
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT
		).build();
		DateProvider.configure(true, DateProvider.getDate(new Date()));

		long journalpostId = saveJournalpost(journalpost).getId();

		endTransaction();
		MottaDokumentUtgaaendeSkanningRequest request = createGyldigRequest();

		var responseEntity = doPutTransaction(GYLDIG_CONSUMER, request, journalpostId);

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();
		Map<String, String> tilleggsopplysninger = oppdatertJP.getTilleggsopplysninger();
		FilDetaljer filDetaljer =
				oppdatertJP.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getFildetaljerListe().iterator().next();
		DokumentFil dokumentfil = dokumentFilTestRepository.findByFilUuid(filDetaljer.getFilUuid());

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(JournalStatusCode.FL, oppdatertJP.getJournalstatus());
		assertEquals(mockTilleggsopplysninger.get(0).getVerdi(),
				tilleggsopplysninger.get(mockTilleggsopplysninger.get(0).getNokkel()));
		assertEquals(MottaksKanalCode.SKAN_NETS, oppdatertJP.getMottakskanal());
		assertEquals(KILDE, oppdatertJP.getEndretKildeNavn());
		assertEquals(mockDate, oppdatertJP.getMottattDato());
		assertEquals(DateProvider.getToday(), oppdatertJP.getJournalDato());
		assertEquals(FilTypeCode.PDF, filDetaljer.getFiltype());
		assertEquals(mockFilnavn, filDetaljer.getFilnavn());
		assertEquals(VariantFormatCode.ORIGINAL, filDetaljer.getVariantFormat());
		assertArrayEquals(mockData, dokumentfil.getFil());
		assertEquals(mockBatchnavn, filDetaljer.getBatchNavn());

	}

	@Test
	public void shouldAcceptMultipleFilesInRequest() {
		Journalpost journalpost = generateTestJournalpost(
				JournalpostTypeCode.U,
				JournalStatusCode.R,
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT
		).build();

		long journalpostId = saveJournalpost(journalpost).getId();

		endTransaction();

		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(
				List.of(
						DokumentVariant
								.builder()
								.filtype(FilTypeCode.PDF.toString())
								.variantformat(VariantFormatCode.ORIGINAL.toString())
								.fysiskDokument(mockData)
								.filnavn(mockFilnavn + "-1")
								.build(),
						DokumentVariant
								.builder()
								.filtype(FilTypeCode.PDF.toString())
								.variantformat(VariantFormatCode.ORIGINAL.toString())
								.fysiskDokument(mockData)
								.filnavn(mockFilnavn + "-2")
								.build()
				));

		var responseEntity = doPutTransaction(GYLDIG_CONSUMER, request, journalpostId);
		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();

		Set<FilDetaljer> filDetaljerSet =
				oppdatertJP.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getFildetaljerListe();
		List<FilDetaljer> filDetaljerList =
				filDetaljerSet.stream().sorted(Comparator.comparing(FilDetaljer::getFilnavn)).toList();

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(2, filDetaljerList.size());
		assertEquals(mockFilnavn + "-1", filDetaljerList.get(0).getFilnavn());
		assertEquals(mockFilnavn + "-2", filDetaljerList.get(1).getFilnavn());
	}

	@Test
	public void shouldReturnBadRequestWithInvalidRequest() throws IOException {
		String expectedErrorMessage = "mottaDokumentUtgaaendeSkanning feilet ved validering av request " +
				"journalpostId=%s " +
				"mottakskanal=SKAN_NETS " +
				"batchnavn=mockBatchnavn " +
				"feilmedling=Kan ikke validere request: " +
				"dokumentvarianter[0] har ugyldig filtype mockUgyldigFiltype, har ugyldig variantformat " +
				"mockUgyldigVariantformat, mangler fysiskDokument";

		Journalpost journalpost = generateTestJournalpost(
				JournalpostTypeCode.U,
				JournalStatusCode.R,
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT
		).build();

		long journalpostId = saveJournalpost(journalpost).getId();

		endTransaction();

		MottaDokumentUtgaaendeSkanningRequest request = buildRequest(List.of(DokumentVariant
				.builder()
				.filtype("mockUgyldigFiltype")
				.variantformat("mockUgyldigVariantformat")
				.fysiskDokument(null)
				.filnavn(mockFilnavn)
				.build()
		));

		validateRequestResponse(request, GYLDIG_CONSUMER, journalpostId, expectedErrorMessage, HttpStatus.BAD_REQUEST);
	}

	@Test
	public void shouldReturnBadRequestWithMissingElements() {
		String errorMessage = "mottaDokumentUtgaaendeSkanning feilet ved validering av journalpost " +
				"journalpostId=%s " +
				"mottakskanal=SKAN_NETS " +
				"batchnavn=mockBatchnavn " +
				"feilmedling=Kan ikke validere journalpost: Har ikke hoveddokument";

		Journalpost journalpost = generateTestJournalpost(
				JournalpostTypeCode.I,
				JournalStatusCode.FL,
				TilknyttetJournalpostSomCode.VEDLEGG
		).build();

		DokumentInfo dokumentInfo = DokumentInfo
				.builder()
				.build();
		dokumentInfo.setOpprettetKildeNavn("Itest");

		JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = JournalpostDokumentInfoRelasjon
				.builder()
				.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.journalpost(journalpost)
				.dokumentInfo(
						dokumentInfo
				)
				.tilknyttetAvNavn("Itest")
				.build();

		journalpostDokumentInfoRelasjon.setOpprettetKildeNavn("Itest");


		journalpost.addJournalpostDokumentInfoRelasjon(
				journalpostDokumentInfoRelasjon
		);


		long journalpostId = saveJournalpost(journalpost).getId();

		endTransaction();

		MottaDokumentUtgaaendeSkanningRequest request = createGyldigRequest();

		var responseEntity = doPutTransaction(GYLDIG_CONSUMER, request, journalpostId);

		try {
			JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());

			assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
			assertEquals(String.format(errorMessage, journalpostId), responseBody.get("message").textValue());

		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void shouldReturnConflictWithInvalidJournalpostTypeCode() {
		String errorMessage = "mottaDokumentUtgaaendeSkanning feilet ved validering av journalpost " +
				"journalpostId=%s " +
				"mottakskanal=SKAN_NETS " +
				"batchnavn=mockBatchnavn " +
				"feilmedling=Kan ikke validere journalpost: Journalposten har ugyldig journalposttype=I";

		Journalpost journalpost = generateTestJournalpost(
				JournalpostTypeCode.I,
				JournalStatusCode.R,
				TilknyttetJournalpostSomCode.HOVEDDOKUMENT
		).build();

		long journalpostId = saveJournalpost(journalpost).getId();

		endTransaction();

		MottaDokumentUtgaaendeSkanningRequest request = createGyldigRequest();

		var responseEntity = doPutTransaction(GYLDIG_CONSUMER, request, journalpostId);

		try {
			JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());

			assertEquals(String.format(errorMessage, journalpostId, journalpostId), responseBody.get("message").textValue());
			assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());

		} catch (IOException e) {
			fail();
		}
	}

	@Test
	public void shouldReturnNotFoundIfJournalpostDoesNotExist() throws IOException {

		String expectedErrorMessage = "mottaDokumentUtgaaendeSkanning\n" + "journalpost med id 0 ikke funnet";
		long journalpostId = 0L;

		MottaDokumentUtgaaendeSkanningRequest request = createGyldigRequest();

		validateRequestResponse(request, GYLDIG_CONSUMER, journalpostId, expectedErrorMessage, HttpStatus.NOT_FOUND);

	}

	private MottaDokumentUtgaaendeSkanningRequest createGyldigRequest() {
		return buildRequest(List.of(DokumentVariant
				.builder()
				.filtype(FilTypeCode.PDF.toString())
				.variantformat(VariantFormatCode.ORIGINAL.toString())
				.fysiskDokument(mockData)
				.filnavn(mockFilnavn)
				.build()
		));
	}

	private ResponseEntity doPutTransaction(String consumer, MottaDokumentUtgaaendeSkanningRequest request, long journalpostId) {
		HttpHeaders headers = createHeaders(consumer);
		HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity = restTemplate.exchange(
				URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT,
				requestHttpEntity, String.class);

		endTransaction();
		return responseEntity;
	}

	private void validateRequestResponse(MottaDokumentUtgaaendeSkanningRequest request, String consumer, long journalpostId, String expectedErrorMessage, HttpStatus expectedStatus) throws IOException {
		HttpHeaders headers = createHeaders(consumer);

		HttpEntity<MottaDokumentUtgaaendeSkanningRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity = restTemplate.exchange(
				URL_PROTECTED_INTERN_JOURNALPOST + journalpostId + "/mottaDokumentUtgaaendeSkanning", HttpMethod.PUT,
				requestHttpEntity, String.class);
		JsonNode responseBody = mapper.readTree((String) responseEntity.getBody());


		assertEquals(String.format(expectedErrorMessage, journalpostId), responseBody.get("message").textValue());
		assertEquals(expectedStatus, responseEntity.getStatusCode());
	}

	private MottaDokumentUtgaaendeSkanningRequest buildRequest(List<DokumentVariant> dokumentVarianter) {
		return new MottaDokumentUtgaaendeSkanningRequest(
				mockDate,
				mockMottaksKanal,
				mockTilleggsopplysninger,
				mockBatchnavn,
				dokumentVarianter
		);
	}

	private HttpHeaders createHeaders(String consumer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + openAmToken(consumer));

		return headers;
	}

	private void endTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

	private JournalpostBuilder generateTestJournalpost(
			JournalpostTypeCode journalpostTypeCode,
			JournalStatusCode journalStatusCode,
			TilknyttetJournalpostSomCode tilknyttetJournalpostSomCode,
			FilDetaljer... filDetaljer
	) {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.fagomrade(FagomradeCode.FOR)
				.opprettetKildeNavn("ITest")
				.journalpostType(journalpostTypeCode)
				.journalStatus(journalStatusCode)
				.dokumentInfoRelasjoner(
						JournalpostDokumentInfoRelasjonBuilder
								.getJournalpostDokumentInfoRelasjonBuilder()
								.opprettetKildeNavn("ITest")
								.tilknyttetAvNavn("ITest")
								.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder().opprettetKildeNavn("ITest").filDetaljerList(filDetaljer).build())
								.tilknyttetJournalpostSom(tilknyttetJournalpostSomCode)
								.build());
	}
}
