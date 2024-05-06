package no.nav.dokarkiv.rjoark102;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static java.util.Arrays.asList;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_FIL_FIL_UUID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_AV;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_KASSERT_DATO;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.fildetaljerSkjermingTypeVariant;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.KASSERING;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.util.TestDataGenerator.FIL_UUID_ARKIV;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createFildetaljerOgFil;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.util.TestUtil.KASSERT_AV_NAVN;
import static no.nav.dokarkiv.util.TestUtil.createKasserDokumentRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class Rjoark102IT extends AbstractAdminIT {

	@Test
	public void skalIkkeKassereDokumentNårDokmentInfoIkkeFinnes() {
		Long dokumentInfoId = 13L;

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(
				createKasserDokumentRequest(dokumentInfoId), createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		assertTrue(responseEntity.getBody().contains("Fant ikke dokument med dokumentInfoId=" + dokumentInfoId));
	}

	@Test
	public void skalKassereDokumentSomErKnyttetTilFlereJournalposter() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfoSomSkalKasseres.removeFilDetaljer(dokumentInfoSomSkalKasseres.findFilDetaljerByVariantFormat(ARKIV));
		dokumentInfoSomSkalKasseres.addFilDetaljer(createFildetaljerOgFil(dokumentInfoSomSkalKasseres, ARKIV, FIL_UUID_ARKIV));
		createVedleggRelasjon(journalpost2, dokumentInfoSomSkalKasseres);

		journalpostTestRepository.persist(journalpost1);
		journalpostTestRepository.persist(journalpost2);
		skjermingServiceTest.setDokumentKassert(dokumentInfoSomSkalKasseres, POL);

		reinitTransaction();

		assertThat(dokumentInfoSomSkalKasseres.getFildetaljerListeAdmin().size()).isEqualTo(2);
		assertThat(journalpostTestRepository.count()).isEqualTo(2); // Feil antall journalposter
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(2); // Feil antall dokumenter
		assertTrue(dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId()).get().isRelatedToMultipleJournalposts());

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(
						createKasserDokumentRequest(dokumentInfoSomSkalKasseres.getDokumentInfoId()),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn()).isEqualTo(KASSERT_AV_NAVN);
		assertThat(Duration.between(dokumentInfoAfter.get().getDatoKassert(), now()).toMillis()).isLessThan(10000L);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size()).isEqualTo(1);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid()).isEqualTo(FIL_UUID_ARKIV);
		;
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat()).isEqualTo(ARKIV);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType()).isNull();

		assertThat(journalpostTestRepository.count()).isEqualTo(2); // Feil antall journalposter etter kall
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(2); // Feil antall dokumenter etter kall

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost1.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId()), KASSERING, journalpost1.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
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
								.tilVerdi(dokumentInfoAfter.get().getDatoKassert().format(ISO_DATE_TIME))
								.build()
				)
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost2.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId()), KASSERING, journalpost2.getJournalpostId(), dokumentInfoSomSkalKasseres.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(fildetaljerSkjermingTypeVariant(ARKIV))
								.fraVerdi(POL.name())
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
								.tilVerdi(dokumentInfoAfter.get().getDatoKassert().format(ISO_DATE_TIME))
								.build()
				)
		);
	}


	@Test
	public void skalKassereDokumentMedSomErKnyttetTilEnJournalpost() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfoSomSkalKasseres.removeFilDetaljer(dokumentInfoSomSkalKasseres.findFilDetaljerByVariantFormat(ARKIV));
		dokumentInfoSomSkalKasseres.addFilDetaljer(createFildetaljerOgFil(dokumentInfoSomSkalKasseres, ARKIV, FIL_UUID_ARKIV));

		journalpostTestRepository.persist(journalpost);
		skjermingServiceTest.setDokumentKassert(dokumentInfoSomSkalKasseres, POL);

		reinitTransaction();

		Optional<DokumentInfo> dokumentInfoRep = dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertThat(dokumentInfoRep.get().getFildetaljerListeAdmin().size()).isEqualTo(2);
		assertThat(journalpostTestRepository.count()).isEqualTo(1); // Feil antall journalposter
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(1); // Feil antall dokumenter
		assertFalse(dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfoSomSkalKasseres.getFildetaljerListe().isEmpty());

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(
				createKasserDokumentRequest(dokumentInfoRep.get().getDokumentInfoId()),
				createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)), String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn()).isEqualTo(KASSERT_AV_NAVN);
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size()).isEqualTo(1);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid()).isEqualTo(FIL_UUID_ARKIV);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat()).isEqualTo(ARKIV);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType()).isNull();

		assertThat(journalpostTestRepository.count()).isEqualTo(1); // Feil antall journalposter etter kall
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(1); // Feil antall dokumenter etter kall
	}

	@Test
	public void skalKassereDokumentSomErKnyttetTilEnJournalpostForStsTokenFraJoarkadmin() {
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalKasseres = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		dokumentInfoSomSkalKasseres.removeFilDetaljer(dokumentInfoSomSkalKasseres.findFilDetaljerByVariantFormat(ARKIV));
		dokumentInfoSomSkalKasseres.addFilDetaljer(createFildetaljerOgFil(dokumentInfoSomSkalKasseres, ARKIV, FIL_UUID_ARKIV));

		journalpostTestRepository.persist(journalpost);
		skjermingServiceTest.setDokumentKassert(dokumentInfoSomSkalKasseres, POL);

		reinitTransaction();

		Optional<DokumentInfo> dokumentInfoRep = dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoRep.isPresent());
		assertThat(dokumentInfoRep.get().getFildetaljerListeAdmin().size()).isEqualTo(2);
		assertThat(journalpostTestRepository.count()).isEqualTo(1); // Feil antall journalposter
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(1); // Feil antall dokumenter
		assertFalse(dokumentInfoSomSkalKasseres.isRelatedToMultipleJournalposts());
		assertFalse(dokumentInfoSomSkalKasseres.getFildetaljerListe().isEmpty());

		var httpHeaders = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_JOARKADMIN);

		var responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(
				createKasserDokumentRequest(dokumentInfoRep.get().getDokumentInfoId()),
				httpHeaders), String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		Optional<DokumentInfo> dokumentInfoAfter = dokumentInfoTestRepository.findById(dokumentInfoSomSkalKasseres.getDokumentInfoId());
		assertTrue(dokumentInfoAfter.isPresent());
		assertThat(dokumentInfoAfter.get().getKassertAvNavn()).isEqualTo(KASSERT_AV_NAVN);
		assertNotNull(dokumentInfoAfter.get().getDatoKassert());
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().size()).isEqualTo(1);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getFilUuid()).isEqualTo(FIL_UUID_ARKIV);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getVariantFormat()).isEqualTo(ARKIV);
		assertThat(dokumentInfoAfter.get().getFildetaljerListe().iterator().next().getSkjermingType()).isNull();

		assertThat(journalpostTestRepository.count()).isEqualTo(1); // Feil antall journalposter etter kall
		assertThat(dokumentInfoTestRepository.count()).isEqualTo(1); // Feil antall dokumenter etter kall
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et on behalf of-token");
	}

	@Test
	public void skalReturnereUnauthorizedHvisTokenErEtClientCredentialToken() {
		var headers = createAuthorizationHeadersClientCredentialGrant();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et on behalf of-token");
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeAppIkkeErJoarkadmin() {
		var headers = createAuthorizationHeaders(AZP_NAME_DOKMET, MS_USER_ID_WITH_GROUP_ACCESS);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må tilhøre en av følgende apper");
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		var headers = createAuthorizationHeaders(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_KASSERDOKUMENT, DELETE, new HttpEntity<>(createKasserDokumentRequest(123L), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

}