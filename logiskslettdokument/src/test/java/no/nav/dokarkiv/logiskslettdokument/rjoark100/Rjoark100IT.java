package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
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

public class Rjoark100IT extends AbstractSlettDokumentIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeaders()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeaders()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalLogiskSletteDokument_avVedlegg() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<JournalpostDokumentInfoRelasjon> rel = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());

		assertThat(rel.isPresent(), is(true));
		assertThat(rel.get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avVedlegg_ettersomVedleggErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s og dokumentInfoId=%s. Dokumentet er skjermet.",
						journalpost.getJournalpostId(),
						rel.getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avVedlegg_ettersomHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + rel.getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er skjermet",
						journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalLogiskSletteDokument_avHoveddokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jp = joarkRepository.findById (journalpost.getJournalpostId());
		assertThat(jp.isPresent(), is(true));
		assertThat(jp.get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_avHoveddokument_ettersomHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke utføre logisk sletting av dokument med journalpostId=%s. Journalposten er skjermet",
						journalpost.getJournalpostId())));
	}

	@Test
	public void skalLogiskSletteDokument_avHoveddokument_evenNaarVedleggErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		JournalpostDokumentInfoRelasjon rel = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next();

		skjermingService.setJpDokInfoRelBegrensning(rel, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jp = joarkRepository.findById (journalpost.getJournalpostId());
		assertThat(jp.isPresent(), is(true));
		assertThat(jp.get().getSkjermingType(), is(SkjermingTypeCode.POL));


		Optional<JournalpostDokumentInfoRelasjon> relRepositury = journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), rel.getDokumentInfo().getDokumentInfoId());
		assertThat(relRepositury.isPresent(), is(true));
		assertThat(relRepositury.get().getSkjermingType(), is(SkjermingTypeCode.POL));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomJournalpostIdIkkeFinnes() throws IOException {
		abacPermit();

		Long ikkeEksisterendeJournalpostId = 13L;
		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + ikkeEksisterendeJournalpostId + "/" + ikkeEksisterendeDokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Journalpost ikke funnet. journalpostId=%s",
						ikkeEksisterendeJournalpostId)));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomJournalpostDokumentInfoRelasjonIkkeFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + ikkeEksisterendeDokumentInfoId,
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost.getJournalpostId(),
						ikkeEksisterendeDokumentInfoId)));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomIngenRelasjonMellomInputJournalpostIdOgInputDokumentInfoIdFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost1 = joarkRepository.save(opprettHoveddokumentForIT());
		Journalpost journalpost2 = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost1.getJournalpostId() + "/" + journalpost2
						.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
						journalpost1.getJournalpostId(),
						journalpost2.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId())));
	}

	@Test
	public void skalIkkeLogiskSletteDokument_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		DokumentInfo sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + sammensattDok.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke logisk slette dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						sammensattDok.getDokumentInfoId())));
	}

	@Test
	public void noAccess() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccesHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

}
