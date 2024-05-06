package no.nav.dokarkiv.rjoark101;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_MELDING_HEADER;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_TILKNYTTET_SOM;
import static no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode.SLETTING;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.DOKUMENT_FIL;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.DOKUMENT_INFO;
import static no.nav.dokarkiv.core.domain.codes.ArkivenhetCode.JOURNALPOST;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithSplittetHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createNavNoUtsendingsInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

public class Rjoark101IT extends AbstractAdminIT {
	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgEttVedlegg() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		saveJournalpost(journalpost);
		utsendingsInfoTestRepository.persist(createNavNoUtsendingsInfo(journalpost));

		createDokumentInfoVedleggRelasjon(journalpost);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpost);

		HttpHeaders httpHeaders = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);
		httpHeaders.remove(AKSJONS_LOGG_MELDING_HEADER);
		var responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpost.getJournalpostId())
						.build(), httpHeaders),
				String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThatJournalpostIsDeleted(journalpost.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
		}

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(2);
		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		Long dokInfoIdVedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long dokInfoHoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), dokInfoIdVedlegg), SLETTING, journalpost.getJournalpostId(), dokInfoIdVedlegg,
				format("Journalpost med journalpostId %s knyttet til dokumentInfoId(er) %s, %s er fysisk slettet og kan ikke gjenopprettes lenger.", journalpost.getJournalpostId(), dokInfoHoveddok, dokInfoIdVedlegg),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpost.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoIdVedlegg.toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoIdVedlegg.toString())
								.tilVerdi(null)
								.build())
		);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), dokInfoHoveddok), SLETTING, journalpost.getJournalpostId(), dokInfoHoveddok,
				format("Journalpost med journalpostId %s knyttet til dokumentInfoId(er) %s, %s er fysisk slettet og kan ikke gjenopprettes lenger.", journalpost.getJournalpostId(), dokInfoHoveddok, dokInfoIdVedlegg),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpost.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build())
		);
	}

	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgEttVedleggForStsTokenFraJoarkadmin() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		saveJournalpost(journalpost);
		utsendingsInfoTestRepository.persist(createNavNoUtsendingsInfo(journalpost));

		createDokumentInfoVedleggRelasjon(journalpost);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpost);

		var httpHeaders = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_JOARKADMIN);
		httpHeaders.remove(AKSJONS_LOGG_MELDING_HEADER);

		var responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpost.getJournalpostId())
						.build(), httpHeaders),
				String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThatJournalpostIsDeleted(journalpost.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
		}

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(2);
		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		Long dokInfoIdVedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();
		Long dokInfoHoveddok = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		assertAksjonsLoggForSts(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), dokInfoIdVedlegg), SLETTING, journalpost.getJournalpostId(), dokInfoIdVedlegg,
				format("Journalpost med journalpostId %s knyttet til dokumentInfoId(er) %s, %s er fysisk slettet og kan ikke gjenopprettes lenger.", journalpost.getJournalpostId(), dokInfoHoveddok, dokInfoIdVedlegg),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpost.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoIdVedlegg.toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoIdVedlegg.toString())
								.tilVerdi(null)
								.build())
		);

		assertAksjonsLoggForSts(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpost.getJournalpostId(), dokInfoHoveddok), SLETTING, journalpost.getJournalpostId(), dokInfoHoveddok,
				format("Journalpost med journalpostId %s knyttet til dokumentInfoId(er) %s, %s er fysisk slettet og kan ikke gjenopprettes lenger.", journalpost.getJournalpostId(), dokInfoHoveddok, dokInfoIdVedlegg),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpost.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build())
		);
	}

	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgVedleggSomErGjenbruktFraEnAnnenJournalpost() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpostSomSkalSlettes = createUniqueJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedleggGjenbrukt = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpostSomSkalSlettes.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpostSomSkalSlettes, dokumentInfoVedleggGjenbrukt));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpostSomSkalSlettes);


		//Simuler skjerming. Skjerming skal ikke påvirke hvordan slettingen utføres
		skjermingServiceTest.setJournalpostSkjerming(journalpostSomSkalSlettes.getJournalpostId(), POL);
		skjermingServiceTest.skjermAllFildetaljer(journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo(), POL);
		journalpostSomSkalSlettes.getJournalpostDokumentInfoRelasjonerAdmin().stream().filter(JournalpostDokumentInfoRelasjon::isVedlegg).forEach(rel -> skjermingServiceTest.setJpDokInfoRelSkjerming(rel.getJournalpostDokumentInfoRelasjonId(), POL));

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedleggGjenbrukt.getDokumentInfoId()).size()).isEqualTo(2);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThatJournalpostIsDeleted(journalpostSomSkalSlettes.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpostSomSkalSlettes.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoVedleggGjenbrukt.getDokumentInfoId())) {
				assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
			} else {
				assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
			}
		}

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(2);

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId()).size()).isEqualTo(1);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		Long dokInfoIdVedlegg = journalpostSomSkalSlettes.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostSomSkalSlettes.getJournalpostId(), dokInfoIdVedlegg), SLETTING, journalpostSomSkalSlettes.getJournalpostId(), dokInfoIdVedlegg,
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpostSomSkalSlettes.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoIdVedlegg.toString())
								.tilVerdi(null)
								.build())
		);

		Long dokInfoHoveddok = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId();
		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostSomSkalSlettes.getJournalpostId(), dokInfoHoveddok), SLETTING, journalpostSomSkalSlettes.getJournalpostId(), dokInfoHoveddok,
				asList(
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpostSomSkalSlettes.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokInfoHoveddok.toString())
								.tilVerdi(null)
								.build())
		);
	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten har hoveddokument som er brukt som vedlegg i andre journalposter (gjenbrukt).
	 * I slike tilfeller må relasjonene hvor hoveddokument er vedlegg slettes før journalposten kan slettes.
	 */
	@Test
	public void skalFeileVedSlettingAvJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedlegg() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedlegg = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, dokumentInfoVedlegg));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId()).size()).isEqualTo(2);

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpost1.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_ACCEPTABLE);
		assertTrue(responseEntity.getBody().getMessage().contains("Hoveddokument er tilknyttet andre journalposter."));

		reinitTransaction();

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(3);

		assertThatJournalpostIsNotDeleted(journalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost);

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId()).size()).isEqualTo(2);
	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten er Splittet
	 */
	@Test
	public void skalIkkeSletteJournalpostSomErSplittet() {
		Journalpost journalpostOriginal = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpostSplit1 = createJournalpostWithSplittetHoveddokument(journalpostOriginal);
		Journalpost journalpostSplit2 = createJournalpostWithSplittetHoveddokument(journalpostOriginal);

		saveJournalpost(journalpostOriginal);
		saveJournalpost(journalpostSplit1);
		saveJournalpost(journalpostSplit2);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostOriginal);
		assertThatJournalpostIsNotDeleted(journalpostSplit1);
		assertThatJournalpostIsNotDeleted(journalpostSplit2);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostOriginal.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_ACCEPTABLE);

		//Journalposter som inneholder splittene skal være mulig å slette
		ResponseEntity<String> responseEntity1 = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostSplit1.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity1.getStatusCode()).isEqualTo(OK);

		ResponseEntity<String> responseEntity2 = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostSplit2.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity2.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();

		assertThat(journalpostListAfter.size()).isEqualTo(1);
		assertThatJournalpostIsNotDeleted(journalpostOriginal);
		assertThatJournalpostIsDeleted(journalpostSplit1.getJournalpostId());
		assertThatJournalpostIsDeleted(journalpostSplit2.getJournalpostId());
	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten har hoveddokument som er brukt som vedlegg i andre journalposter (gjenbrukt).
	 * I slike tilfeller må relasjonene hvor hoveddokument er vedlegg slettes før journalposten kan slettes.
	 * Denne testen skal verifisere at Journalposten kan slettes etter at vedlegg relasjonene til hoveddokumentet er slettet.
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedleggEtterSlettingAvRelasjonene() {
		Journalpost journalpostMedDokumentSomVedlegg = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpostSomSkalSlettes = createUniqueJournalpostWithHoveddokument();
		createDokumentInfoVedleggRelasjon(journalpostSomSkalSlettes);

		DokumentInfo dokumentInfoHoveddokument = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		createVedleggRelasjon(journalpostMedDokumentSomVedlegg, dokumentInfoHoveddokument);

		saveJournalpost(journalpostSomSkalSlettes);
		saveJournalpost(journalpostMedDokumentSomVedlegg);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();
		assertThat(journalpostList.size()).isEqualTo(3);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_ACCEPTABLE);

		//Slett hoveddokument
		ResponseEntity<String> responseEntity2 = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity2.getStatusCode()).isEqualTo(OK);

		//Utfør samme kall som første kallet i testen
		ResponseEntity<String> responseEntity3 = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity3.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(dokumentInfoHoveddokument);
		assertThatDokumentInfoIsDeleted(journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(2);

		assertThatJournalpostIsDeleted(journalpostSomSkalSlettes.getJournalpostId());

		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomVedlegg);
		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);
	}

	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalFeileHvisJournalpostIkkeFinnes() {
		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(JOURNALPOST)
						.journalpostId(1L)
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
	}

	//Tester med arkivEnhet=DOKUMENT_INFO

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteDokumentInfoSomErBareTilknyttetEnJournalpost() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createUniqueJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon hoveddokumentRelasjon = journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon();
		JournalpostDokumentInfoRelasjon vedleggRelasjonSomSlettes = createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);
		JournalpostDokumentInfoRelasjon vedleggRelasjon2 = createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);

		//Simuler at både vedlegg og hoveddokument i journalpost er skjermet. Skjerming skal ikke påvirke hvordan sletting utføres
		skjermingServiceTest.setJpDokInfoRelSkjerming(vedleggRelasjonSomSlettes.getJournalpostDokumentInfoRelasjonId(), POL);
		skjermingServiceTest.setJpDokInfoRelSkjerming(vedleggRelasjon2.getJournalpostDokumentInfoRelasjonId(), POL);
		//HOVEDDOKUMENT skjermes ved å skjerme fildetaljer
		skjermingServiceTest.skjermAllFildetaljer(hoveddokumentRelasjon.getDokumentInfo(), POL);
		skjermingService.setJournalpostSkjerming(journalpostMedDokumentSomSkalSlettes.getJournalpostId(), POL);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();
		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).size()).isEqualTo(3);

		HttpHeaders httpHeaders = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);
		httpHeaders.remove(AKSJONS_LOGG_MELDING_HEADER);
		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(vedleggRelasjonSomSlettes.getDokumentInfo().getDokumentInfoId())
						.build(),
						httpHeaders),
				String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(vedleggRelasjonSomSlettes.getDokumentInfo());

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).size()).isEqualTo(2);

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId()), SLETTING, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), vedleggRelasjonSomSlettes.getDokumentInfo().getDokumentInfoId(),
				format("Dokumentet knyttet til journalpostId(er) %s er fysisk slettet i alle steder der det forekom og kan ikke gjenopprettes lenger.", journalpostMedDokumentSomSkalSlettes.getJournalpostId()),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(vedleggRelasjonSomSlettes.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(vedleggRelasjonSomSlettes.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build()

				)
		);
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteHoveddokumentOgBytteVedleggRelasjonTilHoveddokumentForJournalpostMedFlereRelasjoner() {
		Journalpost journalpost1 = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		JournalpostDokumentInfoRelasjon vedleggRelasjon = createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);

		//Simuler at både vedlegg og hoveddokument i journalpost er skjermet
		skjermingServiceTest.setJpDokInfoRelSkjerming(vedleggRelasjon.getJournalpostDokumentInfoRelasjonId(), POL);
		//HOVEDDOKUMENT skjermes ved å skjerme fildetaljer
		skjermingServiceTest.skjermAllFildetaljer(dokumentInfoSomSkalSlettes, POL);
		skjermingService.setJournalpostSkjerming(journalpostMedDokumentSomSkalSlettes.getJournalpostId(), POL);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();
		assertThat(journalpostList.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).size()).isEqualTo(2);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(dokumentInfoSomSkalSlettes);

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(3);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);

		Journalpost journalpostMedDokumentSomSkalSlettesAfter = journalpostTestRepository.findById(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).get();
		JournalpostDokumentInfoRelasjon relHoveddokAfter = journalpostMedDokumentSomSkalSlettesAfter.getJournalpostDokumentInfoRelasjonerAdmin().stream().filter(rel -> rel.getTilknyttetJournalpostSom() == HOVEDDOKUMENT).findAny().get();
		assertThat(relHoveddokAfter.getDokumentInfo().getDokumentInfoId()).isEqualTo(vedleggRelasjon.getDokumentInfo().getDokumentInfoId());
		assertThat(journalpostMedDokumentSomSkalSlettesAfter.getJournalpostDokumentInfoRelasjonerAdmin().size()).isEqualTo(1);

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoSomSkalSlettes.getDokumentInfoId()),
				SLETTING, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoSomSkalSlettes.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
								.tilVerdi(null)
								.build()

				)
		);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), vedleggRelasjon.getDokumentInfo().getDokumentInfoId()), SLETTING,
				journalpostMedDokumentSomSkalSlettes.getJournalpostId(), vedleggRelasjon.getDokumentInfo().getDokumentInfoId(),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(RELASJON_TILKNYTTET_SOM)
						.fraVerdi(VEDLEGG.name())
						.tilVerdi(HOVEDDOKUMENT.name())
						.build()
				)
		);
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteVedleggOgDeretterHoveddokumentForJournalpostMedEnHoveddokumentOgEnVedlegg() {
		Journalpost journalpostMedDokumentSomSkalSlettes = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		JournalpostDokumentInfoRelasjon vedleggRelasjon = createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);

		//Simuler at både vedlegg og hoveddokument i journalpost er skjermet
		skjermingServiceTest.setJpDokInfoRelSkjerming(vedleggRelasjon.getJournalpostDokumentInfoRelasjonId(), POL);
		//HOVEDDOKUMENT skjermes ved å skjerme fildetaljer
		skjermingServiceTest.skjermAllFildetaljer(dokumentInfoSomSkalSlettes, POL);
		skjermingService.setJournalpostSkjerming(journalpostMedDokumentSomSkalSlettes.getJournalpostId(), POL);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();
		assertThat(journalpostList.size()).isEqualTo(1);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).size()).isEqualTo(2);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntityVedlegg = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(vedleggRelasjon.getDokumentInfo().getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntityVedlegg.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(vedleggRelasjon.getDokumentInfo());

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(1);
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);

		assertThat(journalpostListAfter.get(0).getJournalpostDokumentInfoRelasjonerAdmin().size()).isEqualTo(1);
		assertThat(journalpostListAfter.get(0).getJournalpostDokumentInfoRelasjonerAdmin().iterator().next().getTilknyttetJournalpostSom()).isEqualTo(HOVEDDOKUMENT);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntityHoveddokument = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntityHoveddokument.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(dokumentInfoSomSkalSlettes);
		assertThatDokumentInfoIsDeleted(dokumentInfoSomSkalSlettes);

		List<Journalpost> journalpostListAfterHoveddokument = journalpostTestRepository.findAll();
		assertThat(journalpostListAfterHoveddokument.size()).isEqualTo(0);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), vedleggRelasjon.getDokumentInfo().getDokumentInfoId()), SLETTING,
				journalpostMedDokumentSomSkalSlettes.getJournalpostId(), vedleggRelasjon.getDokumentInfo().getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(vedleggRelasjon.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(vedleggRelasjon.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build()

				)
		);

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoSomSkalSlettes.getDokumentInfoId()), SLETTING,
				journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoSomSkalSlettes.getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpostMedDokumentSomSkalSlettes.getJournalpostId().toString())
								.tilVerdi(null)
								.build()

				)
		);
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteDokumentInfoSomErEnesteDokumentPåEnJournalpostOgVedleggPåEnAnnenJournalpost() {
		Journalpost journalpostSomHarDokumentSomVedlegg = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost2 = createUniqueJournalpostWithHoveddokument();

		Journalpost journalpostSomHarDokumentSomHoveddok = createUniqueJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = TestDataGenerator.createVedleggRelasjon(journalpostSomHarDokumentSomVedlegg, journalpostSomHarDokumentSomHoveddok.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		saveJournalpost(journalpostSomHarDokumentSomHoveddok);
		saveJournalpost(journalpostSomHarDokumentSomVedlegg);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(3);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByDokumentInfoDokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId()).size()).isEqualTo(2);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg.getJournalpostId()).size()).isEqualTo(2);
		assertThatJournalpostIsNotDeleted(journalpostSomHarDokumentSomHoveddok);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatJournalpostIsDeleted(journalpostSomHarDokumentSomHoveddok.getJournalpostId());
		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		List<Journalpost> journalpostListAfter = journalpostTestRepository.findAll();
		assertThat(journalpostListAfter.size()).isEqualTo(2);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg.getJournalpostId()).size()).isEqualTo(1);
		assertThat(journalpostDokumentInfoRelasjonTestRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg.getJournalpostId())
				.get(0)
				.getDokumentInfo()
				.getDokumentInfoId()).isNotEqualTo(relasjonVedlegg.getDokumentInfo().getDokumentInfoId());
		assertThatJournalpostIsNotDeleted(journalpostSomHarDokumentSomVedlegg);
		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(2);

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostSomHarDokumentSomVedlegg.getJournalpostId()), SLETTING, journalpostSomHarDokumentSomVedlegg.getJournalpostId(), relasjonVedlegg.getDokumentInfo().getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build()

				)
		);
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostSomHarDokumentSomHoveddok.getJournalpostId()), SLETTING, journalpostSomHarDokumentSomHoveddok.getJournalpostId(), relasjonVedlegg.getDokumentInfo().getDokumentInfoId(),
				asList(
						ArkivElementEndring.builder()
								.arkivElement(RELASJON_DOKUMENT_INFO_ID)
								.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(JOURNALPOST_JOURNALPOST_ID)
								.fraVerdi(journalpostSomHarDokumentSomHoveddok.getJournalpostId().toString())
								.tilVerdi(null)
								.build(),
						ArkivElementEndring.builder()
								.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
								.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
								.tilVerdi(null)
								.build()
				)
		);
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalFeileVedSlettingAvDokumentInfoSomErSplittet() {
		Journalpost origJournalpost = createUniqueJournalpostWithHoveddokument();
		Journalpost journalpost = createJournalpostWithSplittetHoveddokument(origJournalpost);
		saveJournalpost(origJournalpost);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(2);
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = dokumentInfoTestRepository.findAll();
		assertThat(dokumentInfoList.size()).isEqualTo(2);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(origJournalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId())
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_ACCEPTABLE);

		reinitTransaction();
		assertThatDokumentInfoAndFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		assertThatJournalpostIsNotDeleted(origJournalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(origJournalpost);

		assertThatJournalpostIsNotDeleted(journalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost);

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(0);
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalFeileHvisDokumentInfoIkkeFinnes() {
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_INFO)
						.dokumentInfoId(1L)
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);
		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(0);
	}

	//Tester med arkivEnhet=DOKUMENT_FIL

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalSletteFilOgFildetaljer() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoMedVariantSomSkalSlettes = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		saveJournalpost(journalpost);
		skjermingServiceTest.setVariantSkjermet(dokumentInfoMedVariantSomSkalSlettes.getDokumentInfoId(), ARKIV, POL);

		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();

		assertThat(journalpostList.size()).isEqualTo(1);
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = dokumentInfoTestRepository.findAll();
		assertThat(dokumentInfoList.size()).isEqualTo(1);

		HttpHeaders httpHeaders = createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS);
		httpHeaders.remove(AKSJONS_LOGG_MELDING_HEADER);
		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_FIL)
						.dokumentInfoId(dokumentInfoMedVariantSomSkalSlettes
								.getDokumentInfoId())
						.variant(ARKIV)
						.build(),
						httpHeaders),
				String.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		reinitTransaction();

		assertThatDokumentInfoIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes);
		assertThatFildetaljerIsDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(ARKIV));
		assertThatFildetaljerIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(PRODUKSJON));

		List<AksjonsLogg> aksjonsLoggList = aksjonsLoggTestRepository.findAll();
		assertThat(aksjonsLoggList.size()).isEqualTo(1);

		assertAksjonsLogg(aksjonsLoggList.get(0), SLETTING, journalpost.getJournalpostId(), dokumentInfoMedVariantSomSkalSlettes.getDokumentInfoId(),
				format("Dokumentfil knyttet til dokumentInfoId %s med variant ARKIV er fysisk slettet og kan ikke gjenopprettes lenger.", dokumentInfoMedVariantSomSkalSlettes.getDokumentInfoId()),
				singletonList(ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(ARKIV.name())
						.tilVerdi(null)
						.build()
				)
		);
	}

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalFeileHvisVariantSomSkalSlettesIkkeFinnes() {
		Journalpost journalpost = createUniqueJournalpostWithHoveddokument();
		saveJournalpost(journalpost);
		reinitTransaction();

		List<Journalpost> journalpostList = journalpostTestRepository.findAll();
		assertThat(journalpostList.size()).isEqualTo(1);
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = dokumentInfoTestRepository.findAll();
		assertThat(dokumentInfoList.size()).isEqualTo(1);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE,
				new HttpEntity<>(SlettArkivenhetRequest.builder()
						.arkivenhet(DOKUMENT_FIL)
						.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId())
						.variant(VariantFormatCode.SLADDET)
						.build(),
						createHeadersWithAksjonslogg(AZP_NAME_JOARKADMIN, MS_USER_ID_WITH_GROUP_ACCESS)),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode()).isEqualTo(NOT_FOUND);

		reinitTransaction();

		assertThatDokumentInfoIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		assertThatFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(ARKIV));
		assertThatFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().findFilDetaljerByVariantFormat(PRODUKSJON));
	}

	@Test
	public void skalReturnereUnauthorizedHvisStsTokenIkkeErFraJoarkadmin() {
		var headers = createHeadersWithServiceUserAndAksjonslogg(SERVICEUSER_IKKE_JOARKADMIN);
		headers.remove(AKSJONS_LOGG_MELDING_HEADER);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE, new HttpEntity<>(SlettArkivenhetRequest.builder()
				.arkivenhet(JOURNALPOST)
				.journalpostId(Long.valueOf("123"))
				.build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et on behalf of-token");
	}

	@Test
	public void skalReturnereUnauthorizedHvisTokenErEtClientCredentialToken() {
		var headers = createAuthorizationHeadersClientCredentialGrant();

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE, new HttpEntity<>(SlettArkivenhetRequest.builder()
				.arkivenhet(JOURNALPOST)
				.journalpostId(Long.valueOf("123"))
				.build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må være et on behalf of-token");
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeAppIkkeErJoarkadmin() {
		var headers = createAuthorizationHeaders(AZP_NAME_DOKMET, MS_USER_ID_WITH_GROUP_ACCESS);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE, new HttpEntity<>(SlettArkivenhetRequest.builder()
				.arkivenhet(JOURNALPOST)
				.journalpostId(Long.valueOf("123"))
				.build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("OIDC-token på Authorization-header må tilhøre en av følgende apper");
	}

	@Test
	public void skalReturnereUnauthorizedHvisKallendeBrukerManglerRiktigGruppe() {
		var headers = createAuthorizationHeaders(AZP_NAME_JOARKADMIN, MS_USER_ID_WITHOUT_GROUP_ACCESS);

		ResponseEntity<String> responseEntity = restTemplate.exchange(URL_SLETTARKIVENHET, DELETE, new HttpEntity<>(SlettArkivenhetRequest.builder()
				.arkivenhet(JOURNALPOST)
				.journalpostId(Long.valueOf("123"))
				.build(), headers), String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(UNAUTHORIZED);
		assertThat(responseEntity.getBody()).contains("NAV-ansatt må være medlem av gruppen");
	}

}