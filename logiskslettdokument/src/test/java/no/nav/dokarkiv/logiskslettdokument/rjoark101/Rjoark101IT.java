package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import static no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService.AKSJONS_LOGG_HEADER;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedEtKnyttetVedleggForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.opprettHoveddokumentMedSammensattDokForIT;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.utilgjengeliggjoerHoveddokument;
import static no.nav.dokarkiv.logiskslettdokument.util.TestUtils.utilgjengeliggjoerVedlegg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentIT;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentResponse;
import org.apache.commons.collections15.IteratorUtils;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.IOException;
import java.util.List;

public class Rjoark101IT extends AbstractSlettDokumentIT {

	@Test
	public void skalLagreAksjon() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));
		assertThat(aksjonsLoggList.get(0).getAksjon(), is(AksjonTypeCode.ENDRE_BEGRENSNING));
	}

	@Test
	public void skalFeileNårAksjonsLoggHeaderIkkeErSatt() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeaders()),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(String.format("Missing request header '%s'", AKSJONS_LOGG_HEADER)));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeLagreAksjonsLoggVedFeil() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		List<AksjonsLogg> aksjonsLoggListBefore = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggListBefore.size(), is(0));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(0));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_ettersomJournalpostDokumentInfoRelasjonIkkeFinnes() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		Long ikkeEksisterendeDokumentInfoId = 13L;

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + ikkeEksisterendeDokumentInfoId,
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
	public void skalAngreLogiskSlettDokument_avVedlegg_medVedleggUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_avVedlegg_ettersomVedleggIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for dokument med journalpostId=%s, dokumentInfoId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						vedlegg.getDokumentInfoId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));

		Begrensning begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunVedleggBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));
		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + vedlegg.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avHoveddokument_medHoveddokumentUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalIkkeAngreLogiskSlettDokument_medHoveddokument_ettersomHoveddokumentIkkeErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.NOT_FOUND));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Fant ikke forventet begrensning for journalpost med journalpostId=%s og begrensningsType=%s.",
						journalpost.getJournalpostId(),
						BegrensningTypeCode.UTILGJENGELIGGJORT)));

		Begrensning begrensetJp = hentJournalpostEtterUtfoertKall(journalpost.getJournalpostId());
		assertNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(0L));
	}

	@Test
	public void skalAngreLogiskSlettDokument_avKunHoveddokumentBegrensningen_naarVedleggOgHoveddokumentErUtilgjengeliggjort() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedEtKnyttetVedleggForIT());

		DokumentInfo vedlegg = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));
		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		Begrensning begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNotNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(2L));

		ResponseEntity<LogiskSlettDokumentResponse> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" +
						journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				LogiskSlettDokumentResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		begrensetJp = hentHoveddokumentBegrensningEtterUtfoertKall(journalpost);
		assertNull(begrensetJp);
		begrensetJp = hentVedleggBegrensningEtterUtfoertKall(journalpost.getJournalpostId(), vedlegg.getDokumentInfoId());
		assertNotNull(begrensetJp);
		assertThat(hentAntallBegrensninger(), is(1L));
	}

	@Test
	public void skalIkkeAngreLogiskSletteDokument_ettersomDokumentetErTilknyttetJournalpostSomSammensattDokument() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentMedSammensattDokForIT());

		DokumentInfo sammensattDok = journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.SAMMENSATT_DOK)
				.iterator().next().getDokumentInfo();

		begrensningRepository.save(utilgjengeliggjoerVedlegg(journalpost.getJournalpostId(), sammensattDok.getDokumentInfoId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + sammensattDok.getDokumentInfoId(),
				HttpMethod.POST,
				new HttpEntity<>(createHeadersWithAksjon(AksjonTypeCode.ENDRE_BEGRENSNING.name())),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		assertThat(responseEntity.getBody(), containsString(
				String.format("Kan ikke angre logisk sletting av dokument med journalpostId=%s, dokumentInfoId=%s fordi " +
								"dokumentet er ikke tilknyttet journalposten som hoveddokument eller vedlegg.",
						journalpost.getJournalpostId(),
						sammensattDok.getDokumentInfoId())));
	}

	@Test
	public void noAccess() {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		begrensningRepository.save(utilgjengeliggjoerHoveddokument(journalpost.getJournalpostId()));

		TestTransaction.flagForCommit();
		TestTransaction.end();

		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_ANGRESLETTDOKUMENT + journalpost.getJournalpostId() + "/" + journalpost.
						findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().getDokumentInfoId(),
				HttpMethod.POST,
				createNoAccesHeaders(),
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
	}

}
