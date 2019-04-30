package no.nav.dokarkiv.rjoark101;

import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.DOKUMENT_INFO_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.FILDETALJER_VARIANTFORMAT;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.JOURNALPOST_JOURNALPOST_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_DOKUMENT_INFO_ID;
import static no.nav.dokarkiv.core.aksjonslogg.ArkivElementConstants.RELASJON_TILKNYTTET_SOM;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithSplittetHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unchecked")
public class Rjoark101IT extends AbstractAdminIT {


	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgEnVedleggMedIngenRelasjoner() throws IOException {
		abacPermit();
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		createDokumentInfoVedleggRelasjon(journalpost);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);

		ResponseEntity responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThatJournalpostIsDeleted(journalpost.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
		}

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));
		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.SLETT, journalpost
				.getJournalpostId(), null, Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_JOURNALPOST_ID)
						.fraVerdi(journalpost.getJournalpostId().toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build()
		));
	}

	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgRelasjonerTilAndreDokumentInfoerSomVedlegg() throws IOException {
		abacPermit();
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		Journalpost journalpost = createJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedlegg = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, dokumentInfoVedlegg));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);

		reinitTransaction();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThatJournalpostIsDeleted(journalpost.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoVedlegg.getDokumentInfoId())) {
				assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
			} else {
				assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
			}
		}

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpost.getJournalpostId()), AksjonsTypeCode.SLETT, journalpost
				.getJournalpostId(), null, Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(dokumentInfoVedlegg.getDokumentInfoId().toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_JOURNALPOST_ID)
						.fraVerdi(journalpost.getJournalpostId().toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build()
		));

	}

	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentOgRelasjonerTilAndreDokumentInfoerSomVedleggNårJournalpostErSkjermet() throws IOException {
		abacPermit();
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		Journalpost journalpostSomSkalSlettes = createJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedlegg = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpostSomSkalSlettes.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpostSomSkalSlettes, dokumentInfoVedlegg));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpostSomSkalSlettes);

		skjermingService.setJournalpostSkjerming(journalpostSomSkalSlettes.getJournalpostId(), SkjermingTypeCode.POL);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		assertThatJournalpostIsDeleted(journalpostSomSkalSlettes.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpostSomSkalSlettes.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoVedlegg.getDokumentInfoId())) {
				assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
			} else {
				assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
			}
		}

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten har hoveddokument som er brukt som vedlegg i andre journalposter (gjenbrukt).
	 * I slike tilfeller må relasjonene hvor hoveddokument er vedlegg slettes før journalposten kan slettes.
	 */
	@Test
	public void skalFeileVedSlettingAvJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedlegg() throws IOException {
		abacPermit();
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		Journalpost journalpost = createJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedlegg = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, dokumentInfoVedlegg));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));

		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost1.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));
		assertThat(responseEntity.getBody().getMessage(), containsString("Hoveddokument er tilknyttet andre journalposter."));

		reinitTransaction();

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(3));

		assertThatJournalpostIsNotDeleted(journalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost);

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));
	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten er Splittet
	 */
	@Test
	public void skalIkkeSletteJournalposterSomErSplittet() throws IOException {
		abacPermit();

		Journalpost journalpostOriginal = createJournalpostWithHoveddokument();
		Journalpost journalpostSplit1 = createJournalpostWithSplittetHoveddokument(journalpostOriginal);
		Journalpost journalpostSplit2 = createJournalpostWithSplittetHoveddokument(journalpostOriginal);

		saveJournalpost(journalpostOriginal);
		saveJournalpost(journalpostSplit1);
		saveJournalpost(journalpostSplit2);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostOriginal);
		assertThatJournalpostIsNotDeleted(journalpostSplit1);
		assertThatJournalpostIsNotDeleted(journalpostSplit2);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostOriginal.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));

		ResponseEntity<String> responseEntity1 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSplit1.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));

		ResponseEntity<String> responseEntity2 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSplit2.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostListAfter.size(), is(1));
		assertThatJournalpostIsNotDeleted(journalpostOriginal);
		assertThatJournalpostIsDeleted(journalpostSplit1.getJournalpostId());
		assertThatJournalpostIsDeleted(journalpostSplit2.getJournalpostId());

		TestTransaction.end();
	}

	/**
	 * Slett tjenesten med arkivEnhet=JOURNALPOST skal feile hvis Journalposten har hoveddokument som er brukt som vedlegg i andre journalposter (gjenbrukt).
	 * I slike tilfeller må relasjonene hvor hoveddokument er vedlegg slettes før journalposten kan slettes.
	 * Denne testen skal verifisere at Journalposten kan slettes etter at vedlegg relasjonene til hoveddokumentet er slettet.
	 */
	@Test
	public void skalSletteJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedleggEtterSlettingAvRelasjonene() throws IOException {
		abacPermit();
		Journalpost journalpostMedDokumentSomVedlegg = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		Journalpost journalpostSomSkalSlettes = createJournalpostWithHoveddokument();
		createDokumentInfoVedleggRelasjon(journalpostSomSkalSlettes);

		DokumentInfo dokumentInfoHoveddokument = journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		createVedleggRelasjon(journalpostMedDokumentSomVedlegg, dokumentInfoHoveddokument);

		saveJournalpost(journalpostSomSkalSlettes);
		saveJournalpost(journalpostMedDokumentSomVedlegg);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostList.size(), is(3));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));

		//Slett hoveddokument
		ResponseEntity<String> responseEntity2 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(dokumentInfoHoveddokument.getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));

		//Utfør samme kall som første kallet i testen
		ResponseEntity<String> responseEntity3 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSomSkalSlettes.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity3.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(dokumentInfoHoveddokument);
		assertThatDokumentInfoIsDeleted(journalpostSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));

		assertThatJournalpostIsDeleted(journalpostSomSkalSlettes.getJournalpostId());

		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomVedlegg);
		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

	}


	/**
	 * Test med arkivEnhet=JOURNALPOST
	 */
	@Test
	public void skalFeileHvisJournalpostIkkeFinnes() throws IOException {
		abacPermit();

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(1L)
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

	}


	//Tester med arkivEnhet=DOKUMENT_INFO

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteDokumentInfoSomErBareTilknyttetEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes
				.getJournalpostId())
				.size(), is(2));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes
				.getJournalpostId())
				.size(), is(1));

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(2));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId()), AksjonsTypeCode.SLETT, journalpostMedDokumentSomSkalSlettes
				.getJournalpostId(), relasjonVedlegg.getDokumentInfo().getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));
		assertAksjonsLogg(getAksjonsLoggByDokumentInfoId(aksjonsLoggList, relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()), AksjonsTypeCode.SLETT, null, relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));

	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteHoveddokumentOgBytteVedleggRelasjonTilHovedHvorJournalpostHarFlereRelasjoner() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		createDokumentInfoVedleggRelasjon(journalpostMedDokumentSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes
				.getJournalpostId())
				.size(), is(2));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(dokumentInfoSomSkalSlettes.getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatDokumentInfoIsDeleted(dokumentInfoSomSkalSlettes);

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);

		Long dokumentInfoIdGjortOmTilHoveddokument = journalpostMedDokumentSomSkalSlettes.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator()
				.next()
				.getDokumentInfo()
				.getDokumentInfoId();
		Journalpost journalpostMedDokumentSomSkalSlettesAfter=joarkRepository.findById(journalpostMedDokumentSomSkalSlettes.getJournalpostId()).get();
		assertThat(journalpostMedDokumentSomSkalSlettesAfter.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(), is(dokumentInfoIdGjortOmTilHoveddokument));
		assertThat(journalpostMedDokumentSomSkalSlettesAfter.getJournalpostDokumentInfoRelasjoner().size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostMedDokumentSomSkalSlettes
				.getJournalpostId())
				.size(), is(1));

		assertThatJournalpostIsNotDeleted(journalpost1);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost1);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));


		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoSomSkalSlettes
				.getDokumentInfoId()), AksjonsTypeCode.SLETT, journalpostMedDokumentSomSkalSlettes
				.getJournalpostId(), dokumentInfoSomSkalSlettes.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));
		assertAksjonsLogg(getAksjonsLoggByDokumentInfoId(aksjonsLoggList, dokumentInfoSomSkalSlettes.getDokumentInfoId()), AksjonsTypeCode.SLETT, null, dokumentInfoSomSkalSlettes
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(dokumentInfoSomSkalSlettes.getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));

		assertAksjonsLogg(getAksjonsLoggByJournalpostIdAndDokumentInfoId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId(), dokumentInfoIdGjortOmTilHoveddokument), AksjonsTypeCode.SLETT, journalpostMedDokumentSomSkalSlettes
				.getJournalpostId(), dokumentInfoIdGjortOmTilHoveddokument, Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_TILKNYTTET_SOM)
						.fraVerdi(TilknyttetJournalpostSomCode.VEDLEGG.name())
						.tilVerdi(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name())
						.build()

		));

		TestTransaction.end();
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteDokumentInfoSomErEnesteDokumentPåJournalpostOgVedleggPåEnAnnenJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpostSomHarDokumentSomVedlegg = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = TestDataGenerator.createVedleggRelasjon(journalpostSomHarDokumentSomVedlegg, journalpostMedDokumentSomSkalSlettes
				.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpostSomHarDokumentSomVedlegg);
		saveJournalpost(journalpost2);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()).size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.size(), is(2));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatJournalpostIsDeleted(journalpostMedDokumentSomSkalSlettes.getJournalpostId());
		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.get(0)
				.getDokumentInfo()
				.getDokumentInfoId(), not(relasjonVedlegg.getDokumentInfo().getDokumentInfoId()));
		assertThatJournalpostIsNotDeleted(journalpostSomHarDokumentSomVedlegg);
		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));

		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostSomHarDokumentSomVedlegg.getJournalpostId()), AksjonsTypeCode.SLETT, journalpostSomHarDokumentSomVedlegg
				.getJournalpostId(), relasjonVedlegg.getDokumentInfo().getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));
		assertAksjonsLogg(getAksjonsLoggByJournalpostId(aksjonsLoggList, journalpostMedDokumentSomSkalSlettes.getJournalpostId()), AksjonsTypeCode.SLETT, journalpostMedDokumentSomSkalSlettes
				.getJournalpostId(), relasjonVedlegg.getDokumentInfo().getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(RELASJON_DOKUMENT_INFO_ID)
						.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build(),
				ArkivElementEndring.builder()
						.arkivElement(JOURNALPOST_JOURNALPOST_ID)
						.fraVerdi(journalpostMedDokumentSomSkalSlettes.getJournalpostId().toString())
						.tilVerdi(null)
						.build()
		));
		assertAksjonsLogg(getAksjonsLoggByDokumentInfoId(aksjonsLoggList, relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()), AksjonsTypeCode.SLETT, null, relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(DOKUMENT_INFO_DOKUMENT_INFO_ID)
						.fraVerdi(relasjonVedlegg.getDokumentInfo().getDokumentInfoId().toString())
						.tilVerdi(null)
						.build()

		));
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalSletteDokumentInfoSomErVedleggPåEnAnnenJournalpostNårDokumentInfoOgJournalpostErSkjermet() throws IOException {
		abacPermit();

		Journalpost journalpostSomHarDokumentSomVedlegg = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpostMedDokumentSomSkalSlettes = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoSomSkalSlettes = journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = TestDataGenerator.createVedleggRelasjon(journalpostSomHarDokumentSomVedlegg, dokumentInfoSomSkalSlettes);

		saveJournalpost(journalpostMedDokumentSomSkalSlettes);
		saveJournalpost(journalpostSomHarDokumentSomVedlegg);
		saveJournalpost(journalpost2);

		skjermingService.setJpDokInfoRelSkjerming(relasjonVedlegg.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJpDokInfoRelSkjerming(journalpostMedDokumentSomSkalSlettes.findHoveddokumentDokumentInfoRelasjon()
				.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		skjermingService.setJournalpostSkjerming(journalpostMedDokumentSomSkalSlettes.getJournalpostId(), SkjermingTypeCode.POL);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()).size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.size(), is(2));
		assertThatJournalpostIsNotDeleted(journalpostMedDokumentSomSkalSlettes);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatJournalpostIsDeleted(journalpostMedDokumentSomSkalSlettes.getJournalpostId());
		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpostSomHarDokumentSomVedlegg
				.getJournalpostId())
				.get(0)
				.getDokumentInfo()
				.getDokumentInfoId(), not(relasjonVedlegg.getDokumentInfo().getDokumentInfoId()));

		assertThatJournalpostIsNotDeleted(journalpostSomHarDokumentSomVedlegg);

		assertThatJournalpostIsNotDeleted(journalpost2);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost2);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(3));
	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalFeileVedSlettingAvDokumentInfoSomErSplittet() throws IOException {
		abacPermit();

		Journalpost origJournalpost = createJournalpostWithHoveddokument();
		Journalpost journalpost = createJournalpostWithSplittetHoveddokument(origJournalpost);
		saveJournalpost(origJournalpost);
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(2));
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = IteratorUtils.toList(dokumentinfoRepository.findAll().iterator());
		assertThat(dokumentInfoList.size(), is(2));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(origJournalpost.findHoveddokumentDokumentInfoRelasjon()
										.getDokumentInfo()
										.getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));

		reinitTransaction();
		assertThatDokumentInfoAndFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		assertThatJournalpostIsNotDeleted(origJournalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(origJournalpost);

		assertThatJournalpostIsNotDeleted(journalpost);
		assertThatJournalpostRelasjonerIsNotDeleted(journalpost);

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));

	}

	/**
	 * arkivEnhet=DOKUMENT_INFO
	 */
	@Test
	public void skalFeileHvisDokumentInfoIkkeFinnes() throws IOException {
		abacPermit();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.dokumentInfoId(1L)
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}
	//Tester med arkivEnhet=DOKUMENT_FIL

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalSletteFilOgFildetaljer() throws IOException {
		abacPermit();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoMedVariantSomSkalSlettes = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		saveJournalpost(journalpost);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(1));
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = IteratorUtils.toList(dokumentinfoRepository.findAll().iterator());
		assertThat(dokumentInfoList.size(), is(1));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
								.dokumentInfoId(dokumentInfoMedVariantSomSkalSlettes
										.getDokumentInfoId())
								.variant(VariantFormatCode.ARKIV)
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatDokumentInfoIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes);
		assertThatFildetaljerIsDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV));
		assertThatFildetaljerIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		assertAksjonsLogg(aksjonsLoggList.get(0), AksjonsTypeCode.SLETT, null, dokumentInfoMedVariantSomSkalSlettes
				.getDokumentInfoId(), Arrays.asList(
				ArkivElementEndring.builder()
						.arkivElement(FILDETALJER_VARIANTFORMAT)
						.fraVerdi(VariantFormatCode.ARKIV.name())
						.tilVerdi(null)
						.build()

		));
	}

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalSletteFilOgFildetaljerNårFildetaljerErSkjermet() throws IOException {
		abacPermit();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo dokumentInfoMedVariantSomSkalSlettes = journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo();
		saveJournalpost(journalpost);
		skjermingService.setVariantSkjermet(dokumentInfoMedVariantSomSkalSlettes.getDokumentInfoId(), VariantFormatCode.ARKIV, SkjermingTypeCode.POL);

		reinitTransaction();

		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(1));
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = IteratorUtils.toList(dokumentinfoRepository.findAll().iterator());
		assertThat(dokumentInfoList.size(), is(1));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
								.dokumentInfoId(dokumentInfoMedVariantSomSkalSlettes
										.getDokumentInfoId())
								.variant(VariantFormatCode.ARKIV)
								.build(),
						createHeadersWithAksjon()),
				String.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		reinitTransaction();

		assertThatDokumentInfoIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes);
		assertThatFildetaljerIsDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV));
		assertThatFildetaljerIsNotDeleted(dokumentInfoMedVariantSomSkalSlettes
				.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
	}

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalFeileHvisVariantSomSkalSlettesIkkeFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		saveJournalpost(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(1));
		assertThatJournalpostIsNotDeleted(journalpost);
		List<DokumentInfo> dokumentInfoList = IteratorUtils.toList(dokumentinfoRepository.findAll().iterator());
		assertThat(dokumentInfoList.size(), is(1));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
								.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
										.getDokumentInfo()
										.getDokumentInfoId())
								.variant(VariantFormatCode.SLADDET)
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThatDokumentInfoIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		assertThatFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV));
		assertThatFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));

		TestTransaction.end();
	}

}
