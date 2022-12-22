package no.nav.dokarkiv.rjoark102;


import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.jupiter.api.Test;
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

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_FIL_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_UUID_ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.util.TestUtil.KASSERT_AV_NAVN;
import static no.nav.dokarkiv.util.TestUtil.createKasserDokumentRequest;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Rjoark102IT extends AbstractAdminIT {

	@Test
	public void skalIkkeKassereDokumentNårDokmentInfoIkkeFinnes() throws IOException {
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
	public void skalKassereDokumentSomErKnyttetTilFlereJournalposter() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfoSomSkalKasseres.removeFilDetaljer(dokumentInfoSomSkalKasseres.findFilDetaljerByVariantFormat(ARKIV));
		dokumentInfoSomSkalKasseres.addFilDetaljer(createFildetaljerOgFil(dokumentInfoSomSkalKasseres, ARKIV, FIL_UUID_ARKIV));
		createVedleggRelasjon(journalpost2, dokumentInfoSomSkalKasseres);

		joarkRepository.save(journalpost1);
		joarkRepository.save(journalpost2);
		skjermingServiceTest.setDokumentKassert(dokumentInfoSomSkalKasseres, POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThat(dokumentInfoSomSkalKasseres.getFildetaljerListeAdmin().size(), is(2));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter", dokumentInfoTestRepository.count(), is(2L));
		assertTrue(dokumentInfoTestRepository.findByDokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId()).get().isRelatedToMultipleJournalposts());

		var responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoSomSkalKasseres
						.getDokumentInfoId()), createHeadersWithAksjon()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();


		Optional<DokumentInfo> dokumentInfoAfter = dokumentInfoTestRepository.findByDokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertThat(Duration.between(dokumentInfoAfter.get().getDatoKassert(), LocalDateTime.now()).toMillis(), lessThan(10000L));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid(), is(FIL_UUID_ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType(), nullValue());

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(2L));
		assertThat("Feil antall dokumenter etter kall", dokumentInfoTestRepository.count(), is(2L));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(2));

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost1.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId()), AksjonsTypeCode.KASSERING, journalpost1.getJournalpostId(), dokumentInfoSomSkalKasseres
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(PRODUKSJON.name())
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
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost2.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId()), AksjonsTypeCode.KASSERING, journalpost2.getJournalpostId(), dokumentInfoSomSkalKasseres
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
						.fraVerdi(SkjermingTypeCode.POL.name())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(PRODUKSJON.name())
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

		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfoSomSkalKasseres.removeFilDetaljer(dokumentInfoSomSkalKasseres.findFilDetaljerByVariantFormat(ARKIV));
		dokumentInfoSomSkalKasseres.addFilDetaljer(createFildetaljerOgFil(dokumentInfoSomSkalKasseres, ARKIV, FIL_UUID_ARKIV));

		joarkRepository.save(journalpost);
		skjermingServiceTest.setDokumentKassert(dokumentInfoSomSkalKasseres, POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoRep = dokumentInfoTestRepository.findByDokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertThat(dokumentInfoRep.get().getFildetaljerListeAdmin().size(), is(2));
		assertThat("Feil antall journalposter", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter", dokumentInfoTestRepository.count(), is(1L));
		assertFalse(dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfoSomSkalKasseres.getFildetaljerListe().isEmpty());

		var responseEntity = restTemplate.exchange(
				URL_KASSERDOKUMENT,
				HttpMethod.DELETE,
				new HttpEntity<>(createKasserDokumentRequest(dokumentInfoRep.get()
						.getDokumentInfoId()), createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentInfoTestRepository.findByDokumentInfoId(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn(), is(KASSERT_AV_NAVN));
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid(), is(FIL_UUID_ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType(), nullValue());

		assertThat("Feil antall journalposter etter kall", joarkRepository.count(), is(1L));
		assertThat("Feil antall dokumenter etter kall", dokumentInfoTestRepository.count(), is(1L));
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
