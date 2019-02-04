package no.nav.dokarkiv.fysiskslettdokument.rjoark102;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.core.domain.codes.AksjonTypeCode.SLETT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.knyttDokumentInfoSomVedleggTilJournalpostForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettDuplikatRelasjon;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.fysiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.fysiskslettdokument.AbstractFysiskSlettDokumentIT;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class Rjoark102IT extends AbstractFysiskSlettDokumentIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertFalse(rel.getDokumentInfo().isRelatedToMultipleJournalposts());

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).isEmpty());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size() > 0);


		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(SLETT));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertFalse(rel.getDokumentInfo().isRelatedToMultipleJournalposts());

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).isEmpty());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size() > 0);


		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				createHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + 1 + "/" + 1,
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeGiTilgangHvisKalletIkkeBlirGjortAvJoarkadmin() throws IOException {
		abacPermit();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + 1 + "/" + 1,
				HttpMethod.DELETE,
				createNoAccesHeaders(),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonMangler() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		Long feilDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ feilDokumentInfoId,
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						feilDokumentInfoId)));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomIngenRelasjonMellomInputJournalpostIdOgInputDokumentInfoIdFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomJournalpostDokumentInfoRelasjonIkkeErUnik() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		opprettDuplikatRelasjon(journalpost.findHoveddokumentDokumentInfoRelasjon());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
		assertThat(responseEntity.getBody(), containsString("query did not return a unique result"));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke fysisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						rel.getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						SkjermingTypeCode.POL)));
	}

	@Test
	public void skalSletteDokumentFysisk_avVedlegg_somErKnyttetEnJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertFalse(rel.getDokumentInfo().isRelatedToMultipleJournalposts());
		Optional<JournalpostDokumentInfoRelasjon> relRep1 = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());

		assertTrue(relRep1.isPresent());
		assertThat(relRep1.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).isEmpty());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size() > 0);


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);


		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<JournalpostDokumentInfoRelasjon> relRep2 = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());

		assertFalse(relRep2.isPresent());

		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost.getJournalpostId()).isEmpty());
		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).isEmpty());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size() > 0);
	}

	@Test
	public void skalSletteDokumentFysisk_avVedlegg_somErKnyttetToJournalposter_skalKunSletteRelasjon() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		JournalpostDokumentInfoRelasjon rel = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(rel.getDokumentInfo(), journalpost2);

		joarkRepository.save(journalpost2);

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertTrue(rel.getDokumentInfo().isRelatedToMultipleJournalposts());

		Optional<JournalpostDokumentInfoRelasjon> relRep1 = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost1.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());
		assertTrue(relRep1.isPresent());
		assertThat(relRep1.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).size());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());


		assertEquals(2L, joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<JournalpostDokumentInfoRelasjon> relRep = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost1.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());

		assertFalse(relRep.isPresent());

		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost1.getJournalpostId()).isEmpty());
		assertEquals(1L, journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(rel.getDokumentInfo().getDokumentInfoId()).size());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId()).isPresent());
		assertEquals(1L, joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				rel.getDokumentInfo().getDokumentInfoId()).size());
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						SkjermingTypeCode.POL)));
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokument_utenAndreRelasjoner() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Optional<Journalpost> jpRep = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpRep.isPresent());
		assertThat(jpRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		assertFalse(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isEmpty());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				hoveddokument.getDokumentInfoId()).size() > 0);

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpRep1 = joarkRepository.findById(journalpost.getJournalpostId());
		assertFalse(jpRep1.isPresent());

		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByJournalpostJournalpostId(
				journalpost.getJournalpostId()).isEmpty());
		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(
				hoveddokument.getDokumentInfoId()).size() > 0);
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerHoveddokumentErBegrenset_skalSletteAlt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		Optional<Journalpost> jpRep = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpRep.isPresent());
		assertThat(jpRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).size());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isEmpty());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.isEmpty());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpRep1 = joarkRepository.findById(journalpost.getJournalpostId());
		assertFalse(jpRep1.isPresent());

		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost.getJournalpostId()).isEmpty());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument.getDokumentInfoId())
				.isEmpty());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
				.isEmpty());

		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), hoveddokument.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument.getDokumentInfoId()).isEmpty());
	}

	@Test
	public void skalIkkeSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerVedleggErBegrenset_skalKasteBegrensningIkkeFunnetException() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo hoveddokument = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Optional<JournalpostDokumentInfoRelasjon> relRep = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(rel.getJournalpost().getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());
		assertTrue(relRep.isPresent());
		assertThat(relRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost.getJournalpostId() + "/"
						+ hoveddokument.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						SkjermingTypeCode.POL)));
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentOgEtKnyttetVedleggDerHoveddokumentErBegrensetMenVedleggErKnyttetAnnenJournalpost_skalIkkeSletteAndreJournalpostRelasjonen() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());
		Journalpost journalpost2 = opprettHoveddokumentForIT();

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo hoveddokument2 = journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo vedlegg = journalpost1.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(vedlegg, journalpost2);

		joarkRepository.save(journalpost2);

		skjermingService.setJournalpostBegrensning(journalpost1, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		// Assert riktig datastruktur før sletting
		assertTrue(vedlegg.isRelatedToMultipleJournalposts());

		Optional<Journalpost> jpRep = joarkRepository.findById(journalpost1.getJournalpostId());
		assertTrue(jpRep.isPresent());
		assertThat(jpRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).size());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).size());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isEmpty());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isEmpty());
		assertEquals(2L,
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
						.size());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());

		// Assert originalJournalpost er journalpost1
		assertThat(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).get().getOriginalJournalpost().getJournalpostId(),
				is(journalpost1.getJournalpostId()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ hoveddokument1.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		// Assert sletting av begrensning
		Optional<Journalpost> jpRep1 = joarkRepository.findById(journalpost1.getJournalpostId());
		assertFalse(jpRep1.isPresent());

		// Assert sletting av relasjoner for journalpost1 og hoveddokument1 og at vedlegg kun har en relasjon
		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).isEmpty());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).size());
		assertTrue(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isEmpty());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isEmpty());
		assertEquals(1L,
				journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(vedlegg.getDokumentInfoId())
						.size());

		// Assert sletting av hoveddokument1, men hoveddokument2 og vedlegg finnes kvar
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).isPresent());

		//Assert sletting av journalpost1
		assertTrue(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());

		// Assert originalJournalpost er skiftet til journalpost2
		assertThat(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), vedlegg.getDokumentInfoId()).get().getOriginalJournalpost().getJournalpostId(),
				is(journalpost2.getJournalpostId()));
	}

	@Test
	public void skalSletteDokumentFysisk_medHoveddokumentDerHoveddokumentErBegrensetMenKnyttetTilAnnenJournalpostSomVedlegg_skalIkkeSletteAndreJournalpostRelasjonen() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT());

		DokumentInfo hoveddokument1 = journalpost1.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		DokumentInfo hoveddokument2 = journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();

		knyttDokumentInfoSomVedleggTilJournalpostForIT(hoveddokument1, journalpost2);
		JournalpostDokumentInfoRelasjon jp1RelasjonSomVedleggTilJp2 = hoveddokument1.findJournalpostRelasjonByJournalpostId(journalpost2
				.getJournalpostId());
		journalpostDokumentInfoRelasjonRepository.save(jp1RelasjonSomVedleggTilJp2);

		skjermingService.setJournalpostBegrensning(journalpost1, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		// Assert riktig datastruktur før sletting
		assertTrue(hoveddokument1.isRelatedToMultipleJournalposts());
		Optional<Journalpost> jpRep = joarkRepository.findById(journalpost1.getJournalpostId());
		assertTrue(jpRep.isPresent());
		assertThat(jpRep.get().getSkjermingType(), is(SkjermingTypeCode.POL));

		assertEquals(1L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).size());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).size());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isEmpty());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isEmpty());

		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());

		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());

		// Assert originalJournalpost er journalpost1
		assertThat(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument1.getDokumentInfoId())
						.get()
						.getOriginalJournalpost()
						.getJournalpostId(),
				is(journalpost1.getJournalpostId()));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_FYSISKSLETTDOKUMENT + journalpost1.getJournalpostId() + "/"
						+ hoveddokument1.getDokumentInfoId(),
				HttpMethod.DELETE,
				new HttpEntity<>(createHeadersWithAksjon(SLETT.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		// Assert sletting av begrensning
		Optional<Journalpost> jpRep1 = joarkRepository.findById(journalpost1.getJournalpostId());
		assertFalse(jpRep1.isPresent());

		// Assert sletting av relasjoner for journalpost1, og at hoveddokument1 ikke er slettet
		assertTrue(journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost1.getJournalpostId()).isEmpty());
		assertEquals(2L, journalpostDokumentInfoRelasjonRepository
				.findAllByJournalpostJournalpostId(journalpost2.getJournalpostId()).size());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument1.getDokumentInfoId())
				.isEmpty());
		assertFalse(journalpostDokumentInfoRelasjonRepository.findAllByDokumentInfoDokumentInfoId(hoveddokument2.getDokumentInfoId())
				.isEmpty());

		// Assert sletting av hoveddokument1 sett fra journalpost1 men ikke fra journalpost2
		assertFalse(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost1.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument1.getDokumentInfoId()).isPresent());
		assertTrue(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument2.getDokumentInfoId()).isPresent());

		// Assert at begge hoveddokumenter ikke er slettede, men at journalpost1 er slettet
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument1.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findAllJournalpostIdsByDokumentInfoId(hoveddokument2.getDokumentInfoId()).isEmpty());
		assertFalse(joarkRepository.findById(journalpost1.getJournalpostId()).isPresent());
		assertTrue(joarkRepository.findById(journalpost2.getJournalpostId()).isPresent());

		// Assert originalJournalpost er skiftet til journalpost2
		assertThat(dokumentinfoRepository.findAllByJournalpostRelasjonerJournalpostJournalpostIdAndDokumentInfoId(
				journalpost2.getJournalpostId(), hoveddokument1.getDokumentInfoId())
						.get()
						.getOriginalJournalpost()
						.getJournalpostId(),
				is(journalpost2.getJournalpostId()));

	}

}
