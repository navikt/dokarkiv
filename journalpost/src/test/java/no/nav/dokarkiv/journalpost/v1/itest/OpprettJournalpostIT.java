package no.nav.dokarkiv.journalpost.v1.itest;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.AKTOER_ID;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FAIL_AKTOER_ID;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FNR;
import static no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerV2Mock.FNR_2;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.AO01;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.KONT;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService.UKJENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.ARKIVSAKSNUMMER;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_MOTTAKER_LAND;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.AVSENDER_NAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_ORGANISASJON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FAGSAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_PEN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_SYM;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_TIL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_UFO;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createFagsak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createGenerellSak;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.consumer.aktoer.AktoerConsumerService;
import no.nav.dokarkiv.core.consumer.aktoer.HentAktoerIdForIdentRequestTo;
import no.nav.dokarkiv.core.consumer.aktoer.HentAktoerIdForIdentResponseTo;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
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
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpprettJournalpostIT extends AbstractJournalpostIT {

	private ObjectMapper mapper = new ObjectMapper();

	@Mock
	private AktoerConsumerService aktoerConsumerService;

	@Test
	public void happyPathOpprettInngaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
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
				.findAny()
				.get()
				.getFilnavn());

		assertEquals(AvsenderMottakerIdTypeCode.FNR, journalpost.getAvsenderMottakerIdType());
		assertEquals(AVSENDER_NAVN, journalpost.getAvsenderMottaker());
		assertEquals(AVSENDER_ID_PERSON, journalpost.getAvsenderMottakerId());
		assertEquals(AVSENDER_MOTTAKER_LAND, journalpost.getLand());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());

		ArrayList<DokumentFil> dokumentFilList = Lists.newArrayList(dokumentFilRepository.findAll());
		assertEquals(3, dokumentFilList.size());
		dokumentFilList.forEach(dokumentFil -> assertNotNull(dokumentFil.getFil()));
		assertEquals(2, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT, dokumentFil.getFil())).count());
		assertEquals(1, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT_2, dokumentFil.getFil())).count());
	}

	@Test
	public void happyPathOpprettUtgaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());
	}

	@Test
	public void happyPathOpprettNotat() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(NOTAT);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.N, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.D, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());
	}

	@Test
	public void happyPathOpprettOgFerdigstillInngaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, "9999");

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertThat(response.getBody().getJournalpostferdigstilt(), is(true));
		assertNotNull(response.getBody().getDokumenter());
		assertNotNull(response.getBody().getDokumenter().get(0).getDokumentInfoId());

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.J, journalpost.getJournalstatus());
		assertEquals("9999", journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(2, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(1).getBruker());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void happyPathOpprettOgFerdigstillUtgaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "0123");

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertThat(response.getBody().getJournalpostferdigstilt(), is(true));

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.FS, journalpost.getJournalstatus());
		assertEquals("0123", journalpost.getJournalForendeEnhetId());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(2, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());

		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(1).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(1).getBruker());
		assertEquals(AksjonsTypeCode.FERDIGSTILL, aksjonsLoggList.get(1).getAksjon());
		assertEquals(2, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void happyPathGsakArkivsak() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(Arkivsaksystem.GSAK)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), request.getSak().getArkivsaksnummer());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathGsakArkivsakSakstypeIkkeAngitt() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder().arkivsaksnummer(ARKIVSAKSNUMMER).arkivsaksystem(Arkivsaksystem.GSAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), request.getSak().getArkivsaksnummer());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathPsakArkivsak() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.sak(Sak.builder()
						.sakstype(Sakstype.ARKIVSAK)
						.arkivsaksnummer(ARKIVSAKSNUMMER)
						.arkivsaksystem(Arkivsaksystem.PSAK)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), request.getSak().getArkivsaksnummer());
		assertEquals(saksrelasjon.getFagsystem(), FagsystemCode.PEN);
	}

	@Test
	public void happyPathNyGenerellSak() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_SYM);
		assertTrue(isBlank(sak.getFagsakNr()));
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId().toString());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathEksisterendeGenerellSak() throws IOException {
		clearSakRepository();
		abacPermit();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createGenerellSak();
		sakRepository.save(sak);
		commitAndStartNewTransaction();

		assertEquals(sakRepository.count(), 1);

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_SYM)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId().toString());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathNyFagsak() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);
		assertTrue(isBlank(sak.getOrgnr()));
		assertEquals(sak.getTema(), TEMA_TIL);
		assertEquals(sak.getFagsakNr(), FAGSAK_ID);
		assertEquals(sak.getApplikasjon(), Fagsaksystem.AO01.name());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId().toString());
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathNyFagsakAktoerId() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(AKTOER_ID).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), AKTOER_ID);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = joarkRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), FNR);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.PERSON);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(aksjonsLoggList.get(0).getBruker(), FNR);
	}

	@Test
	public void happyPathTemaUFO() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getTema(), TEMA_UFO);
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void happyPathTemaPEN() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_PEN)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.GENERELL_SAK).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getTema(), TEMA_PEN);
		assertEquals(sak.getApplikasjon(), FS22.name());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getFagsystem(), FS22);
	}

	@Test
	public void shouldOppretteJournalpostWithoutBrukerWhenFnrNotFound() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.AKTOERID).id(FAIL_AKTOER_ID).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getAktoerId(), FAIL_AKTOER_ID);

		assertEquals(joarkRepository.findAll().iterator().next().getBrukere().size(), 0);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(aksjonsLoggList.get(0).getBruker(), UKJENT);
	}

	@Test
	public void happyPathNyFagsakOrgnr() throws IOException {
		clearSakRepository();
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.ORGNR).id(BRUKER_ID_ORGANISASJON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		no.nav.dokarkiv.core.domain.entities.Sak sak = sakRepository.findAll().iterator().next();
		assertEquals(sak.getOrgnr(), BRUKER_ID_ORGANISASJON);

		no.nav.dokarkiv.core.domain.entities.Bruker bruker = joarkRepository.findAll()
				.iterator()
				.next()
				.getBrukere()
				.iterator()
				.next();
		assertEquals(bruker.getBrukerId(), BRUKER_ID_ORGANISASJON);
		assertEquals(bruker.getBrukerType(), BrukerTypeCode.ORGANISASJON);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(aksjonsLoggList.get(0).getBruker(), BRUKER_ID_ORGANISASJON);
	}

	@Test
	public void happyPathEksisterendeFagsak() throws IOException {
		clearSakRepository();
		abacPermit();

		no.nav.dokarkiv.core.domain.entities.Sak sak = createFagsak();
		sakRepository.save(sak);
		commitAndStartNewTransaction();

		assertEquals(sakRepository.count(), 1);

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(AO01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(sakRepository.count(), 1);

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), sak.getSakId().toString());
		assertEquals(saksrelasjon.getFagsystem(), FS22);

	}

	@Test
	public void happyPathFagsakPesys() throws IOException {
		clearSakRepository();
		abacPermit();

		long sakRepositoryCount = sakRepository.count();

		OpprettJournalpostRequest request = createMinimalRequest(JournalpostType.INNGAAENDE)
				.tema(TEMA_TIL)
				.bruker(Bruker.builder().idType(BrukerIdType.FNR).id(BRUKER_ID_PERSON).build())
				.sak(Sak.builder().sakstype(Sakstype.FAGSAK).fagsakId(FAGSAK_ID).fagsaksystem(PP01).build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		Saksrelasjon saksrelasjon = joarkRepository.findAll().iterator().next().getSaksrelasjon();
		assertEquals(saksrelasjon.getSakId(), request.getSak().getFagsakId());
		assertEquals(saksrelasjon.getFagsystem(), FagsystemCode.PEN);

		assertEquals(sakRepository.count(), sakRepositoryCount);
	}


	@Test
	public void shouldFailOnFerdigstillingWhenMissingJournalfoerendeEnhet() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, null);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertThat(response.getBody().getJournalpostferdigstilt(), is(false));

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());
	}

	@Test
	public void shouldFailOnFerdigstillingWhenMissingPaakrevdeFelter() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.tittel(INNHOLD)
				.journalfoerendeEnhet("9999")
				.bruker(Bruker.builder()
						.id(BRUKER_ID_PERSON)
						.idType(BrukerIdType.FNR)
						.build())
				.sak(Sak.builder()
						.arkivsaksnummer(SAK_ID)
						.arkivsaksystem(Arkivsaksystem.GSAK)
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
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertTrue(response.getBody().getMelding().contains("følgende felt(er) mangler"));
		assertThat(response.getBody().getJournalpostferdigstilt(), is(false));

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(BRUKER_ID_PERSON, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());
	}

	@Test
	public void shouldFailOnFerdigstillingIfMissingBruker() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_FOR)
				.tittel(INNHOLD)
				.bruker(null)
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
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNotNull(response.getBody().getMelding());
		assertTrue(response.getBody().getMelding().contains("må knyttes til en bruker"));
		assertThat(response.getBody().getJournalpostferdigstilt(), is(false));

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.I, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.M, journalpost.getJournalstatus());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(UKJENT, aksjonsLoggList.get(0).getBruker());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());
	}

	@Test
	public void shouldRunOKWithoutTittelAndTema() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE).build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());

	}

	@Test
	public void shouldJournalfoereSoeknadOmStoenadIPensjonsnoed() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = mapper.readValue(classpathToString("__files/opprettJournalpostMedEttDokument.json"), OpprettJournalpostRequest.class);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("M", response.getBody().getJournalstatus());

	}

	@Test
	public void shouldEndeligJournalfoereSoeknadOmForeldrepengerVedFoedsel() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = mapper.readValue(classpathToString("__files/soeknadOmForeldrepengerVedFoedsel.json"), OpprettJournalpostRequest.class);

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("ENDELIG", response.getBody().getJournalstatus());

	}

	@Test
	public void shouldCallAktoerService() throws IOException {
		abacPermit();

		when(aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(AKTOER_ID)))
				.thenReturn(new HentAktoerIdForIdentResponseTo(FNR, new ArrayList<>()));

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(KONT)
						.fagsakId(FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.AKTOERID)
						.id(AKTOER_ID)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertEquals(FNR, journalpost.getBrukere().stream().findFirst().get().getBrukerId());
	}

	@Test
	public void shouldNotCallAktoerServiceWithoutBrukerIdTypeAKTOERID() throws IOException {
		abacPermit();

		when(aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(AKTOER_ID)))
				.thenReturn(new HentAktoerIdForIdentResponseTo(FNR_2, new ArrayList<>()));

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(KONT)
						.fagsakId(FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		journalpost.getBrukere().forEach(bruker -> assertNotEquals(FNR_2, bruker.getBrukerId()));
	}

	@Test
	public void shouldNotCallAktoerServiceWithSAKFagsystemPP01() throws IOException {
		abacPermit();

		when(aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(AKTOER_ID)))
				.thenReturn(new HentAktoerIdForIdentResponseTo(FNR_2, new ArrayList<>()));

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.sak(Sak.builder()
						.sakstype(Sakstype.FAGSAK)
						.fagsaksystem(PP01)
						.fagsakId(FAGSAK_ID)
						.build())
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		journalpost.getBrukere().forEach(bruker -> assertNotEquals(FNR_2, bruker.getBrukerId()));
	}

	@Test
	public void shouldNotCallAktoerServiceWithWrongSakstype() throws IOException {
		abacPermit();

		when(aktoerConsumerService.hentAktoerIdForIdent(new HentAktoerIdForIdentRequestTo(AKTOER_ID)))
				.thenReturn(new HentAktoerIdForIdentResponseTo(FNR_2, new ArrayList<>()));

		OpprettJournalpostRequest request = createMinimalRequest(INNGAAENDE)
				.tema(TEMA_UFO)
				.bruker(Bruker.builder()
						.idType(BrukerIdType.FNR)
						.id(FNR)
						.build())
				.build();

		HttpEntity<OpprettJournalpostRequest> requestEntity = new HttpEntity<>(request, createHeadersWithServiceUserToken());
		restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		journalpost.getBrukere().forEach(bruker -> assertNotEquals(FNR_2, bruker.getBrukerId()));
	}
}