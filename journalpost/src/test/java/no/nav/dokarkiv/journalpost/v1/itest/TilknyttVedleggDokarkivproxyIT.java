package no.nav.dokarkiv.journalpost.v1.itest;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ConsumerUnauthorizedDokarkivFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.ArsakKode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TilknyttVedleggDokarkivproxyIT extends AbstractJournalpostIT {

	private static final String UGYLDIG_JOURNALPOST = "12312312312";
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORG_DOK_INFO_ID";
	private static final String GYLDIG_CONSUMER = "srvdokarkivproxy";
	private static final String UGYLDIG_CONSUMER = "srvdokarkiv";
	public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";


	@Test
	public void shouldTilknytteArkivVedleggTilJournalpost() {
		Journalpost targetJournalpost = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostArkiv();
		sourceJournalpost.setJournalstatus(JournalStatusCode.J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();

		completeCurrentAndStartNewTransaction();


		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourcejournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity= restTemplate.exchange(
				URL_JOURNALPOST_INTERN + targetJournalpostId + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoId)).findAny().get().getDokumentInfo();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(sourceDokumentInfo.getDokumentInfoId(), dokumentInfoKopi.getDokumentInfoId());
		TestTransaction.end();
	}

	@Test
	public void shouldTilknytteFlereVedleggTilJournalpost() {
		Journalpost targetJournalpost = createJournalpostArkiv();
		Journalpost sourceJournalpost1 = createJournalpostSladdet();
		Journalpost sourceJournalpost2 = createJournalpostSladdet();
		Journalpost sourceJournalpost3 = createJournalpostArkiv();
		sourceJournalpost3.setJournalstatus(JournalStatusCode.J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
		Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
		Long sourceJournalpostId3 = saveJournalpost(sourceJournalpost3).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + targetJournalpostId + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		//Assert 1 Sladdet
		Journalpost journalpostTilknyttetVedlegg1 = joarkRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil1 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
		assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
		assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

		//Assert 2 sladdet
		Journalpost journalpostTilknyttetVedlegg2 = joarkRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo2 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil2 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
		DokumentFil dokumentFilKopi2 = dokumentFilRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

		assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
		assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
		assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);


		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi3 = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3))
				.findAny()
				.get()
				.getDokumentInfo();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));
		assertEquals(sourceDokumentInfo3.getDokumentInfoId(), dokumentInfoKopi3.getDokumentInfoId());

		TestTransaction.end();
	}

	@Test
	public void shouldTilknytte2av3VedleggTilJournalpost() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost1 = createJournalpostSladdet();
		Journalpost sourceJournalpost2 = createJournalpostSladdet();
		Journalpost sourcejJournalpost3 = createJournalpostArkiv();
		Long journalpostIdVedlegg = saveJournalpost(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId1 = saveJournalpost(sourceJournalpost1).getJournalpostId();
		Long sourceJournalpostId2 = saveJournalpost(sourceJournalpost2).getJournalpostId();
		Long sourceJournalpostId3 = saveJournalpost(sourcejJournalpost3).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourcejJournalpost3.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId1, sourceDokumentInfoId1.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId2, sourceDokumentInfoId2.toString()));
		dokumentVedleggList.add(createDokumentVedlegg(sourceJournalpostId3, sourceDokumentInfoId3.toString()));

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		completeCurrentAndStartNewTransaction();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(ArsakKode.UGYLDIG_STATUS));

		//Assert 1 Sladdet
		Journalpost journalpostTilknyttetVedlegg1 = joarkRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId1.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil1 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertDokumentInfo(sourceDokumentInfo1, dokumentInfoKopi1);
		assertFildetaljer(sourceFilDetaljer1, filDetaljerKopi1);
		assertDokumentFil(sourceDokumentFil1, dokumentFilKopi1);

		//Assert 2 sladdet
		Journalpost journalpostTilknyttetVedlegg2 = joarkRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo2 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(sourceDokumentInfoId2.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil2 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
		DokumentFil dokumentFilKopi2 = dokumentFilRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

		assertDokumentInfo(sourceDokumentInfo2, dokumentInfoKopi2);
		assertFildetaljer(sourceFilDetaljer2, filDetaljerKopi2);
		assertDokumentFil(sourceDokumentFil2, dokumentFilKopi2);

		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(journalpostIdVedlegg).get();
		assertThat(journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.anyMatch(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3)), is(false));

		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenForWrongConsumer() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();
		when(tokenGrantValidator.validateOnBehalfOfAccessToken(any())).thenThrow(new ConsumerUnauthorizedDokarkivFunctionalException("Access Token is invalid"));

		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeaders(UGYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity= restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.FORBIDDEN));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnInvalidRequestForMissingTilknytetAvNavn() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequestWithoutTilknyttetAvNavn(
				createDokumentVedleggList(sourceJournalpostId, dokumentInfoId.toString())
		);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		var responseEntity= restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnNotFoundForJournalpost() {

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + UGYLDIG_JOURNALPOST + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnConflictForJournalpostWrongStatus() {
		Journalpost sourceJournalpost = createJournalpostSladdet();
		sourceJournalpost.setJournalstatus(JournalStatusCode.M);
		Long journalpostIdVedlegg = joarkRepository.save(sourceJournalpost).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.CONFLICT));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeUgyldigStatus() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		sourceJournalpost.setJournalstatus(JournalStatusCode.M);
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, dokumentInfoId
				.toString()));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(ArsakKode.UGYLDIG_STATUS));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeIkkeFunnet() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		completeCurrentAndStartNewTransaction();

		HttpHeaders headers = createHeaders(GYLDIG_CONSUMER);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(createDokumentVedleggList(sourceJournalpostId, "200000345"));

		var requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiledeDokumenter().get(0).getArsakKode(), is(ArsakKode.IKKE_FUNNET));
		TestTransaction.end();
	}

	private void assertDokumentInfo(DokumentInfo sourceDokumentInfo, DokumentInfo dokumentInfoKopi) {
		assertEquals(sourceDokumentInfo.getDokumentstatus(), dokumentInfoKopi.getDokumentstatus());
		assertEquals(sourceDokumentInfo.getDokumentFerdigDato(), dokumentInfoKopi.getDokumentFerdigDato());
		assertEquals(sourceDokumentInfo.getTittel(), dokumentInfoKopi.getTittel());
		assertEquals(sourceDokumentInfo.getBrevkode(), dokumentInfoKopi.getBrevkode());
		assertEquals(sourceDokumentInfo.getDokumenttypeId(), dokumentInfoKopi.getDokumenttypeId());
		assertEquals(sourceDokumentInfo.getBrevgruppe(), dokumentInfoKopi.getBrevgruppe());
		assertNull(dokumentInfoKopi.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo.getSensitivt(), dokumentInfoKopi.getSensitivt());
		assertEquals(sourceDokumentInfo.getInnskrenketPartsinnsyn(), dokumentInfoKopi.getInnskrenketPartsinnsyn());
		assertEquals(sourceDokumentInfo.getInnskrenketPartsinnsynFraTredjepart(), dokumentInfoKopi.getInnskrenketPartsinnsynFraTredjepart());
		assertEquals(sourceDokumentInfo.getOrganInternt(), dokumentInfoKopi.getOrganInternt());
		assertEquals(sourceDokumentInfo.getKonvertertFraSystem(), dokumentInfoKopi.getKonvertertFraSystem());
		assertNull(dokumentInfoKopi.getEndretAvNavn());
		assertEquals(sourceDokumentInfo.getKassertAvNavn(), dokumentInfoKopi.getKassertAvNavn());
		assertEquals(sourceDokumentInfo.getDatoKassert(), dokumentInfoKopi.getDatoKassert());
		assertThat(dokumentInfoKopi.getOpprettetKildeNavn(), is(GYLDIG_CONSUMER));
		assertNull(dokumentInfoKopi.getEndretKildeNavn());

	}

	private void assertFildetaljer(FilDetaljer sourceFilDetaljer, FilDetaljer filDetaljerKopi) {
		assertEquals(sourceFilDetaljer.getFiltype(), filDetaljerKopi.getFiltype());
		assertEquals(sourceFilDetaljer.getOnDemandId(), filDetaljerKopi.getOnDemandId());
		assertEquals(sourceFilDetaljer.getOnDemandInstans(), filDetaljerKopi.getOnDemandInstans());
		assertEquals(sourceFilDetaljer.getMetaforceInstanceId(), filDetaljerKopi.getMetaforceInstanceId());
		assertThat(filDetaljerKopi.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljerKopi.getOpprettetKildeNavn(), is(GYLDIG_CONSUMER));
		assertEquals(sourceFilDetaljer.getBatchNavn(), filDetaljerKopi.getBatchNavn());
		assertEquals(sourceFilDetaljer.getFilnavn(), filDetaljerKopi.getFilnavn());
		assertEquals(sourceFilDetaljer.getFilstorrelse(), filDetaljerKopi.getFilstorrelse());
		assertEquals(sourceFilDetaljer.getSkjermingType(), filDetaljerKopi.getSkjermingType());
		assertNull(filDetaljerKopi.getEndretKildeNavn());
	}

	private void assertDokumentFil(DokumentFil sourceDokumentFil, DokumentFil dokumentFilKopi) {
		assertEquals(new String(sourceDokumentFil.getFil()), new String(dokumentFilKopi.getFil()));
		assertThat(dokumentFilKopi.getOpprettetKildeNavn(), is(GYLDIG_CONSUMER));
	}

	private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList) {
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("TilknyttVedleggIT")
				.dokument(dokumentVedleggList)
				.build();
	}

	private TilknyttVedleggRequest createTilknyttVedleggRequestWithoutTilknyttetAvNavn(List<DokumentVedlegg> dokumentVedleggList) {
		return TilknyttVedleggRequest.builder()
				.dokument(dokumentVedleggList)
				.build();
	}

	private Journalpost createJournalpostSladdet() {
		Journalpost journalpostSladdet = createJournalpostWithHoveddokument();
		journalpostSladdet.setJournalstatus(JournalStatusCode.J);
		journalpostSladdet.setJournalposttype(JournalpostTypeCode.U);
		journalpostSladdet.setOpprettetAvNavn("opprettetAvNavn");
		journalpostSladdet.setOpprettetKildeNavn("opprettetKildeNavn");
		journalpostSladdet.setEndretKildeNavn("endretKildeNavn");
		journalpostSladdet.setEndretAvNavn("endretAvNavn");

		DokumentInfo dokumentInfo = journalpostSladdet.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));
		dokumentInfo.addFilDetaljer(createFildetaljerOgFil(dokumentInfo, VariantFormatCode.SLADDET));
		return journalpostSladdet;
	}

	private Journalpost createJournalpostArkiv() {
		Journalpost journalpostArkiv = createJournalpostWithHoveddokument();
		journalpostArkiv.setJournalstatus(JournalStatusCode.D);
		journalpostArkiv.setJournalposttype(JournalpostTypeCode.U);
		journalpostArkiv.setOpprettetAvNavn("opprettetAvNavn");
		journalpostArkiv.setOpprettetKildeNavn("opprettetKildeNavn");
		journalpostArkiv.setEndretKildeNavn("endretKildeNavn");
		journalpostArkiv.setEndretAvNavn("endretAvNavn");


		DokumentInfo dokumentInfo = journalpostArkiv.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfo.removeFilDetaljer(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));
		return journalpostArkiv;
	}

	private HttpHeaders createHeaders(String consumer) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Nav-Consumer-Id", NAV_CONSUMER_ID);
		String token = Base64Utils.encodeToString(
				(consumer + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		return headers;
	}

	private List<DokumentVedlegg> createDokumentVedleggList(Long journalpostId, String dokumentinfoId) {
		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(createDokumentVedlegg(journalpostId, dokumentinfoId));
		return dokumentVedleggList;
	}

	private DokumentVedlegg createDokumentVedlegg(Long journalpostId, String dokumentinfoId) {
		return DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostId)
				.dokumentInfoId(dokumentinfoId)
				.build();
	}

	private void completeCurrentAndStartNewTransaction() {
		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();
	}

}
