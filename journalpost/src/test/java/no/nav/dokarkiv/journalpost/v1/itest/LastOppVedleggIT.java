package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.TILKNYTT_NYTT_DOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.IS;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.XML;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BREVKODE;
import static no.nav.dokarkiv.core.util.TestDataGenerator.TITTEL;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFilMedFilnavn;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.journalpost.v1.util.TestDataUtils.createJournalpostUnderArbeid;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN_VEDLEGG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALPOST_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class LastOppVedleggIT extends AbstractJournalpostIT {

	private HttpHeaders headers;
	private static final String LAST_OPP_VEDLEGG_URL = apiJournalpostPath("%s/lastOppVedlegg");
	private static final DokumentVariant DOCUMENT_PDF = DokumentVariant.builder()
			.filtype(FILTYPE_PDF)
			.variantformat(VARIANTFORMAT_ARKIV)
			.fysiskDokument(FYSISK_DOKUMENT)
			.filnavn(FILNAVN_PDF)
			.build();
	private static final DokumentVariant DOCUMENT_XML = DokumentVariant.builder()
			.filtype(FILTYPE_XML)
			.variantformat(VARIANTFORMAT_ORIGINAL)
			.fysiskDokument(FYSISK_DOKUMENT_2)
			.filnavn(FILNAVN_XML)
			.build();
	private static final LastOppVedleggRequest LAST_OPP_VEDLEGG_REQUEST = new LastOppVedleggRequest(Dokument.builder()
			.tittel(TITTEL)
			.brevkode(BREVKODE)
			.dokumentvarianter(List.of(DOCUMENT_PDF))
			.build());

	@BeforeEach
	public void setUp() {
		headers = createHeadersWithUserAndServiceUserToken();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
	}
	
	@Test
	void shouldLastOppVedlegg() {
		Journalpost journalpost = createJournalpostUnderArbeid();
		Long journalpostId = saveJournalpost(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var request = new HttpEntity<>(LAST_OPP_VEDLEGG_REQUEST, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, LastOppVedleggResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(CREATED);

		var updatedJournalpost = journalpostTestRepository.findById(journalpostId);
		assertThat(updatedJournalpost).isPresent();

		//Relasjon
		assertThat(updatedJournalpost.get().findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)).hasSize(1);

		var vedleggRelasjon = updatedJournalpost.get().findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).iterator().next();
		assertThat(vedleggRelasjon.getTilknyttetAvNavn()).isEqualTo(PERSON_USER_NAME);
		assertThat(vedleggRelasjon.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);

		//DokumentInfo
		var dokumentInfo = vedleggRelasjon.getDokumentInfo();
		assertThat(dokumentInfo.getTittel()).isEqualTo(TITTEL);
		assertThat(dokumentInfo.getBrevkode()).isEqualTo(BREVKODE);
		assertThat(dokumentInfo.getKategori()).isEqualTo(IS);
		assertThat(dokumentInfo.getDokumentstatus()).isEqualTo(FERDIGSTILT);
		assertThat(dokumentInfo.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);

		//Fildetaljer & DokumentFil
		var fildetaljerListe = dokumentInfo.getFildetaljerListe();

		assertThat(fildetaljerListe)
				.hasSize(1)
				.extracting(FilDetaljer::getVariantFormat)
				.containsOnly(ARKIV);

		assertThat(dokumentInfo.getFildetaljerListe())
				.filteredOn(fildetaljer -> fildetaljer.getVariantFormat().equals(ARKIV))
				.singleElement()
				.satisfies(fildetaljer -> {
					assertThat(fildetaljer.getFilnavn()).isEqualTo(FILNAVN_PDF);
					assertThat(fildetaljer.getFiltype()).isEqualTo(PDF);
					assertThat(fildetaljer.getVariantFormat()).isEqualTo(ARKIV);
					assertThat(fildetaljer.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
					assertThat(fildetaljer.getFilstorrelse()).isEqualTo(String.valueOf(FYSISK_DOKUMENT.length));

					var dokumentFil = dokumentFilTestRepository.findByFilUuid(fildetaljer.getFilUuid());
					assertThat(dokumentFil.getFil()).isEqualTo(FYSISK_DOKUMENT);
				});

		//Aksjonslogg
		var aksjon = aksjonsLoggTestRepository.getAksjonsLoggByJournalpostId(journalpostId);

		assertThat(aksjon).hasSize(1);
		assertThat(aksjon)
				.singleElement()
				.satisfies(aksjonsLogg -> {
					assertThat(aksjonsLogg.getDokumentInfoId()).isEqualTo(dokumentInfo.getDokumentInfoId());
					assertThat(aksjonsLogg.getAksjon()).isEqualTo(TILKNYTT_NYTT_DOKUMENT);
					assertThat(aksjonsLogg.getUtfoertAv()).isEqualTo(NAV_USER_ID);
				});

		//Response
		assertThat(response.getBody())
				.isNotNull()
				.extracting(LastOppVedleggResponse::dokumentInfoId)
				.isEqualTo(dokumentInfo.getDokumentInfoId().toString());
	}

	@Test
	void shouldLastOppVedleggWhenMultipleDokumentvarianter() {
		Journalpost journalpost = createJournalpostUnderArbeid();
		Long journalpostId = saveJournalpost(journalpost).getJournalpostId();

		commitAndStartNewTransaction();

		var requestBody = new LastOppVedleggRequest(
				Dokument.builder()
						.tittel(TITTEL)
						.brevkode(BREVKODE)
						.dokumentvarianter(List.of(DOCUMENT_PDF, DOCUMENT_XML))
						.build());

		var request = new HttpEntity<>(requestBody, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, LastOppVedleggResponse.class);

		assertThat(response.getStatusCode()).isEqualTo(CREATED);

		var updatedJournalpost = journalpostTestRepository.findById(journalpostId);
		assertThat(updatedJournalpost).isPresent();

		assertThat(updatedJournalpost.get().findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)).hasSize(1);

		var vedleggRelasjon = updatedJournalpost.get().findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG).iterator().next();
		var dokumentInfo = vedleggRelasjon.getDokumentInfo();
		var fildetaljerListe = dokumentInfo.getFildetaljerListe();

		assertThat(fildetaljerListe)
				.hasSize(2)
				.extracting(FilDetaljer::getVariantFormat)
				.containsExactly(ARKIV, ORIGINAL);

		assertThat(dokumentInfo.getFildetaljerListe())
				.filteredOn(fildetaljer -> fildetaljer.getVariantFormat().equals(ARKIV))
				.singleElement()
				.satisfies(fildetaljer -> {
					assertThat(fildetaljer.getFilnavn()).isEqualTo(FILNAVN_PDF);
					assertThat(fildetaljer.getFiltype()).isEqualTo(PDF);
					assertThat(fildetaljer.getVariantFormat()).isEqualTo(ARKIV);
					assertThat(fildetaljer.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
					assertThat(fildetaljer.getFilstorrelse()).isEqualTo(String.valueOf(FYSISK_DOKUMENT.length));

					var dokumentFil = dokumentFilTestRepository.findByFilUuid(fildetaljer.getFilUuid());
					assertThat(dokumentFil.getFil()).isEqualTo(FYSISK_DOKUMENT);
				});

		assertThat(dokumentInfo.getFildetaljerListe())
				.filteredOn(fildetaljer -> fildetaljer.getVariantFormat().equals(ORIGINAL))
				.singleElement()
				.satisfies(fildetaljer -> {
					assertThat(fildetaljer.getFilnavn()).isEqualTo(FILNAVN_XML);
					assertThat(fildetaljer.getFiltype()).isEqualTo(XML);
					assertThat(fildetaljer.getVariantFormat()).isEqualTo(ORIGINAL);
					assertThat(fildetaljer.getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
					assertThat(fildetaljer.getFilstorrelse()).isEqualTo(String.valueOf(FYSISK_DOKUMENT_2.length));

					var dokumentFil = dokumentFilTestRepository.findByFilUuid(fildetaljer.getFilUuid());
					assertThat(dokumentFil.getFil()).isEqualTo(FYSISK_DOKUMENT_2);
				});

		assertThat(response.getBody())
				.isNotNull()
				.extracting(LastOppVedleggResponse::dokumentInfoId)
				.isEqualTo(dokumentInfo.getDokumentInfoId().toString());
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", "abc", "123.45"})
	void shouldReturnBadRequestWhenInvalidJournalpostId(String journalpostId) {
		var request = new HttpEntity<>(LAST_OPP_VEDLEGG_REQUEST, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
	}

	@Test
	void shouldReturnNotFoundWhenJournalpostDoesNotExist() {
		var request = new HttpEntity<>(LAST_OPP_VEDLEGG_REQUEST, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(JOURNALPOST_ID), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
		assertThat(response.getBody())
				.contains("Kunne ikke finne journalpost med journalpostId=%s i joark".formatted(JOURNALPOST_ID));
	}

	@Test
	void shouldReturnBadRequestWhenInvalidRequest() {
		var request = new HttpEntity<>(new LastOppVedleggRequest(null), headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted("123"), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody())
				.contains("Kunne ikke legge til vedlegg på journalpost med journalpostId=123. Validering av input feilet");
	}

	@Test
	void shouldReturnBadRequestWhenRequestBodyIsMissing() {
		var request = new HttpEntity<>(null, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted("123"), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(response.getBody()).contains("Required request body is missing");
	}

	@Test
	void shouldReturnConflictWhenJournalpostIsNotUnderProduksjon() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		journalpost.setJournalstatus(J);
		Long journalpostId = saveJournalpost(journalpost).getJournalpostId();

		commitAndStartNewTransaction();
		
		var request = new HttpEntity<>(LAST_OPP_VEDLEGG_REQUEST, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(response.getBody())
				.contains("Kunne ikke legge til vedlegg på journalpost med journalpostId=%s. Journalposten har status=%s, men må ha status=%s"
						.formatted(journalpost.getJournalpostId(), J, D));
	}

	@Test
	void shouldReturnConflictWhenJournalpostDoesNotHaveHoveddokument() {
		Journalpost journalpost = createJournalpostUnderArbeid();
		journalpost.removeJournalpostDokumentInfoRelasjon(journalpost.findHoveddokumentDokumentInfoRelasjon());
		Long journalpostId = saveJournalpost(journalpost).getJournalpostId();

		commitAndStartNewTransaction();
		
		var request = new HttpEntity<>(LAST_OPP_VEDLEGG_REQUEST, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(response.getBody())
				.contains("Kunne ikke legge til vedlegg på journalpost med journalpostId=%s. Journalposten må et hoveddokument"
						.formatted(journalpostId));
	}

	@Test
	void shouldReturnConflictWhenDuplikatVedlegg() {
		DokumentInfo vedlegg = createDokumentInfo();
		vedlegg.clearFildetaljerListe();

		FilDetaljer filDetaljer = createFildetaljerOgFilMedFilnavn(vedlegg, ARKIV, FILNAVN_VEDLEGG);
		vedlegg.addFilDetaljer(filDetaljer);

		Journalpost journalpost = createJournalpostUnderArbeid();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, vedlegg));
		Long journalpostId = saveJournalpost(journalpost).getJournalpostId();

		commitAndStartNewTransaction();
		
		var requestBody = new LastOppVedleggRequest(
				Dokument.builder()
						.tittel(TITTEL)
						.dokumentvarianter(List.of(DokumentVariant.builder()
								.filtype(FILTYPE_PDF)
								.variantformat(ARKIV.name())
								.fysiskDokument(FYSISK_DOKUMENT)
								.filnavn(FILNAVN_VEDLEGG)
								.build()))
						.build());

		var request = new HttpEntity<>(requestBody, headers);
		var response = restTemplate.exchange(LAST_OPP_VEDLEGG_URL.formatted(journalpostId), PATCH, request, String.class);

		assertThat(response.getStatusCode()).isEqualTo(CONFLICT);
		assertThat(response.getBody())
				.contains("Kunne ikke legge til vedlegg på journalpost med journalpostId=%s. Dokument med variantformat=%s og filnavn=%s er allerede tilknyttet journalposten"
						.formatted(journalpostId, ARKIV.name(), FILNAVN_VEDLEGG));
	}
}
