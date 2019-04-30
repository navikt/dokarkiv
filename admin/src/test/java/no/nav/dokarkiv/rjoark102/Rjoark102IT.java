package no.nav.dokarkiv.rjoark102;


import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_FIL_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static no.nav.dokarkiv.util.TestUtil.FIL_UUID_ARKIV;
import static no.nav.dokarkiv.util.TestUtil.KASSERT_AV_NAVN;
import static no.nav.dokarkiv.util.TestUtil.createKasserDokumentRequest;
import static no.nav.dokarkiv.util.TestUtil.knyttDokumentInfoSomVedleggTilJournalpost;
import static no.nav.dokarkiv.util.TestUtil.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Rjoark102IT extends AbstractAdminIT {

	@Test
	public void skallIkkeKassereDokumentNårDokmentInfoIkkeFinnes() throws IOException {
		abacPermit();

		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoId), createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skallKassereDokumentSomErKnyttetTilFlereJournalposter() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		knyttDokumentInfoSomVedleggTilJournalpost(dokumentInfoSomSkalKasseres, journalpost2);
		joarkRepository.save(journalpost2);
		skjermingService.skjermAllFildetaljer(dokumentInfoSomSkalKasseres, POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(dokumentInfoSomSkalKasseres.getFildetaljerListeAdmin().size(), is(2));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(2L));
		assertTrue(dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts());

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoSomSkalKasseres
						.getDokumentInfoId()), createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();


		Optional<DokumentInfo> dokumentInfoAfter = dokumentinfoRepository.findByDokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertThat(Duration.between(dokumentInfoAfter.get().getDatoKassert(), LocalDateTime.now()).toMillis(), lessThan(10000L));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid(), is(FIL_UUID_ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType(), nullValue());

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(2L));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByDokumentInfoId(aksjonsLoggList, dokumentInfoSomSkalKasseres.getDokumentInfoId()), AksjonsTypeCode.KASSASJON, null, dokumentInfoSomSkalKasseres
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(SLADDET.name())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_FIL_FIL_UUID)
						.fraVerdi(FIL_UUID_ARKIV)
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_KASSERT_AV)
						.fraVerdi(null)
						.tilVerdi(KASSERT_AV_NAVN)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_KASSERT_DATO)
						.fraVerdi(null)
						.tilVerdi(dokumentInfoAfter.get().getDatoKassert().format(DateTimeFormatter.ISO_DATE_TIME))
						.build()

		));
	}


	@Test
	public void skalKassereDokumentMedSomErKnyttetTilEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		skjermingService.skjermAllFildetaljer(dokumentInfo, POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoRep = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertThat(dokumentInfoRep.get().getFildetaljerListeAdmin().size(), is(2));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter", dokumentinfoRepository.count(), is(1L));
		assertFalse(dokumentInfo.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfo.getFildetaljerListe().isEmpty());

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoRep.get()
						.getDokumentInfoId()), createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid(), is(FIL_UUID_ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType(), nullValue());

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter etter kall", dokumentinfoRepository.count(), is(1L));
	}

	@Test
	public void skalIkkeFåTilgangHvisServiceBrukerIkkeErSrvJoarkadmin() {
		abacPermit();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(123L), createHeadersWithServiceUserToken(NO_ACCESS_SERVICE_USER_ID)),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}
}
