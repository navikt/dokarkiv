package no.nav.dokarkiv.journalpost.v1.itest;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.ArsakFeilCode;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import org.junit.Test;
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
import java.util.Set;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggIT extends AbstractJournalpostIT {

	private static final String UGYLDIG_JOURNALPOST = "***gammelt_fnr***";
	private static final String OPPRETTET_KILDE_NAVN = "dokarkiv";
	private static final String TILLEGGOPPLYSNINGER_KEY = "DOK_ORIGINAL_DOKUMENT_INFO_ID";

	@Test
	public void shouldTilknytteArkivVedleggTilJournalpost() {
		Journalpost targetJournalpost = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostArkiv();
		sourceJournalpost.setJournalstatus(JournalStatusCode.J);
		Long targetJournalpostId = saveJournalpost(targetJournalpost).getJournalpostId();
		Long sourcejournalpostId = saveJournalpost(sourceJournalpost).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourcejournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + targetJournalpostId + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

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

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Long sourceDokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		Long sourceDokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		Long sourceDokumentInfoId3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId1)
				.dokumentInfoId(sourceDokumentInfoId1.toString())
				.build());
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId2)
				.dokumentInfoId(sourceDokumentInfoId2.toString())
				.build());
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId3)
				.dokumentInfoId(sourceDokumentInfoId3.toString())
				.build());


		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + targetJournalpostId + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


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
		DokumentFil dourceDokumentFil1 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertEquals(sourceDokumentInfo1.getDokumentstatus(), dokumentInfoKopi1.getDokumentstatus());
		assertEquals(sourceDokumentInfo1.getDokumentFerdigDato(), dokumentInfoKopi1.getDokumentFerdigDato());
		assertEquals(sourceDokumentInfo1.getTittel(), dokumentInfoKopi1.getTittel());
		assertEquals(sourceDokumentInfo1.getBrevkode(), dokumentInfoKopi1.getBrevkode());
		assertEquals(sourceDokumentInfo1.getDokumenttypeId(), dokumentInfoKopi1.getDokumenttypeId());
		assertEquals(sourceDokumentInfo1.getBrevgruppe(), dokumentInfoKopi1.getBrevgruppe());
		assertEquals(null, dokumentInfoKopi1.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo1.getSensitivt(), dokumentInfoKopi1.getSensitivt());
		assertEquals(sourceDokumentInfo1.getInnskrenketPartsinnsyn(), dokumentInfoKopi1.getInnskrenketPartsinnsyn());
		assertEquals(sourceDokumentInfo1.getInnskrenketPartsinnsynFraTredjepart(), dokumentInfoKopi1.getInnskrenketPartsinnsynFraTredjepart());
		assertEquals(sourceDokumentInfo1.getOrganInternt(), dokumentInfoKopi1.getOrganInternt());
		assertEquals(sourceDokumentInfo1.getKonvertertFraSystem(), dokumentInfoKopi1.getKonvertertFraSystem());
		assertEquals(sourceDokumentInfo1.getEndretAvNavn(), dokumentInfoKopi1.getEndretAvNavn());
		assertEquals(sourceDokumentInfo1.getKassertAvNavn(), dokumentInfoKopi1.getKassertAvNavn());
		assertEquals(sourceDokumentInfo1.getDatoKassert(), dokumentInfoKopi1.getDatoKassert());
		assertThat(dokumentInfoKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceDokumentInfo1.getEndretKildeNavn(), dokumentInfoKopi1.getEndretKildeNavn());

		assertEquals(sourceFilDetaljer1.getFiltype(), filDetaljerKopi1.getFiltype());
		assertEquals(sourceFilDetaljer1.getOnDemandId(), filDetaljerKopi1.getOnDemandId());
		assertEquals(sourceFilDetaljer1.getOnDemandInstans(), filDetaljerKopi1.getOnDemandInstans());
		assertEquals(sourceFilDetaljer1.getMetaforceInstanceId(), filDetaljerKopi1.getMetaforceInstanceId());
		assertThat(filDetaljerKopi1.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljerKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceFilDetaljer1.getBatchNavn(), filDetaljerKopi1.getBatchNavn());
		assertEquals(sourceFilDetaljer1.getFilnavn(), filDetaljerKopi1.getFilnavn());
		assertEquals(sourceFilDetaljer1.getFilstorrelse(), filDetaljerKopi1.getFilstorrelse());
		assertEquals(sourceFilDetaljer1.getSkjermingType(), filDetaljerKopi1.getSkjermingType());
		assertEquals(sourceFilDetaljer1.getEndretKildeNavn(), filDetaljerKopi1.getEndretKildeNavn());

		assertEquals(new String(dourceDokumentFil1.getFil()), new String(dokumentFilKopi1.getFil()));
		assertThat(dokumentFilKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));

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

		assertEquals(sourceDokumentInfo2.getDokumentstatus(), dokumentInfoKopi2.getDokumentstatus());
		assertEquals(sourceDokumentInfo2.getDokumentFerdigDato(), dokumentInfoKopi2.getDokumentFerdigDato());
		assertEquals(sourceDokumentInfo2.getTittel(), dokumentInfoKopi2.getTittel());
		assertEquals(sourceDokumentInfo2.getBrevkode(), dokumentInfoKopi2.getBrevkode());
		assertEquals(sourceDokumentInfo2.getDokumenttypeId(), dokumentInfoKopi2.getDokumenttypeId());
		assertEquals(sourceDokumentInfo2.getBrevgruppe(), dokumentInfoKopi2.getBrevgruppe());
		assertEquals(null, dokumentInfoKopi2.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo2.getSensitivt(), dokumentInfoKopi2.getSensitivt());
		assertEquals(sourceDokumentInfo2.getInnskrenketPartsinnsyn(), dokumentInfoKopi2.getInnskrenketPartsinnsyn());
		assertEquals(sourceDokumentInfo2.getInnskrenketPartsinnsynFraTredjepart(), dokumentInfoKopi2.getInnskrenketPartsinnsynFraTredjepart());
		assertEquals(sourceDokumentInfo2.getOrganInternt(), dokumentInfoKopi2.getOrganInternt());
		assertEquals(sourceDokumentInfo2.getKonvertertFraSystem(), dokumentInfoKopi2.getKonvertertFraSystem());
		assertEquals(sourceDokumentInfo2.getEndretAvNavn(), dokumentInfoKopi2.getEndretAvNavn());
		assertEquals(sourceDokumentInfo2.getKassertAvNavn(), dokumentInfoKopi2.getKassertAvNavn());
		assertEquals(sourceDokumentInfo2.getDatoKassert(), dokumentInfoKopi2.getDatoKassert());
		assertThat(dokumentInfoKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceDokumentInfo2.getEndretKildeNavn(), dokumentInfoKopi2.getEndretKildeNavn());

		assertEquals(sourceFilDetaljer2.getFiltype(), filDetaljerKopi2.getFiltype());
		assertEquals(sourceFilDetaljer2.getOnDemandId(), filDetaljerKopi2.getOnDemandId());
		assertEquals(sourceFilDetaljer2.getOnDemandInstans(), filDetaljerKopi2.getOnDemandInstans());
		assertEquals(sourceFilDetaljer2.getMetaforceInstanceId(), filDetaljerKopi2.getMetaforceInstanceId());
		assertThat(filDetaljerKopi2.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljerKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceFilDetaljer2.getBatchNavn(), filDetaljerKopi2.getBatchNavn());
		assertEquals(sourceFilDetaljer2.getFilnavn(), filDetaljerKopi2.getFilnavn());
		assertEquals(sourceFilDetaljer2.getFilstorrelse(), filDetaljerKopi2.getFilstorrelse());
		assertEquals(sourceFilDetaljer2.getSkjermingType(), filDetaljerKopi2.getSkjermingType());
		assertEquals(sourceFilDetaljer2.getEndretKildeNavn(), filDetaljerKopi2.getEndretKildeNavn());

		assertEquals(new String(sourceDokumentFil2.getFil()), new String(dokumentFilKopi2.getFil()));
		assertThat(dokumentFilKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));


		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(targetJournalpostId).get();
		DokumentInfo sourceDokumentInfo3 = sourceJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi3 = journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getDokumentInfoId().equals(sourceDokumentInfoId3)).findAny().get().getDokumentInfo();

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

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Long dokumentInfoId1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		Long dokumentInfoId2 = sourceJournalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();
		Long dokumentInfoId3 = sourcejJournalpost3.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId1)
				.dokumentInfoId(dokumentInfoId1.toString())
				.build());
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId2)
				.dokumentInfoId(dokumentInfoId2.toString())
				.build());
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId3)
				.dokumentInfoId(dokumentInfoId3.toString())
				.build());


		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiletDokument().get(0).getArsakKode(), is(ArsakFeilCode.UGYLDIG_STATUS));

		//Assert 1 Sladdet
		Journalpost journalpostTilknyttetVedlegg1 = joarkRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo1 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi1 = journalpostTilknyttetVedlegg1.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(dokumentInfoId1.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer1 = sourceDokumentInfo1.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi1 = dokumentInfoKopi1.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil1 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer1.getFilUuid());
		DokumentFil dokumentFilKopi1 = dokumentFilRepository.findByFilUuid(filDetaljerKopi1.getFilUuid());

		assertEquals(sourceDokumentInfo1.getDokumentstatus(), dokumentInfoKopi1.getDokumentstatus());
		assertEquals(sourceDokumentInfo1.getDokumentFerdigDato(), dokumentInfoKopi1.getDokumentFerdigDato());
		assertEquals(sourceDokumentInfo1.getTittel(), dokumentInfoKopi1.getTittel());
		assertEquals(sourceDokumentInfo1.getBrevkode(), dokumentInfoKopi1.getBrevkode());
		assertEquals(sourceDokumentInfo1.getDokumenttypeId(), dokumentInfoKopi1.getDokumenttypeId());
		assertEquals(sourceDokumentInfo1.getBrevgruppe(), dokumentInfoKopi1.getBrevgruppe());
		assertEquals(null, dokumentInfoKopi1.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo1.getSensitivt(), dokumentInfoKopi1.getSensitivt());
		assertEquals(sourceDokumentInfo1.getInnskrenketPartsinnsyn(), dokumentInfoKopi1.getInnskrenketPartsinnsyn());
		assertEquals(sourceDokumentInfo1.getInnskrenketPartsinnsynFraTredjepart(), dokumentInfoKopi1.getInnskrenketPartsinnsynFraTredjepart());
		assertEquals(sourceDokumentInfo1.getOrganInternt(), dokumentInfoKopi1.getOrganInternt());
		assertEquals(sourceDokumentInfo1.getKonvertertFraSystem(), dokumentInfoKopi1.getKonvertertFraSystem());
		assertEquals(sourceDokumentInfo1.getEndretAvNavn(), dokumentInfoKopi1.getEndretAvNavn());
		assertEquals(sourceDokumentInfo1.getKassertAvNavn(), dokumentInfoKopi1.getKassertAvNavn());
		assertEquals(sourceDokumentInfo1.getDatoKassert(), dokumentInfoKopi1.getDatoKassert());
		assertThat(dokumentInfoKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceDokumentInfo1.getEndretKildeNavn(), dokumentInfoKopi1.getEndretKildeNavn());

		assertEquals(sourceFilDetaljer1.getFiltype(), filDetaljerKopi1.getFiltype());
		assertEquals(sourceFilDetaljer1.getOnDemandId(), filDetaljerKopi1.getOnDemandId());
		assertEquals(sourceFilDetaljer1.getOnDemandInstans(), filDetaljerKopi1.getOnDemandInstans());
		assertEquals(sourceFilDetaljer1.getMetaforceInstanceId(), filDetaljerKopi1.getMetaforceInstanceId());
		assertThat(filDetaljerKopi1.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljerKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceFilDetaljer1.getBatchNavn(), filDetaljerKopi1.getBatchNavn());
		assertEquals(sourceFilDetaljer1.getFilnavn(), filDetaljerKopi1.getFilnavn());
		assertEquals(sourceFilDetaljer1.getFilstorrelse(), filDetaljerKopi1.getFilstorrelse());
		assertEquals(sourceFilDetaljer1.getSkjermingType(), filDetaljerKopi1.getSkjermingType());
		assertEquals(sourceFilDetaljer1.getEndretKildeNavn(), filDetaljerKopi1.getEndretKildeNavn());

		assertEquals(new String(sourceDokumentFil1.getFil()), new String(dokumentFilKopi1.getFil()));
		assertThat(dokumentFilKopi1.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));

		//Assert 2 sladdet
		Journalpost journalpostTilknyttetVedlegg2 = joarkRepository.findById(journalpostIdVedlegg).get();
		DokumentInfo sourceDokumentInfo2 = sourceJournalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo dokumentInfoKopi2 = journalpostTilknyttetVedlegg2.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.filter(j -> j.getDokumentInfo().getTilleggsopplysninger().containsKey(TILLEGGOPPLYSNINGER_KEY))
				.filter(d -> d.getDokumentInfo().getTilleggsopplysninger().containsValue(dokumentInfoId2.toString()))
				.findAny()
				.get()
				.getDokumentInfo();
		FilDetaljer sourceFilDetaljer2 = sourceDokumentInfo2.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET);
		FilDetaljer filDetaljerKopi2 = dokumentInfoKopi2.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV);
		DokumentFil sourceDokumentFil2 = dokumentFilRepository.findByFilUuid(sourceFilDetaljer2.getFilUuid());
		DokumentFil dokumentFilKopi2 = dokumentFilRepository.findByFilUuid(filDetaljerKopi2.getFilUuid());

		assertEquals(sourceDokumentInfo2.getDokumentstatus(), dokumentInfoKopi2.getDokumentstatus());
		assertEquals(sourceDokumentInfo2.getDokumentFerdigDato(), dokumentInfoKopi2.getDokumentFerdigDato());
		assertEquals(sourceDokumentInfo2.getTittel(), dokumentInfoKopi2.getTittel());
		assertEquals(sourceDokumentInfo2.getBrevkode(), dokumentInfoKopi2.getBrevkode());
		assertEquals(sourceDokumentInfo2.getDokumenttypeId(), dokumentInfoKopi2.getDokumenttypeId());
		assertEquals(sourceDokumentInfo2.getBrevgruppe(), dokumentInfoKopi2.getBrevgruppe());
		assertEquals(null, dokumentInfoKopi2.getOriginalJournalpost());
		assertEquals(sourceDokumentInfo2.getSensitivt(), dokumentInfoKopi2.getSensitivt());
		assertEquals(sourceDokumentInfo2.getInnskrenketPartsinnsyn(), dokumentInfoKopi2.getInnskrenketPartsinnsyn());
		assertEquals(sourceDokumentInfo2.getInnskrenketPartsinnsynFraTredjepart(), dokumentInfoKopi2.getInnskrenketPartsinnsynFraTredjepart());
		assertEquals(sourceDokumentInfo2.getOrganInternt(), dokumentInfoKopi2.getOrganInternt());
		assertEquals(sourceDokumentInfo2.getKonvertertFraSystem(), dokumentInfoKopi2.getKonvertertFraSystem());
		assertEquals(sourceDokumentInfo2.getEndretAvNavn(), dokumentInfoKopi2.getEndretAvNavn());
		assertEquals(sourceDokumentInfo2.getKassertAvNavn(), dokumentInfoKopi2.getKassertAvNavn());
		assertEquals(sourceDokumentInfo2.getDatoKassert(), dokumentInfoKopi2.getDatoKassert());
		assertThat(dokumentInfoKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceDokumentInfo2.getEndretKildeNavn(), dokumentInfoKopi2.getEndretKildeNavn());

		assertEquals(sourceFilDetaljer2.getFiltype(), filDetaljerKopi2.getFiltype());
		assertEquals(sourceFilDetaljer2.getOnDemandId(), filDetaljerKopi2.getOnDemandId());
		assertEquals(sourceFilDetaljer2.getOnDemandInstans(), filDetaljerKopi2.getOnDemandInstans());
		assertEquals(sourceFilDetaljer2.getMetaforceInstanceId(), filDetaljerKopi2.getMetaforceInstanceId());
		assertThat(filDetaljerKopi2.getVariantFormat(), is(VariantFormatCode.ARKIV));
		assertThat(filDetaljerKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));
		assertEquals(sourceFilDetaljer2.getBatchNavn(), filDetaljerKopi2.getBatchNavn());
		assertEquals(sourceFilDetaljer2.getFilnavn(), filDetaljerKopi2.getFilnavn());
		assertEquals(sourceFilDetaljer2.getFilstorrelse(), filDetaljerKopi2.getFilstorrelse());
		assertEquals(sourceFilDetaljer2.getSkjermingType(), filDetaljerKopi2.getSkjermingType());
		assertEquals(sourceFilDetaljer2.getEndretKildeNavn(), filDetaljerKopi2.getEndretKildeNavn());

		assertEquals(new String(sourceDokumentFil2.getFil()), new String(dokumentFilKopi2.getFil()));
		assertThat(dokumentFilKopi2.getOpprettetKildeNavn(), is(OPPRETTET_KILDE_NAVN));

		//Assert 3 Arkiv
		Journalpost journalpostTilknyttetVedlegg = joarkRepository.findById(journalpostIdVedlegg).get();
		assertThat(journalpostTilknyttetVedlegg.getJournalpostDokumentInfoRelasjoner()
				.stream()
				.anyMatch(j -> j.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoId3)), is(false));

		TestTransaction.end();
	}

	@Test
	public void shouldReturnForbiddenForWrongConsumer() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkiv" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnNotFoundForJournalpost() {

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

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

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
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

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		Long dokumentInfoId = sourceJournalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId();

		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId)
				.dokumentInfoId(dokumentInfoId.toString())
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiletDokument().get(0).getArsakKode(), is(ArsakFeilCode.UGYLDIG_STATUS));
		TestTransaction.end();
	}

	@Test
	public void shouldReturnFeiletDokumentListeAarsakKodeIkkeFunnet() {
		Journalpost journalpostVedlegg = createJournalpostArkiv();
		Journalpost sourceJournalpost = createJournalpostSladdet();
		Long journalpostIdVedlegg = joarkRepository.save(journalpostVedlegg).getJournalpostId();
		Long sourceJournalpostId = joarkRepository.save(sourceJournalpost).getJournalpostId();

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();


		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(sourceJournalpostId)
				.dokumentInfoId("200000345")
				.build());

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		String token = Base64Utils.encodeToString(
				("srvdokarkivproxy" + ":" + "hemmelig").getBytes(StandardCharsets.UTF_8));
		headers.add(HttpHeaders.AUTHORIZATION, "Basic " + token);

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity requestHttpEntity = new HttpEntity<>(request, headers);
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST_INTERN + journalpostIdVedlegg + "/tilknyttVedlegg", HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.MULTI_STATUS));
		assertThat(responseEntity.getBody().getFeiletDokument().get(0).getArsakKode(), is(ArsakFeilCode.IKKE_FUNNET));
		TestTransaction.end();
	}

	private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList) {
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("TilknyttVedleggIT")
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

	public void saveFil(Set<FilDetaljer> fd) {
		fd.stream().forEach(filDetaljer -> {
			DokumentFil dokumentFil = filDetaljer.createDokumentFil();
			dokumentFil.setOpprettetKildeNavn("kildenavn");
			dokumentFilRepository.save(dokumentFil);
		});
	}

}
