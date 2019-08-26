package no.nav.dokarkiv.journalpost.v1.itest;

import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.OPPRETT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.NOTAT;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.UTGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.services.OpprettJournalpostService.UKJENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BREVKODE2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.BRUKER_ID_PERSON;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENTKATEGORI_SED;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL1;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.DOKUMENT_TITTEL2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILNAVN;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_PDF;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FILTYPE_XML;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.FYSISK_DOKUMENT_2;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.INNHOLD;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.SAK_ID;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.TEMA_FOR;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ARKIV;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.VARIANTFORMAT_ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createBaseRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createMinimalRequest;
import static no.nav.dokarkiv.journalpost.v1.util.TestUtils.createRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.Arkivsaksystem;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.OpprettJournalpostResponse;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpprettJournalpostIT extends AbstractJournalpostIT {

	private ObjectMapper mapper = new ObjectMapper();

	@Test
	public void happyPathOpprettInngaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertEquals(1, aksjonsLoggList.size());
		assertEquals(SERVICE_USER_ID, aksjonsLoggList.get(0).getUtfoertAv());
		assertEquals(OPPRETT, aksjonsLoggList.get(0).getAksjon());
		assertTrue(aksjonsLoggList.get(0).getArkivElementEndringer().isEmpty());

		ArrayList<DokumentFil> dokumentFilList = Lists.newArrayList(dokumentFilRepository.findAll());
		assertEquals(3, dokumentFilList.size());
		dokumentFilList.forEach(dokumentFil -> assertNotNull(dokumentFil.getFil()));
		assertEquals(2, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT, dokumentFil.getFil()))
				.collect(Collectors.toList())
				.size());
		assertEquals(1, dokumentFilList.stream()
				.filter(dokumentFil -> Arrays.equals(FYSISK_DOKUMENT_2, dokumentFil.getFil()))
				.collect(Collectors.toList())
				.size());
	}

	@Test
	public void happyPathOpprettUtgaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertThat(response.getBody().getJournalpostferdigstilt(), is(true));

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
		assertEquals(3, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void happyPathOpprettOgFerdigstillUtgaaende() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(UTGAAENDE, "9999");

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertNull(response.getBody().getMelding());
		assertThat(response.getBody().getJournalpostferdigstilt(), is(true));

		Journalpost journalpost = joarkRepository.findAll().iterator().next();
		assertNotNull(journalpost.getJournalpostId());
		assertEquals(JournalpostTypeCode.U, journalpost.getJournalposttype());
		assertEquals(JournalStatusCode.FS, journalpost.getJournalstatus());
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
		assertEquals(3, aksjonsLoggList.get(1).getArkivElementEndringer().size());
	}

	@Test
	public void shouldFailOnFerdigstillingWhenMissingJournalfoerendeEnhet() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = createRequest(INNGAAENDE, null);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
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

		OpprettJournalpostRequest request = createBaseRequest(INNGAAENDE)
				.journalfoerendeEnhet(null)
				.tittel(null)
				.tema(null)
				.dokumenter(Collections.singletonList(
						Dokument.builder()
								.tittel(DOKUMENT_TITTEL1)
								.brevkode(BREVKODE1)
								.dokumentKategori(DOKUMENTKATEGORI_SED)
								.dokumentvarianter(Arrays.asList(DokumentVariant.builder()
												.filtype(FILTYPE_PDF)
												.variantformat(VARIANTFORMAT_ARKIV)
												.fysiskDokument(FYSISK_DOKUMENT)
												.build(),
										DokumentVariant.builder()
												.filtype(FILTYPE_XML)
												.variantformat(VARIANTFORMAT_ORIGINAL)
												.filnavn(FILNAVN)
												.fysiskDokument(FYSISK_DOKUMENT_2)
												.build()))
								.build()))
				.build();

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());

	}

	@Test
	public void shouldJournalfoereSoeknadOmStoenadIPensjonsnoed() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = mapper.readValue(classpathToString("__files/opprettJournalpostMedEttDokument.json"), OpprettJournalpostRequest.class);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("M", response.getBody().getJournalstatus());

	}

	@Test
	public void shouldEndeligJournalfoereSoeknadOmForeldrepengerVedFoedsel() throws IOException {
		abacPermit();

		OpprettJournalpostRequest request = mapper.readValue(classpathToString("__files/soeknadOmForeldrepengerVedFoedsel.json"), OpprettJournalpostRequest.class);

		HttpEntity requestEntity = new HttpEntity(request, createHeadersWithServiceUserToken());
		ResponseEntity<OpprettJournalpostResponse> response = restTemplate.exchange(URL_JOURNALPOST + FERDIGSTILL_QUERY, HttpMethod.POST, requestEntity, OpprettJournalpostResponse.class);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals("ENDELIG", response.getBody().getJournalstatus());

	}
}