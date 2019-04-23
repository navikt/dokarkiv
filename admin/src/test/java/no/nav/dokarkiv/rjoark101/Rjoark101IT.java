package no.nav.dokarkiv.rjoark101;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithSplittetHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.AbstractAdminIT;
import no.nav.dokarkiv.core.consumer.RestConsumerExceptionResponse;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.util.TestDataGenerator;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.dto.SlettArkivenhetRequest;
import no.nav.dokarkiv.dto.SlettArkivenhetResponse;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test caser som er dekket
 * <p>
 * JOURNALPOST
 * skalSletteJournalpostMedHoveddokumentOgEnVedleggMedIngenRelasjoner
 * skalSletteJournalpostMedHoveddokumentOgRelasjonerTilAndreDokumentInfoerSomVedlegg
 * skalFeileVedSlettingAvJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedlegg
 * skalSletteJournalpostMedHoveddokumentSomHarRelasjonTilAndreJournalposterSomVedleggEtterSlettingAvRelasjonene
 * skalIkkeSletteJournalposterMedDokumenterSomErSplittet
 * <p>
 * VEDLEGG
 * skalSletteDokumentInfoVedleggSomErBareTilknyttetEnJournalpost
 * skalSletteRelasjonTilVedleggForDokumentInfoSomErHoveddokumentPåEnAnnenJournalpost
 * skalFeileVedSlettingAvHoveddokument
 * <p>
 * DOKUMENT_FIL
 * skalSletteFilOgFildetaljer
 * skalFeileHvisVariantSomSkalSlettesIkkeFinnes
 */
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
		journalpost.addJournalpostDokumentInfoRelasjon(TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpost));
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);

		ResponseEntity<SlettArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);

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
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), nullValue());
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(4));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer().size(), is(4));
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
						.arkivElement("JournalpostDokumentInfoRelasjon.dokumentInfoId")
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("Journalpost.journalpostId")
						.fraVerdi(journalpost.getJournalpostId().toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil()
		));

		TestTransaction.end();


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

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));


		ResponseEntity<SlettArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
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
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), nullValue());
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(3));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
						.arkivElement("JournalpostDokumentInfoRelasjon.dokumentInfoId")
						.fraVerdi(dokumentInfoVedlegg.getDokumentInfoId().toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("Journalpost.journalpostId")
						.fraVerdi(journalpost.getJournalpostId().toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(journalpost.findHoveddokumentDokumentInfoRelasjon()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil()
		));
		TestTransaction.end();

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

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));

		HttpEntity httpEntity = new HttpEntity(
				SlettArkivenhetRequest.builder()
						.arkivenhet(ArkivenhetCode.JOURNALPOST)
						.journalpostId(journalpost1.getJournalpostId())
						.build(),
				createHeadersWithAksjon());


		ResponseEntity<RestConsumerExceptionResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				RestConsumerExceptionResponse.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost1);
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

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
		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();
		Journalpost journalpost = createJournalpostWithHoveddokument();

		DokumentInfo dokumentInfoVedlegg = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		journalpost.addJournalpostDokumentInfoRelasjon(createVedleggRelasjon(journalpost, dokumentInfoVedlegg));

		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(2));

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
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

		//Slett vedleggrelasjoner til hoveddokument
		ResponseEntity<SlettArkivenhetResponse> responseEntity2 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.journalpostId(journalpost.getJournalpostId())
								.dokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));

		//Utfør samme kall som første kallet i testen
		ResponseEntity<SlettArkivenhetResponse> responseEntity3 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpost1.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity3.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThatJournalpostIsDeleted(journalpost1.getJournalpostId());
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
		}

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());
		assertThat(journalpostListAfter.size(), is(2));

		assertThatJournalpostIsNotDeleted(journalpost);
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getDokumentInfoId().equals(dokumentInfoVedlegg.getDokumentInfoId())) {
				assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
			} else {
				assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
			}
		}

		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId())
				.size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(dokumentInfoVedlegg.getDokumentInfoId())
				.size(), is(0));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(journalpost2.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));

		TestTransaction.end();
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

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
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

		ResponseEntity<SlettArkivenhetResponse> responseEntity1 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSplit1.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity1.getStatusCode(), is(HttpStatus.OK));

		ResponseEntity<SlettArkivenhetResponse> responseEntity2 = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.JOURNALPOST)
								.journalpostId(journalpostSplit2.getJournalpostId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity2.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostListAfter.size(), is(1));
		assertThatJournalpostIsNotDeleted(journalpostOriginal);
		assertThatJournalpostIsDeleted(journalpostSplit1.getJournalpostId());
		assertThatJournalpostIsDeleted(journalpostSplit2.getJournalpostId());

		TestTransaction.end();
	}


	//Tester med arkivEnhet=VEDLEGG

	/**
	 * arkivEnhet=VEDLEGG
	 */
	@Test
	public void skalSletteDokumentInfoVedleggSomErBareTilknyttetEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = TestDataGenerator.createDokumentInfoVedleggRelasjon(journalpost);
		journalpost.addJournalpostDokumentInfoRelasjon(relasjonVedlegg);

		saveJournalpost(journalpost);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<SlettArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.journalpostId(journalpost.getJournalpostId())
								.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostListAfter.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (rel.getDokumentInfo().getDokumentInfoId().equals(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())) {
				assertThatDokumentInfoIsDeleted(rel.getDokumentInfo());
			} else {
				assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
			}
		}
		assertThatJournalpostIsNotDeleted(journalpost1);
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThatDokumentInfoIsDeleted(relasjonVedlegg.getDokumentInfo());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(relasjonVedlegg.getDokumentInfo().getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(2));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
						.arkivElement("JournalpostDokumentInfoRelasjon.dokumentInfoId")
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil(),
				ArkivElementEndring.builder()
						.arkivElement("DokumentInfo.dokumentInfoId")
						.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.iterator()
								.next()
								.getDokumentInfo()
								.getDokumentInfoId()
								.toString())
						.tilVerdi(null)
						.build().toStringElementFraTil()
		));
		TestTransaction.end();
	}

	/**
	 * arkivEnhet=VEDLEGG
	 */
	@Test
	public void skalSletteRelasjonTilVedleggForDokumentInfoSomErHoveddokumentPåEnAnnenJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost1 = createJournalpostWithHoveddokument();
		Journalpost journalpost2 = createJournalpostWithHoveddokument();

		Journalpost journalpost = createJournalpostWithHoveddokument();
		JournalpostDokumentInfoRelasjon relasjonVedlegg = TestDataGenerator.createVedleggRelasjon(journalpost, journalpost1.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo());
		journalpost.addJournalpostDokumentInfoRelasjon(relasjonVedlegg);
		saveJournalpost(journalpost1);
		saveJournalpost(journalpost2);
		saveJournalpost(journalpost);


		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();
		List<Journalpost> journalpostList = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostList.size(), is(3));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()).size(), is(2));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(relasjonVedlegg.getJournalpost()
				.getJournalpostId()).size(), is(2));
		assertThatJournalpostIsNotDeleted(journalpost);

		//Sjekk at tjenesten feiler ved sletting av journalpost med hoveddokument som er brukt som vedlegg i andre journalposter
		ResponseEntity<SlettArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.journalpostId(journalpost.getJournalpostId())
								.dokumentInfoId(relasjonVedlegg.getDokumentInfo().getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		List<Journalpost> journalpostListAfter = IteratorUtils.toList(joarkRepository.findAll().iterator());

		assertThat(journalpostListAfter.size(), is(3));
		assertThatJournalpostIsNotDeleted(journalpost);
		for (JournalpostDokumentInfoRelasjon rel : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost1);
		for (JournalpostDokumentInfoRelasjon rel : journalpost1.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}
		assertThatJournalpostIsNotDeleted(journalpost2);
		for (JournalpostDokumentInfoRelasjon rel : journalpost2.getJournalpostDokumentInfoRelasjoner()) {
			assertThatDokumentInfoAndFildetaljerIsNotDeleted(rel.getDokumentInfo());
		}

		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(relasjonVedlegg.getDokumentInfo()
				.getDokumentInfoId()).size(), is(1));
		assertThat(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(relasjonVedlegg.getJournalpost()
				.getJournalpostId()).size(), is(1));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(relasjonVedlegg.getDokumentInfo().getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
				.arkivElement("JournalpostDokumentInfoRelasjon.dokumentInfoId")
				.fraVerdi(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.iterator()
						.next()
						.getDokumentInfo()
						.getDokumentInfoId()
						.toString())
				.tilVerdi(null)
				.build().toStringElementFraTil()
		));
		TestTransaction.end();
	}

	/**
	 * arkivEnhet=VEDLEGG
	 */
	@Test
	public void skalFeileVedSlettingAvHoveddokument() throws IOException {
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
								.arkivenhet(ArkivenhetCode.DOKUMENT_INFO)
								.journalpostId(journalpost.getJournalpostId())
								.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
										.getDokumentInfo()
										.getDokumentInfoId())
								.build(),
						createHeadersWithAksjon()),
				RestConsumerExceptionResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_ACCEPTABLE));

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThatDokumentInfoAndFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());

		TestTransaction.end();
	}
	//Tester med arkivEnhet=DOKUMENT_FIL

	/**
	 * arkivEnhet=DOKUMENT_FIL
	 */
	@Test
	public void skalSletteFilOgFildetaljer() throws IOException {
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
		ResponseEntity<SlettArkivenhetResponse> responseEntity = restTemplate.exchange(
				URL_SLETTARKIVENHET,
				HttpMethod.DELETE,
				new HttpEntity(
						SlettArkivenhetRequest.builder()
								.arkivenhet(ArkivenhetCode.DOKUMENT_FIL)
								.dokumentInfoId(journalpost.findHoveddokumentDokumentInfoRelasjon()
										.getDokumentInfo()
										.getDokumentInfoId())
								.variant(VariantFormatCode.ARKIV)
								.build(),
						createHeadersWithAksjon()),
				SlettArkivenhetResponse.class);
		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.flagForCommit();
		TestTransaction.end();
		TestTransaction.start();

		assertThatDokumentInfoIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo());
		assertThatFildetaljerIsDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV));
		assertThatFildetaljerIsNotDeleted(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON));
		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.SLETT));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo()
				.getDokumentInfoId()));
		assertThat(aksjonsLogg.getJournalpostId(), nullValue());
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		List<ArkivElementEndring> arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer()
				.iterator());
		assertThat(arkivElementEndringList.stream()
				.map(ArkivElementEndring::toStringElementFraTil)
				.collect(Collectors.toList()), hasItems(ArkivElementEndring.builder()
				.arkivElement("FilDetaljer.variantFormat")
				.fraVerdi("ARKIV")
				.tilVerdi(null)
				.build().toStringElementFraTil()
		));

		TestTransaction.end();
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
