package no.nav.dokarkiv.journalpost.v1.itest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.datautil.SakTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.exactly;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.lang.Long.parseLong;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SAKSTILKNYTNING;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode.ALTINN;
import static no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem.GSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.ARGUS;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.DAGPENGER;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.KELVIN;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService.UKJENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AKTOER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BATCHNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SOK;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAIL_AKTOER_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDFA;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XLSX;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FNR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FNR_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FNR_UGYLDIG;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.JOURNALFOERENDE_ENHET;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.KANALREFERANSE_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.KANAL_ALTINN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.KANAL_NAVNO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.PENSJON_FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_KTR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SYM;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_TIL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.XLSX_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createFagsak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createGenerellSak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequestWithAvsenderMottaker;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequestWithKanal;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

public class OpprettJournalpostIT extends AbstractJournalpostIT {

	public static final String FAGSYSTEM_ARGUS_AZP_NAME = "dev-fss:dsopkontroll:dsop-kontroll";
	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	public void happyPathOpprettInngaaende() {
		restStsToken();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());
		assertEquals(FILNAVN, journalpost.findAllDokumentInfos()
				.stream()
				.filter(dokumentInfo -> BREVKODE1.equals(dokumentInfo.getBrevkode()))
				.findAny()
				.get()
				.getFildetaljerListe()
				.stream()
				.filter(filDetaljer -> FILNAVN.equals(filDetaljer.getFilnavn()))
				.filter(filDetaljer -> BATCHNAVN.equals(filDetaljer.getBatchNavn()))
				.findAny()
				.get()
				.getFilnavn());

		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_LAND, journalpost.getLand());
		assertNotNull(journalpost.getDokumentDato());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(5);

		ArrayList<DokumentFil> dokumentFilList = Lists.newArrayList(dokumentFilTestRepository.findAll());
		assertEquals(3, dokumentFilList.size());
		dokumentFilList.forEach(dokumentFil -> assertNotNull(dokumentFil.getFil()));
		assertEquals(2, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT, dokumentFil.getFil())).count());
		assertEquals(1, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT_2, dokumentFil.getFil())).count());
	}

	@ParameterizedTest
	@EnumSource(value = InnsynCode.class, names = {"VISES_MASKINELT_GODKJENT", "VISES_MANUELT_GODKJENT"})
	public void happyPathOpprettInngaaendeMedOverstyringAvInnsynsregler(InnsynCode overstyrInnsynsregler) {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(JournalpostType.INNGAAENDE)
				.overstyrInnsynsregler(overstyrInnsynsregler.name())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(overstyrInnsynsregler, journalpost.getInnsyn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());

		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(4);
	}

	@Test
	public void happyPathOpprettInngaaendeUtenOverstyringAvInnsynsregler() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(JournalpostType.INNGAAENDE)
				.overstyrInnsynsregler(null)
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNull(journalpost.getInnsyn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());

		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(3);
	}

	@Test
	public void happyPathOpprettUtgaaende() {
		restStsToken();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(5);
	}

	@Test
	public void happyPathOpprettNotat() {
		restStsToken();

		OpprettJournalpostRequest request = createRequest(NOTAT);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.N, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(5);
	}

	@Test
	public void happyPathOpprettOgFerdigstillInngaaende() {
		restStsToken();

		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999", sak.getSakId().toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath() + FERDIGSTILL_QUERY, POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertTrue(response.getBody().isJournalpostferdigstilt());
		assertNotNull(response.getBody().getDokumenter());
		assertNotNull(response.getBody().getDokumenter().get(0).getDokumentInfoId());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(J, journalpost.getJournalstatus());
		assertEquals("9999", journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertEquals(2, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(6);

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(1).getBruker());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void happyPathOpprettOgFerdigstillUtgaaende() {
		restStsToken();

		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "0123", sak.getSakId().toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath() + FERDIGSTILL_QUERY, POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertTrue(response.getBody().isJournalpostferdigstilt());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.FS, journalpost.getJournalstatus());
		assertEquals("0123", journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(6);

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(1).getBruker());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void happyPathGsakArkivsak() {
		clearSakRepository();
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(GSAK)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), parseLong(ARKIVSAKSNUMMER));
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathGsakArkivsakSakstypeIkkeAngitt() {
		clearSakRepository();
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().arkivsaksnummer(ARKIVSAKSNUMMER).arkivsaksystem(GSAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), parseLong(ARKIVSAKSNUMMER));
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathPsakArkivsak() {
		clearSakRepository();
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(Arkivsaksystem.PSAK)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), parseLong(ARKIVSAKSNUMMER));
		assertEquals(saksrelasjon.getFagsystem(), FagsystemCode.PEN);
	}

	@Test
	public void happyPathNyGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(JournalpostType.INNGAAENDE)
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_SYM);
		assertTrue(isBlank(sak.getFagsakNr()));
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId());
		assertEquals(saksrelasjon.getFagsystem(), FS22);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());

		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(4);

		assertEquals(SAKSTILKNYTNING, aksjonsLoggList.get(1).getAksjon());
		assertThat(aksjonsLoggList.get(1).getArkivElementEndringer()).hasSize(3);
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

		assertEquals(sakTestRepository.count(), 1);

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
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

		assertEquals(2, sakTestRepository.count());

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(2, sakTestRepository.count());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), eldsteSak.getSakId());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathNyFagsak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_TIL);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), Fagsaksystem.AO01.name());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId());
		assertEquals(saksrelasjon.getFagsystem(), FS22);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());

		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(4);

		assertEquals(SAKSTILKNYTNING, aksjonsLoggList.get(1).getAksjon());
		assertThat(aksjonsLoggList.get(1).getArkivElementEndringer()).hasSize(4);
	}

	@Test
	public void happyPathNyFagsakAktoerId() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyFnrIdentStub();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = journalpostTestRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), FNR);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.PERSON);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(aksjonsLoggList.get(0).getBruker(), FNR);
	}

	@Test
	public void happyPathWithKanalAsNull() {
		restStsToken();

		OpprettJournalpostRequest requestWithKanalAsNull = createMinimalRequestWithKanal(null);

		HttpEntity<OpprettJournalpostRequest> requestEntityWithKanalAsNull = new HttpEntity<>(requestWithKanalAsNull, createHeadersWithServiceUserToken());

		restTemplate.exchange(apiJournalpostPath(), POST, requestEntityWithKanalAsNull, OpprettJournalpostResponse.class);

		Journalpost emptyKanalJournalpost = journalpostTestRepository.findByKanalReferanseId(requestWithKanalAsNull.getEksternReferanseId()).get();
		assertNotNull(emptyKanalJournalpost);
	}

	@Test
	public void happyPathWithKanalAsEmpty() {
		restStsToken();

		OpprettJournalpostRequest requestWithKanalAsEmpty = createMinimalRequestWithKanal("");

		HttpEntity<OpprettJournalpostRequest> requestEntityWithKanalAsEmpty = new HttpEntity<>(requestWithKanalAsEmpty, createHeadersWithServiceUserToken());

		restTemplate.exchange(apiJournalpostPath(), POST, requestEntityWithKanalAsEmpty, OpprettJournalpostResponse.class);

		Journalpost emptyKanalJournalpost = journalpostTestRepository.findByKanalReferanseId(requestWithKanalAsEmpty.getEksternReferanseId()).get();
		assertNotNull(emptyKanalJournalpost);
	}

	@Test
	public void shouldJournalfoereWhenTemUFOAndGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getTema(), TEMA_UFO);
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void shouldJournalfoereWhenTemaPENAndGenerellSak() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getTema(), TEMA_PEN);
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void shouldOppretteJournalpostWithoutBrukerWhenFnrNotFound() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		identNotFoundStub();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(FAIL_AKTOER_ID).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), FAIL_AKTOER_ID);

		assertEquals(journalpostTestRepository.findAll().iterator().next().getBrukere().size(), 0);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(aksjonsLoggList.get(0).getBruker(), UKJENT);
	}

	@Test
	public void shouldReturn404WhenFantIkkeAktoridIPDL() {
		clearSakRepository();
		stubAzure();
		identNotFoundStub();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(FNR_UGYLDIG).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, String.class);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertThat(response.getBody()).contains("Fant ikke aktørid for person i pdl.");
	}

	@Test
	public void happyPathNyFagsakOrgnr() {
		clearSakRepository();
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.ORGNR).id(BRUKER_ID_ORGANISASJON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getOrgnr(), BRUKER_ID_ORGANISASJON);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = journalpostTestRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), BRUKER_ID_ORGANISASJON);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.ORGANISASJON);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(2);
		assertEquals(aksjonsLoggList.get(0).getBruker(), BRUKER_ID_ORGANISASJON);
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

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathFagsakPesys() {
		clearSakRepository();
		restStsToken();

		long sakRepositoryCount = sakTestRepository.count();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(PENSJON_FAGSAK_ID).fagsaksystem(PP01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = journalpostTestRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), parseLong(PENSJON_FAGSAK_ID));
		assertEquals(saksrelasjon.getFagsystem(), FagsystemCode.PEN);

		assertEquals(sakTestRepository.count(), sakRepositoryCount);
	}

	@Test
	public void happyPathKunOriginalFerdigstillingWhenArgusIsConsumerId() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn("NAV Sparebank Oslo")
						.build())
				.tema(TEMA_KTR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(ARGUS).build())
				.kanal(KANAL_ALTINN)
				.tittel("Kontoopplysninger")
				.journalfoerendeEnhet("9999")
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel("Kontoopplysninger")
								.brevkode("KONTOOPPLYSNINGER")
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XLSX)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.fysiskDokument(XLSX_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndRolesClaim(FAGSYSTEM_ARGUS_AZP_NAME, ""));
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_KTR);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), ARGUS.name());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(journalpost.getJournalstatus(), J);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(3);
	}

	@Test
	public void happyPathKunOriginalFerdigstillingWhenArgusIsConsumerIdMedFlereDokumenter() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn("NAV Sparebank Oslo")
						.build())
				.tema(TEMA_KTR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(ARGUS).build())
				.kanal(KANAL_ALTINN)
				.tittel("Kontoopplysninger")
				.journalfoerendeEnhet("9999")
				.dokumenter(List.of(
						Dokument.builder()
								.tittel("Kontoopplysninger")
								.brevkode("KONTOOPPLYSNINGER")
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build(),
						Dokument.builder()
								.tittel("Kontoopplysninger")
								.brevkode("KONTOOPPLYSNINGER")
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XLSX)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.fysiskDokument(XLSX_DOKUMENT)
										.build()))
								.build(),
						Dokument.builder()
								.tittel("Kontoopplysninger")
								.brevkode("KONTOOPPLYSNINGER")
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_XLSX)
										.variantformat(VARIANTFORMAT_ORIGINAL)
										.fysiskDokument(XLSX_DOKUMENT)
										.build()))
								.build()
				))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndRolesClaim(FAGSYSTEM_ARGUS_AZP_NAME, ""));
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_KTR);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), ARGUS.name());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(journalpost.getJournalstatus(), J);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(3);
	}

	@Test
	public void happyPathKunArkivFerdigstillingWhenArgusIsConsumerId() {
		clearSakRepository();
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.avsenderMottaker(AvsenderMottaker.builder()
						.navn("NAV Sparebank Oslo")
						.build())
				.tema(TEMA_KTR)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(ARGUS).build())
				.kanal(KANAL_ALTINN)
				.tittel("Kontoopplysninger")
				.journalfoerendeEnhet("9999")
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel("Kontoopplysninger")
								.brevkode("KONTOOPPLYSNINGER")
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndRolesClaim(FAGSYSTEM_ARGUS_AZP_NAME, ""));
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakTestRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakTestRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_KTR);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), ARGUS.name());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(journalpost.getJournalstatus(), J);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(3);
	}

	@Test
	public void shouldFailOnFerdigstillingWhenMissingJournalfoerendeEnhet() {
		restStsToken();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, null);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertFalse(response.getBody().isJournalpostferdigstilt());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(5);
	}

	@Test
	public void shouldFailOnFerdigstillingWhenMissingPaakrevdeFelter() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.tema(TEMA_FOR)
				.journalfoerendeEnhet("2340")
				.bruker(Bruker.builder()
						.id(BRUKER_ID_PERSON)
						.idType(BrukerIdType.FNR)
						.build())
				.sak(Sak.builder()
						.arkivsaksnummer(SAK_ID.toString())
						.arkivsaksystem(GSAK)
						.build())
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertThat(response.getBody().getMelding()).contains("Journalposten mangler følgende felter: Journalpost.innhold");
		assertFalse(response.getBody().isJournalpostferdigstilt());

		Journalpost journalpost = journalpostTestRepository.findById(parseLong(response.getBody().getJournalpostId())).orElseThrow();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());
		assertEquals("2340", journalpost.getJournalForendeEnhetId());
		assertNull(journalpost.getJournalDato());
		assertNull(journalpost.getJournalfortAvNavn());
		assertNull(journalpost.getEndretAvNavn());
		assertNull(journalpost.getEndretKildeNavn());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(5);
	}

	@Test
	public void shouldFeilWhenFysiskDokumentFileContentNotMatchesWithFileTypeMagicNumber() {
		restStsToken();

		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.variantformat(VARIANTFORMAT_ARKIV)
												.fysiskDokument(FYSISK_DOKUMENT_WITH_INVALID_MAGIC_NUMBER)
												.batchnavn(BATCHNAVN)
												.build(),
										DokumentVariant.builder()
												.filtype(FILTYPE_XML)
												.variantformat(VARIANTFORMAT_ORIGINAL)
												.filnavn(FILNAVN)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.batchnavn(BATCHNAVN)
												.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat(response.getBody()).contains("fysiskDokument magicNumber={FF D8 FF E0 00} matcher ikke angitt filtype=PDF");
	}

	@Test
	public void shouldFailOnFerdigstillingAndSetJournalfoerendeEnhetWhenMissingPaakrevdeFelterAndInputJournalfoerendeEnhetIsMaskinell9999() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.tema(TEMA_FOR)
				.journalfoerendeEnhet("9999")
				.bruker(Bruker.builder()
						.id(BRUKER_ID_PERSON)
						.idType(BrukerIdType.FNR)
						.build())
				.sak(Sak.builder()
						.arkivsaksnummer(SAK_ID.toString())
						.arkivsaksystem(GSAK)
						.build())
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		String url = apiJournalpostPath(FERDIGSTILL_QUERY);
		System.out.println(url);
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(url, POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertThat(response.getBody().getMelding()).contains("Journalposten mangler følgende felter: Journalpost.innhold");
		assertFalse(response.getBody().isJournalpostferdigstilt());

		Journalpost journalpost = journalpostTestRepository.findById(parseLong(response.getBody().getJournalpostId())).orElseThrow();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());
		assertNull(journalpost.getJournalForendeEnhetId());
		assertNull(journalpost.getJournalDato());
		assertNull(journalpost.getJournalfortAvNavn());
		assertNull(journalpost.getEndretAvNavn());
		assertNull(journalpost.getEndretKildeNavn());
	}

	@Test
	public void shouldFailWhenNullEksternReferanseId() {
		restStsToken();

		OpprettJournalpostRequest request = OpprettJournalpostRequest.builder()
				.tema(FagomradeCode.FOR.name())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat(response.getBody()).contains("eksternReferanseId kan ikke være null eller tomt");
	}

	@Test
	public void shouldOppdatertJournalfoerendeEnhetToNullWhenFerdigstillingFailsAndJournalfoerendeEnhetEr9999() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.tema(TEMA_FOR)
				.tittel(INNHOLD)
				.bruker(Bruker.builder()
						.id(BRUKER_ID_PERSON)
						.idType(BrukerIdType.FNR)
						.build())
				.journalfoerendeEnhet("9999")
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDF)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertThat(response.getBody().getMelding()).contains(String.format("Kunne ikke ferdigstille: Journalpost med journalpostId=%s må ha en saksrelasjon", response.getBody().getJournalpostId()));
		assertFalse(response.getBody().isJournalpostferdigstilt());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());

		assertNull(journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList).hasSize(1);
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer()).hasSize(6);
	}

	@Test
	public void shouldRunOKWithoutTittelAndTema() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE).build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
	}

	@Test
	public void shouldJournalfoereSoeknadOmStoenadIPensjonsnoed() throws IOException {
		restStsToken();

		OpprettJournalpostRequest request = mapper.readValue(classpathToString("__files/opprettJournalpostMedEttDokument.json"), OpprettJournalpostRequest.class);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("M", response.getBody().getJournalstatus());
	}

	@Test
	public void shouldEndeligJournalfoereSoeknadOmForeldrepengerVedFoedsel() throws IOException {
		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithAvsenderMottaker(INNGAAENDE)
				.behandlingstema("ab0001")
				.bruker(Bruker.builder()
						.id(FNR)
						.idType(BrukerIdType.FNR)
						.build())
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel("Søknad om foreldrepenger ved fødsel")
								.brevkode("NAV 14-05.09")
								.dokumentKategori(DOKUMENTKATEGORI_SOK)
								.dokumentvarianter(singletonList(DokumentVariant.builder()
										.filtype(FILTYPE_PDFA)
										.variantformat(VARIANTFORMAT_ARKIV)
										.fysiskDokument(FYSISK_DOKUMENT)
										.build()))
								.build()))
				.eksternReferanseId("eksrefid")
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.kanal(KANAL_NAVNO)
				.sak(Sak.builder()
						.arkivsaksnummer(sak.getSakId().toString())
						.arkivsaksystem(GSAK)
						.build())
				.tema(TEMA_FOR)
				.tittel("Ettersendelse til søknad om foreldrepenger")
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("ENDELIG", response.getBody().getJournalstatus());
	}

	@Test
	public void shouldCallAktoerService() {
		restStsToken();
		stubAzure();
		happyAktoerIdStub();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(KELVIN)
						.fagsakId(FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR_2)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		verify(exactly(1), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutBrukerIdTypeFNR() {
		restStsToken();
		stubAzure();
		happyFnrIdentStub();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(DAGPENGER)
						.fagsakId(FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id(AKTOER_ID)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")).withRequestBody(containing("AKTORID")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithSAKFagsystemPP01() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(PP01)
						.fagsakId(PENSJON_FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutSakstype() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		verify(exactly(0), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	public void shouldNotOpprettIfReferanseIdAlreadyInDBAndEndeligJournalfortFirstTime() {

		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE, sak.getSakId().toString())
				.eksternReferanseId(KANALREFERANSE_ID)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.dokumenter(singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.variantformat(VARIANTFORMAT_ARKIV)
												.fysiskDokument(FYSISK_DOKUMENT)
												.batchnavn(BATCHNAVN)
												.build(),
										DokumentVariant.builder()
												.filtype(FILTYPE_XML)
												.variantformat(VARIANTFORMAT_ORIGINAL)
												.filnavn(FILNAVN)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.batchnavn(BATCHNAVN)
												.build()))
								.build()))
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> responseFirst = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);
		ResponseEntity<OpprettJournalpostResponse> responseSecond = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, responseFirst.getStatusCode());
		assertNotNull(responseFirst.getBody());
		assertThat(responseFirst.getBody().getJournalpostId()).isNotNull();
		assertTrue(responseFirst.getBody().isJournalpostferdigstilt());
		assertThat(responseFirst.getBody().getJournalstatus()).isEqualTo("ENDELIG");
		assertThat(responseFirst.getBody().getMelding()).isNull();
		assertThat(responseFirst.getBody().getDokumenter()).hasSize(1);
		assertEquals(HttpStatus.CONFLICT, responseSecond.getStatusCode());
		assertThat(responseSecond.getHeaders().getContentType()).isEqualTo(APPLICATION_JSON);
		assertNotNull(responseSecond.getBody());
		assertEqualOpprettJournalpostResponses(responseFirst.getBody(), responseSecond.getBody());
	}

	@Test
	public void shouldNotOpprettIfReferanseIdAlreadyInDBAndMidlertidigJournalfoertFirstTime() {
		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.eksternReferanseId(KANALREFERANSE_ID)
				.journalfoerendeEnhet(JOURNALFOERENDE_ENHET)
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> responseFirst = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);
		ResponseEntity<OpprettJournalpostResponse> responseSecond = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, responseFirst.getStatusCode());
		assertNotNull(responseFirst.getBody());
		assertThat(responseFirst.getBody().getJournalpostId()).isNotNull();
		assertFalse(responseFirst.getBody().isJournalpostferdigstilt());
		assertThat(responseFirst.getBody().getJournalstatus()).isEqualTo("MIDLERTIDIG");
		assertThat(responseFirst.getBody().getDokumenter()).hasSize(1);
		assertEquals(HttpStatus.CONFLICT, responseSecond.getStatusCode());
		assertThat(responseSecond.getHeaders().getContentType()).isEqualTo(APPLICATION_JSON);
		assertNotNull(responseSecond.getBody());
		assertEqualOpprettJournalpostResponses(responseFirst.getBody(), responseSecond.getBody());
	}


	@Test
	public void shouldOppretteUtgaaendeJournalpostAndSetSporingmetadataWhenServiceuserToken() {
		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNull(journalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetAvNavn());
		assertNull(journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertNull(journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(SERVICE_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldFerdigstilleUtgaaendeAndSetSporingmetadataWhenServiceuserToken() {
		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999", sak.getSakId().toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(SERVICE_USER_ID, journalpost.getEndretAvNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetAvNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(SERVICE_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldOppretteUtgaaendeJournalpostAndSetSporingmetadataWhenUserAndServiceuserToken() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithUserAndServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNull(journalpost.getEndretAvNavn());
		assertEquals(PERSON_USER_NAME, journalpost.getOpprettetAvNavn());
		assertNull(journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertNull(journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldFerdigstilleUtgaaendeAndSetSporingmetadataWhenUserAndServiceuserToken() {
		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999", sak.getSakId().toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithUserAndServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(PERSON_USER_NAME, journalpost.getEndretAvNavn());
		assertEquals(PERSON_USER_NAME, journalpost.getOpprettetAvNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldOpprettJournalpostWithNavUserIdFromHeaderWhenNavUserIdHeaderSet() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndUserIdHeader(SERVICE_USER_ID, NAV_USER_ID));
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertNull(journalpost.getEndretAvNavn());
		assertEquals(PERSON_USER_NAME, journalpost.getOpprettetAvNavn());
		assertNull(journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertNull(journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldOpprettAndFerdigstillJournalpostWithNavUserIdFromHeaderWhenNavUserIdHeaderSet() {
		no.nav.dokarkiv.core.domain.entities.Sak sak = SakTestDataProvider.createSakWithStatus(null).build();
		sakTestRepository.persist(sak);
		commitAndStartNewTransaction();

		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999", sak.getSakId().toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserTokenAndUserIdHeader(SERVICE_USER_ID, NAV_USER_ID));
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(PERSON_USER_NAME, journalpost.getEndretAvNavn());
		assertEquals(PERSON_USER_NAME, journalpost.getOpprettetAvNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getEndretKildeNavn());
		assertEquals(SERVICE_USER_ID, journalpost.getOpprettetKildeNavn());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getUpdatedBy());
		assertEquals(NAV_USER_ID, journalpost.getChangeStamp().getCreatedBy());
	}

	@Test
	public void shouldNotCreateDuplicateJournalpostWithSameEksternReferanseId() {
		stubAzure();
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequestWithKanal(ALTINN.toString());

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> firstResponse = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);
		ResponseEntity<OpprettJournalpostResponse> secondResponse = restTemplate.exchange(apiJournalpostPath(), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(firstResponse.getBody().getJournalpostId(), secondResponse.getBody().getJournalpostId());
	}

	@Test
	public void shouldUsePdlNameForAvsenderMottakerNameNull() {
		restStsToken();
		stubAzure();
		happyPersonIdentStub();
		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999");
		ReflectionTestUtils.setField(request.getAvsenderMottaker(), "navn", "");
		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals("TESTFORNAVN TESTFAMILIEN", journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		verify(exactly(1), postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	public void shouldUseProvidedNameForAvsenderMottaker() {
		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999");

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = journalpostTestRepository.findAll().iterator().next();
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
	}

	@Test
	public void shouldFailWhenTittelIsSkjult() {
		restStsToken();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE).tittel(SKJULT_TITTEL).build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<String> response = restTemplate.exchange(apiJournalpostPath(FERDIGSTILL_QUERY), POST, requestEntity, String.class);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertThat(response.getBody()).contains("Tittel kan ikke være " + SKJULT_TITTEL);
	}

	private void assertEqualOpprettJournalpostResponses(OpprettJournalpostResponse res1, OpprettJournalpostResponse res2) {
		assertEquals(res1.getJournalpostId(), res2.getJournalpostId());
		assertEquals(res1.getJournalstatus(), res2.getJournalstatus());
		assertEquals(res1.getMelding(), res2.getMelding());
		assertEquals(res1.isJournalpostferdigstilt(), res2.isJournalpostferdigstilt());
		assertEquals(res1.getDokumenter().size(), res2.getDokumenter().size());
		for (int i = 0; i < res1.getDokumenter().size(); i++) {
			assertEquals(res1.getDokumenter().get(i).getDokumentInfoId(), res2.getDokumenter().get(i).getDokumentInfoId());
		}
	}
}