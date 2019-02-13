package no.nav.dokarkiv.skjermarkivenhet.rjoark101;

import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import static no.nav.dokarkiv.skjermarkivenhet.util.TestUtils.opprettHoveddokumentForIT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.ArkivenhetCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.util.TestDataUtils;
import no.nav.dokarkiv.skjermarkivenhet.AbstractSkjermArkivenhetIT;
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

public class Rjoark101IT extends AbstractSkjermArkivenhetIT {

	@Test
	public void skalOppheveSkjermeJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		skjermingService.setJournalpostBegrensning(journalpost, SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(joarkRepository.findById(journalpost.getJournalpostId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.JOURNALPOST, journalpost.getJournalpostId(), null, null),
				createHeadersWithAksjon());


		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<Journalpost> jpEtterKall = joarkRepository.findById(journalpost.getJournalpostId());
		assertTrue(jpEtterKall.isPresent());
		assertNull(jpEtterKall.get().getSkjermingType());


		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ENDRE_SKJERMING));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), nullValue());
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		ArkivElementEndring arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer().iterator())
				.get(0);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer().size(), is(1));
		assertThat(arkivElementEndringList.getArkivElement(), is("Journalpost.skjermingType"));
		assertThat(arkivElementEndringList.getFraVerdi(), is("POL"));
		assertThat(arkivElementEndringList.getTilVerdi(), nullValue());
		assertThat(arkivElementEndringList.getAksjonsLogg(), is(aksjonsLogg));

		TestTransaction.end();
	}


	@Test
	public void skalOppheveSkjermeDokumentInfo() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingService.setJpDokInfoRelBegrensning(journalpost.findHoveddokumentDokumentInfoRelasjon(), SkjermingTypeCode.POL);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(journalpostDokumentInfoRelasjonRepository.findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(
				journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId())
				.get()
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.VEDLEGG, journalpost.getJournalpostId(),
						dokumentInfo.getDokumentInfoId(), null),
				createHeadersWithAksjon());

		TestTransaction.start();
		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		Optional<JournalpostDokumentInfoRelasjon> jpDokInfoEtterKall = journalpostDokumentInfoRelasjonRepository.
				findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(journalpost.getJournalpostId(), dokumentInfo.getDokumentInfoId());
		assertTrue(jpDokInfoEtterKall.isPresent());
		assertNull(jpDokInfoEtterKall.get().getSkjermingType());


		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ENDRE_SKJERMING));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), is(journalpost.getJournalpostId()));
		assertThat(aksjonsLogg.getDokumentInfoId(), is(dokumentInfo.getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		ArkivElementEndring arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer().iterator())
				.get(0);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer().size(), is(1));
		assertThat(arkivElementEndringList.getArkivElement(), is("JournalpostDokumentInfoRelasjon.skjermingType"));
		assertThat(arkivElementEndringList.getFraVerdi(), is("POL"));
		assertThat(arkivElementEndringList.getTilVerdi(), nullValue());
		assertThat(arkivElementEndringList.getAksjonsLogg(), is(aksjonsLogg));

		TestTransaction.end();
	}

	@Test
	public void skalOppheveSkjermeDokumentFil() throws IOException {
		abacPermit();

		Journalpost journalpost = joarkRepository.save(opprettHoveddokumentForIT());
		DokumentInfo dokumentInfo = journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo();
		skjermingService.setVariantSkjermet(dokumentInfo, VariantFormatCode.ARKIV, SkjermingTypeCode.POL);


		TestTransaction.flagForCommit();
		TestTransaction.end();

		TestTransaction.start();

		assertThat(dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId())
				.get()
				.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV)
				.getSkjermingType(), is(SkjermingTypeCode.POL));

		HttpEntity httpEntity = new HttpEntity(
				createSkjermarkivenhetRequest(SkjermingTypeCode.POL, ArkivenhetCode.DOKUMENT_FIL, null,
						dokumentInfo.getDokumentInfoId(), VariantFormatCode.ARKIV),
				createHeadersWithAksjon());


		ResponseEntity<String> responseEntity = restTemplate.exchange(
				URL_SKJERMARKIVENHET,
				HttpMethod.DELETE,
				httpEntity,
				String.class);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));

		TestTransaction.start();

		Optional<DokumentInfo> dokInfoEtterKall = dokumentinfoRepository.findByDokumentInfoId(dokumentInfo.getDokumentInfoId());
		assertTrue(dokInfoEtterKall.isPresent());
		assertNull(dokInfoEtterKall.get().findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getSkjermingType());

		List<AksjonsLogg> aksjonsLoggList = IteratorUtils.toList(aksjonsLoggRepository.findAll().iterator());
		assertThat(aksjonsLoggList.size(), is(1));

		AksjonsLogg aksjonsLogg = aksjonsLoggList.get(0);
		assertThat(aksjonsLogg.getAksjon(), is(AksjonsTypeCode.ENDRE_SKJERMING));
		assertThat(aksjonsLogg.getUtfoertAv(), is(TestDataUtils.AKSJON_UTFOERT_AV));
		assertThat(aksjonsLogg.getHjemmel(), is(TestDataUtils.AKSJON_HJEMMEL));
		assertThat(aksjonsLogg.getMelding(), is(TestDataUtils.AKSJON_MELDING));
		assertThat(aksjonsLogg.getJournalpostId(), nullValue());
		assertThat(aksjonsLogg.getDokumentInfoId(), is(dokumentInfo.getDokumentInfoId()));
		assertThat(aksjonsLogg.getApplikasjon(), is(SERVICE_USER_ID));
		assertThat(aksjonsLogg.getArkivElementEndringer().size(), is(1));

		ArkivElementEndring arkivElementEndringList = IteratorUtils.toList(aksjonsLogg.getArkivElementEndringer().iterator())
				.get(0);
		assertThat(aksjonsLoggList.get(0).getArkivElementEndringer().size(), is(1));
		assertThat(arkivElementEndringList.getArkivElement(), is("Fildetaljer.variantFormat[ARKIV].skjermingType"));
		assertThat(arkivElementEndringList.getFraVerdi(), is("POL"));
		assertThat(arkivElementEndringList.getTilVerdi(), nullValue());
		assertThat(arkivElementEndringList.getAksjonsLogg(), is(aksjonsLogg));

		TestTransaction.flagForCommit();
		TestTransaction.end();
	}
}
