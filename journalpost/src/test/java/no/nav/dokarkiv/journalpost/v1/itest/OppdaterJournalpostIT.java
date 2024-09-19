package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottakerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.Tilleggsopplysning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.datautil.BrukerTestDataProvider.createBruker;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.INNHOLD;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.buildJournalpost;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.ENDRE_METADATA;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.ORGANISASJON;
import static no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.PERSON;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem.GSAK;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.DAGPENGER;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.KELVIN;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AKTOER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAIL_AKTOER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.LOCAL_DATE_TIME;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.PENSJON_FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SYM;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_TIL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createFagsak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createGenerellSak;
import static no.nav.dokarkiv.journalpost.v1.validators.OppdaterJournalpostValidator.LOVLIGE_INNSYNSKODER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class OppdaterJournalpostIT extends AbstractJournalpostIT {

	private static final String IDENTIFIKATOR = "12312312312";
	private static final String AVSENDER_MOTTAKER_NAVN = "etternavn, fornavn";
	private static final AvsenderMottakerIdType AVSENDER_MOTTAKER_TYPE_ID = AvsenderMottakerIdType.FNR;
	private static final String ARKIVSAKSNUMMER = "123123";
	private static final String TEMA = "FOR";
	private static final String BEHANDLINGSTEMA = "ab0001";
	private static final String TITTEL = "Ettersendelse av something";
	private static final String AVSENDER_MOTTAKER_LAND = "Legoland";
	private static final String NOKKEL = "nokkel";
	private static final String VERDI = "verdi";
	private static final String BREVKODE = "brevkode";
	private static final String JOURNALFOERENDE_ENHET = "9999";
	private static final String SERVICE_USER_ID = "srvdokarkiv";

	@BeforeEach
	public void setUp() {
		super.setUp();
		stubMsGraphGetUser(NAV_USER_ID);
		OIDC_TOKEN_PERSON_USER_TEST = openAmToken(NAV_USER_ID);
		OIDC_TOKEN_SERVICE_USER_TEST = restStsToken(SERVICE_USER_ID);
	}

	/**
	 * HVIS forsoekEndeligJF == TRUE, og ingen felter mangler for å endelig journalføre => returner 200 OK og journalpostId.
	 */
	@Test
	public void shouldFerdigstillJournalpostVedOppdateringUserTokenAndServiceUserToken() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn()).isEqualTo(PERSON_USER_NAME);
		assertThat(oppdatertJP.getEndretKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy()).isEqualTo(NAV_USER_ID);
		assertThat(oppdatertJP.getInnhold()).isEqualTo(request.getTittel());
		assertThat(oppdatertJP.getFagomrade().name()).isEqualTo(request.getTema());
		assertThat(oppdatertJP.getBehandlingstema()).isEqualTo(request.getBehandlingstema());
		assertThat(oppdatertJP.getJournalForendeEnhetId()).isEqualTo(request.getJournalfoerendeEnhet());
		assertThat(oppdatertJP.getLand()).isEqualTo(request.getAvsenderMottaker().getLand());
		assertThat(oppdatertJP.getAvsenderMottakerId()).isEqualTo(request.getAvsenderMottaker().getId());
		assertThat(oppdatertJP.getAvsenderMottaker()).isEqualTo(request.getAvsenderMottaker().getNavn());
		assertThat(oppdatertJP.getAvsenderMottakerIdType()).isEqualTo(AvsenderMottakerIdTypeCode.FNR);
		assertThat(oppdatertJP.getBrukere()).hasSize(1);
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId()).isEqualTo(request.getBruker().getId());
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType()).isEqualTo(PERSON);
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name()).isEqualTo("FS22");

		LocalDateTime twoDaysAgo = LocalDateTime.now().minusDays(2);
		assertThat(toLocalDateTime(oppdatertJP.getDokumentDato())).isBefore(twoDaysAgo);

		assertThat(oppdatertJP.getTilleggsopplysninger()).hasSize(1);
		assertThat(oppdatertJP.getTilleggsopplysninger().containsKey(request.getTilleggsopplysninger().get(0).getNokkel())).isNotNull();
		assertThat(oppdatertJP.getTilleggsopplysninger().containsValue(request.getTilleggsopplysninger().get(0).getVerdi())).isNotNull();

		no.nav.dokarkiv.core.domain.entities.DokumentInfo dokumentInfo = oppdatertJP.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(parseLong(request.getDokumenter().get(0).getDokumentInfoId()));
		assertThat(dokumentInfo.getTittel()).isEqualTo(request.getDokumenter().get(0).getTittel());
		assertThat(dokumentInfo.getBrevkode()).isEqualTo(request.getDokumenter().get(0).getBrevkode());
		assertThat(dokumentInfo.getSensitivt()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(3);

		assertThat(aksjonsLoggList.get(0).getUtfoertAv()).isEqualTo(NAV_USER_ID);
		assertThat(aksjonsLoggList.get(0).getApplikasjon()).isEqualTo(SERVICE_USER_ID);
		assertThat(aksjonsLoggList.get(0).getAksjon()).isEqualTo(SAKSTILKNYTNING);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(3);

		assertThat(aksjonsLoggList.get(1).getUtfoertAv()).isEqualTo(NAV_USER_ID);
		assertThat(aksjonsLoggList.get(1).getApplikasjon()).isEqualTo(SERVICE_USER_ID);
		assertThat(aksjonsLoggList.get(1).getAksjon()).isEqualTo(ENDRE_METADATA);
		assertThat(aksjonsLoggList.get(1).getArkivElementEndringer()).hasSize(7);

		assertThat(aksjonsLoggList.get(2).getUtfoertAv()).isEqualTo(NAV_USER_ID);
		assertThat(aksjonsLoggList.get(2).getApplikasjon()).isEqualTo(SERVICE_USER_ID);
		assertThat(aksjonsLoggList.get(2).getAksjon()).isEqualTo(ENDRE_METADATA);
		assertThat(aksjonsLoggList.get(2).getArkivElementEndringer()).hasSize(2);
	}

	private LocalDateTime toLocalDateTime(Date date){
		return date.toInstant()
				.atZone(ZoneId.systemDefault())
				.toLocalDateTime();
	}

	@Test
	public void shouldFjerneSaksrelasjonWhenOppdaterJournalpostEndrerTema() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));

		Long journalpostId = journalpost.getJournalpostId();
		String nyttTema = TEMA_UFO;

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(nyttTema)
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJP.getFagomrade().name()).isEqualTo(nyttTema);
		assertThat(oppdatertJP.getSaksrelasjon()).isNull();

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isNotEmpty();
	}

	@Test
	public void shouldFjerneSaksrelasjonWhenOppdaterJournalpostEndrerBruker() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		Bruker nyBruker = Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.bruker(nyBruker)
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getBrukere()).hasSize(1);
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId()).isEqualTo(request.getBruker().getId());
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType()).isEqualTo(PERSON);
		assertNull(oppdatertJP.getSaksrelasjon());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isNotEmpty();
	}

	@Test
	public void shouldNotProduceAksjonsLoggForEmptyRequest() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = new OppdaterJournalpostRequest();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		var aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@Test
	public void shouldUpdateJournalpostWithSaksrelasjonIsNull() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen")
				.saksrelasjon(null));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertThat(aksjonsLoggList.get(0).getAksjon()).isEqualTo(SAKSTILKNYTNING);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(3);
	}

	@Test
	public void shouldNotProduceAksjonsLoggForUnchangedFields() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.tittel(INNHOLD)
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@Test
	public void shouldFerdigstillJournalpostVedOppdateringOnlyServiceUserToken() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.add(AUTHORIZATION, BEARER + OIDC_TOKEN_SERVICE_USER_TEST);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getEndretKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getInnhold()).isEqualTo(request.getTittel());
		assertThat(oppdatertJP.getFagomrade().name()).isEqualTo(request.getTema());
		assertThat(oppdatertJP.getAvsenderMottakerId()).isEqualTo(request.getAvsenderMottaker().getId());
		assertThat(oppdatertJP.getAvsenderMottaker()).isEqualTo(request.getAvsenderMottaker().getNavn());
		assertThat(oppdatertJP.getAvsenderMottakerIdType()).isEqualTo(AvsenderMottakerIdTypeCode.FNR);
		assertThat(oppdatertJP.getBrukere()).hasSize(1);
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerId()).isEqualTo(request.getBruker().getId());
		assertThat(oppdatertJP.getBrukere().iterator().next().getBrukerType()).isEqualTo(PERSON);
		assertThat(oppdatertJP.getBrukere().iterator().next().getOpprettetKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJP.getSaksrelasjon().getFagsystem().name()).isEqualTo("FS22");
	}

	@Test
	public void shouldSetNavUserIdHeaderSporingWhenServiceUserTokenAndNavUserIdHeaderIsSet() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.add(AUTHORIZATION, BEARER + OIDC_TOKEN_SERVICE_USER_TEST);
		headers.add(NavHeaders.NAV_USER_ID, NAV_USER_ID);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJP = journalpostTestRepository.findById(journalpostId).get();

		assertThat(oppdatertJP.getEndretAvNavn()).isEqualTo(PERSON_USER_NAME);
		assertThat(oppdatertJP.getEndretKildeNavn()).isEqualTo(SERVICE_USER_ID);
		assertThat(oppdatertJP.getChangeStamp().getUpdatedBy()).isEqualTo(NAV_USER_ID);
	}

	@Test
	public void shouldReturnUnauthorizedIfBearerPrefixIsMissing() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();
		Long dokumentInfoId = journalpost.getJournalpostDokumentInfoRelasjoner()
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();

		OppdaterJournalpostRequest request = createPutOppdaterJournalpostRequestWithDokumentInfoId(dokumentInfoId);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.add(AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, headers);

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
	}

	@Test
	public void happyPathGsakArkivsak() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.sakstype(Sakstype.ARKIVSAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathGsakArkivsakSakstypeIkkeAngitt() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathPsakArkivsak() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.sak(Sak.builder()
						.arkivsaksystem(Arkivsaksystem.PSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.sakstype(Sakstype.ARKIVSAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(parseLong(ARKIVSAKSNUMMER));
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FagsystemCode.PEN);
	}

	@Test
	public void happyPathNyGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());
		assertThat(sakTestRepository.count()).isEqualTo(1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();

		assertThat(sak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(sak.getOrgnr()).isBlank();
		assertThat(sak.getTema()).isEqualTo(TEMA);
		assertThat(sak.getFagsakNr()).isBlank();
		assertThat(sak.getApplikasjon()).isEqualTo(FS22.name());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(sak.getSakId());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathEksisterendeGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createGenerellSak();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		assertThat(sakTestRepository.count()).isEqualTo(1);

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertThat(saksrelasjon.getSakId()).isEqualTo(sak.getSakId());
		assertThat(saksrelasjon.getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathVelgEldsteSakBlantToEksisterendeSaker() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		no.nav.dokarkiv.core.domain.entities.Sak eldsteSak = createGenerellSak();
		sakTestRepository.persist(eldsteSak);
		commitAndStartNewTransaction();

		no.nav.dokarkiv.core.domain.entities.Sak nyesteSak = createGenerellSak();
		sakTestRepository.persist(nyesteSak);
		commitAndStartNewTransaction();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		assertThat(sakTestRepository.count()).isEqualTo(2);

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertThat(saksrelasjon.getSakId()).isEqualTo(eldsteSak.getSakId());
		assertThat(saksrelasjon.getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathNyFagsak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());
		assertThat(sakTestRepository.count()).isEqualTo(1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertThat(sak.getAktoerId()).isEqualTo(AKTOER_ID);
		assertThat(sak.getOrgnr()).isBlank();
		assertThat(sak.getTema()).isEqualTo(TEMA);
		assertThat(sak.getFagsakNr()).isEqualTo(FAGSAK_ID);
		assertThat(sak.getApplikasjon()).isEqualTo(AO01.name());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(sak.getSakId());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathNyFagsakAktoerId() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyFnrIdentStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(sakTestRepository.count()).isEqualTo(1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertThat(sak.getAktoerId()).isEqualTo(AKTOER_ID);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = journalpostTestRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertThat(bruker.getBrukerId()).isEqualTo(FNR);
		assertThat(bruker.getBrukerType()).isEqualTo(PERSON);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertThat(aksjonsLoggList.get(1).getUtfoertAv()).isEqualTo(NAV_USER_ID);
	}

	@Test
	public void shouldOppdatereJournalpostWithoutBrukerWhenFnrNotFound() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		identNotFoundStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(AKTOERID).id(FAIL_AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(sakTestRepository.count()).isEqualTo(1);
		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertThat(sak.getAktoerId()).isEqualTo(FAIL_AKTOER_ID);

		assertThat(journalpostTestRepository.findAll().iterator().next().getBrukere()).hasSize(0);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
	}

	@Test
	public void happyPathNyFagsakOrgnr() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen")
				.brukere(createBruker("11111111111", PERSON)));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA)
				.bruker(Bruker.builder().idType(ORGNR).id(BRUKER_ID_ORGANISASJON).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());
		assertThat(sakTestRepository.count()).isEqualTo(1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertThat(sak.getOrgnr()).isEqualTo(BRUKER_ID_ORGANISASJON);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = journalpostTestRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertThat(bruker.getBrukerId()).isEqualTo(BRUKER_ID_ORGANISASJON);
		assertThat(bruker.getBrukerType()).isEqualTo(ORGANISASJON);
	}

	@Test
	public void happyPathEksisterendeFagsak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createFagsak();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		assertEquals(sakTestRepository.count(), 1);

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(AO01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());
		assertThat(sakTestRepository.count()).isEqualTo(1);

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getSakId()).isEqualTo(sak.getSakId());
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void happyPathFagsakPesys() {
		clearSakRepository();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(PENSJON_FAGSAK_ID)
						.fagsaksystem(PP01)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FagsystemCode.PEN);
		assertThat(sakTestRepository.count()).isEqualTo(0);
	}

	@Test
	public void shouldUpdateWhenTemaPENAndGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void shouldUpdateWhenTemaUFOAndGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder()
						.sakstype(GENERELL_SAK)
						.build())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost oppdatertJournalpost = journalpostTestRepository.findById(journalpostId).get();
		assertThat(oppdatertJournalpost.getSaksrelasjon().getFagsystem()).isEqualTo(FS22);
	}

	@Test
	public void shouldCallAktoerService() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(KELVIN)
						.build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		verify(exactly(1), postRequestedFor(urlEqualTo("/pdl")).withRequestBody(containing("AKTORID")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutBrukerIdTypeFNR() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyFnrIdentStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(FAGSAK_ID)
						.fagsaksystem(DAGPENGER)
						.build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")).withRequestBody(containing("AKTORID")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutSakstype() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")).withRequestBody(containing("AKTORID")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithSAKFagsystemPP01() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.sak(Sak.builder()
						.sakstype(FAGSAK)
						.fagsakId(PENSJON_FAGSAK_ID)
						.fagsaksystem(PP01)
						.build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	public void shouldDeleteAvsenderMottaker() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen")
				.avsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.avsenderMottaker(AvsenderMottaker.builder().id(" ").build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getAvsenderMottakerId()).isNull();
		assertThat(journalpostOppdatert.getAvsenderMottakerIdType()).isNull();
	}
	@Test
	public void shouldNotDeleteAvsenderMottaker() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen")
				.avsenderMottakerIdType(AvsenderMottakerIdTypeCode.FNR));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.avsenderMottaker(AvsenderMottaker.builder().id("").build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getAvsenderMottakerId()).isEqualTo("1");
		assertThat(journalpostOppdatert.getAvsenderMottakerIdType()).isEqualTo(AvsenderMottakerIdTypeCode.FNR);
	}

	@Test
	public void shouldNotUpdateAvsenderMottakerOnJournalpostOlderThanOneYear() {
		clearSakRepository();
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, J)
				.endretAvNavn("saksbehandlersen")
				.journalDato(java.sql.Date.valueOf(LOCAL_DATE_TIME.toLocalDate())));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.avsenderMottaker(AvsenderMottaker.builder().id("5").navn("Max Mekker").build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getAvsenderMottakerId()).isEqualTo("1");
		assertThat(journalpostOppdatert.getAvsenderMottaker()).isEqualTo("Bjarne Betjent");
	}

	@Test
	public void shouldNotUpdateTittelOnFerdigstiltJournalpostNotat() {
		clearSakRepository();

		Journalpost journalpostDraft = Journalpost.builder()
				.avsenderMottakerId("1")
				.journalposttype(N)
				.journalstatus(FL)
				.endretAvNavn("saksbehandlersen")
				.innhold("Gammel tittel")
				.journalDato(java.sql.Date.valueOf(LOCAL_DATE_TIME.toLocalDate()))
				.fagomrade(FagomradeCode.PEN)
				.build();
		journalpostDraft.setOpprettetKildeNavn("itest");

		Journalpost journalpost = journalpostTestRepository.persist(journalpostDraft);

		commitAndStartNewTransaction();

		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.tittel("Ny tittel")
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody().getMessage()).contains("Tittel kan ikke oppdateres for journalpost med journalpoststatus=FL og journalposttype=N");

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).orElse(null);
		assertThat(journalpostOppdatert).isNotNull();
		assertThat(journalpostOppdatert.getInnhold()).isEqualTo("Gammel tittel");
	}

	@Test
	public void shouldUsePdlNameForAvsenderMottakerNameNull() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyPersonIdentStub();

		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR).build())
				.avsenderMottaker(AvsenderMottaker.builder().id("01234567891").idType(AvsenderMottakerIdType.FNR).build())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getAvsenderMottaker()).isEqualTo("TESTFORNAVN TESTFAMILIEN");
		verify(exactly(1), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	void shouldReturnBadRequestWithErrorMessageOnInvalidFagsaksystem() {
		var requestString = classpathToString("__files/oppdaterJournalpostBodyMedUgyldigFagsaksystem.json");
		HttpEntity<String> stringHttpEntity = new HttpEntity<>(requestString, oidcHeaders());

		var responseEntity = restTemplate.exchange(URL_JOURNALPOST + "123123123", PUT, stringHttpEntity, String.class);

		String forventetFeilmelding = "Feltet sak.fagsaksystem=UGYLDIG må være en av %s".formatted(Arrays.toString(Fagsaksystem.values()));

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).contains(forventetFeilmelding);
	}

	@Test
	void shouldReturnBadRequestWithErrorMessageWhenTittelFemStjerner() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M));
		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.tittel("*****")
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpost.getJournalpostId(), PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).contains("Tittel kan ikke oppdateres til *****");
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, mode = INCLUDE, names = {
			"BRUK_STANDARDREGLER", "VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT",
			"SKJULES_FEILSENDT", "SKJULES_BRUKERS_SIKKERHET", "SKJULES_BRUKERS_ONSKE"
	})
	public void shouldUpdateJournalpostWithOverstyrInnsynsregler(InnsynCode innsynCode) {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(innsynCode.name())
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getInnsyn()).isEqualTo(innsynCode);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertThat(aksjonsLoggList.get(0).getUtfoertAv()).isEqualTo(NAV_USER_ID);
		assertThat(aksjonsLoggList.get(0).getApplikasjon()).isEqualTo(SERVICE_USER_ID);
		assertThat(aksjonsLoggList.get(0).getAksjon()).isEqualTo(ENDRE_METADATA);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(1);
	}

	@Test
	public void shouldAllowNullForOverstyrInnsynsregler() {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(null)
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getInnsyn()).isEqualTo(null);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, names = {"BRUK_STANDARDREGLER"})
	@NullSource
	public void shouldNotUpdateOverstyrInnsynsreglerIfTheValueIsUnchanged(InnsynCode innsynCode) {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.innsyn(innsynCode)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(innsynCode != null ? innsynCode.name() : null)
				.build();
		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		ResponseEntity<OppdaterJournalpostResponse> responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, OppdaterJournalpostResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(responseEntity.getBody().getJournalpostId()).isEqualTo(journalpostId.toString());

		Journalpost journalpostOppdatert = journalpostTestRepository.findById(journalpostId).get();
		assertThat(journalpostOppdatert.getInnsyn()).isEqualTo(innsynCode);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).isEmpty();
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, mode = EXCLUDE, names = {
			"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT", "SKJULES_FEILSENDT",
			"SKJULES_BRUKERS_SIKKERHET", "SKJULES_BRUKERS_ONSKE", "BRUK_STANDARDREGLER"
	})
	public void shouldReturnBadRequestWithErrorMessageOnInvalidOverstyrInnsynsregler(InnsynCode innsynCode) {
		Journalpost journalpost = buildAndCommit(buildJournalpost(I, M)
				.endretAvNavn("saksbehandlersen"));
		Long journalpostId = journalpost.getJournalpostId();

		OppdaterJournalpostRequest request = OppdaterJournalpostRequest.builder()
				.overstyrInnsynsregler(innsynCode.toString())
				.build();

		HttpEntity<OppdaterJournalpostRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());

		var responseEntity = restTemplate.exchange(URL_JOURNALPOST + journalpostId, PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(BAD_REQUEST);
		assertThat(responseEntity.getBody()).contains(format("OverstyrInnsynsregler må være en av følgende verdier: null eller %s. Mottatt: %s", LOVLIGE_INNSYNSKODER, innsynCode));
	}

	private OppdaterJournalpostRequest createPutOppdaterJournalpostRequestWithDokumentInfoId(Long dokumentInfoId) {
		return OppdaterJournalpostRequest.builder()
				.avsenderMottaker(AvsenderMottaker.builder()
						.id(IDENTIFIKATOR)
						.idType(AVSENDER_MOTTAKER_TYPE_ID)
						.navn(AVSENDER_MOTTAKER_NAVN)
						.land(AVSENDER_MOTTAKER_LAND)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(IDENTIFIKATOR)
						.build())
				.sak(Sak.builder()
						.arkivsaksystem(GSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.build())
				.tema(TEMA)
				.behandlingstema(BEHANDLINGSTEMA)
				.tittel(TITTEL)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.datoDokument(LocalDateTime.now().minusDays(3))
				.tilleggsopplysninger(List.of(Tilleggsopplysning.builder()
						.nokkel(NOKKEL)
						.verdi(VERDI)
						.build()))
				.dokumenter(List.of(DokumentInfo.builder()
						.dokumentInfoId(Long.toString(dokumentInfoId))
						.brevkode(BREVKODE)
						.tittel(TITTEL)
						.build()))
				.build();
	}
}