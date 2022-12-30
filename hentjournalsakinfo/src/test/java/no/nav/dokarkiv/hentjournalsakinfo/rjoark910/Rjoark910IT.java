package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static no.nav.dokarkiv.core.util.TestDataGenerator.AKTOER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.PSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createGsak;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createPsakSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class Rjoark910IT extends AbstractHentjournalsakinfoItest {
	private static final String DOKUMENTOVERSIKTBRUKER_ENDPOINT = "/hentjournalsakinfo/dokumentoversiktbruker";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		DokumentoversiktBrukerResponseTo responseTo = dokumentoversiktBrukerRest(createRequest(JournalStatusCode.U));
		assertThat(responseTo.getJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindAllJournalpostWithJournalstatusFS() {
		Journalpost ferdigstiltJournalpost1 = createUniqueJournalpost();
		ferdigstiltJournalpost1.getSaksrelasjon().setSakId("1");
		Journalpost ferdigstiltJournalpost2 = createUniqueJournalpost();
		ferdigstiltJournalpost2.getSaksrelasjon().setSakId("2");
		journalpostRepository.save(ferdigstiltJournalpost1);
		journalpostRepository.save(ferdigstiltJournalpost2);
		sakTestRepository.persist(createGsak());
		sakTestRepository.persist(createGsak());
		TestTransaction.flagForCommit();
		TestTransaction.end();

		DokumentoversiktBrukerRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		DokumentoversiktBrukerResponseTo responseTo = dokumentoversiktBrukerRest(request);

		assertThat(responseTo.getJournalposter(), hasSize(2));
	}

	@Test
	public void shouldFindAllJournalpostForGsakAndPsak() {
		Journalpost gsakJournalpost = createUniqueJournalpost();
		Journalpost psakJournalpost = createUniqueJournalpost();
		psakJournalpost.setSaksrelasjon(createPsakSaksrelasjon());
		gsakJournalpost.getSaksrelasjon().setSakId("1");
		sakTestRepository.persist(createGsak());
		journalpostRepository.save(gsakJournalpost);
		journalpostRepository.save(psakJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		DokumentoversiktBrukerRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		request.setPsakSakIds(Collections.singletonList(PSAK_ID));

		DokumentoversiktBrukerResponseTo responseTo = dokumentoversiktBrukerRest(request);

		assertThat(responseTo.getJournalposter(), hasSize(2));
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg1);
		Journalpost journalpost = createUniqueJournalpost();
		journalpost.getSaksrelasjon().setSakId("1");
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		journalpostRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostRepository.save(journalpost);
		sakTestRepository.persist(createGsak());
		TestTransaction.flagForCommit();
		TestTransaction.end();

		DokumentoversiktBrukerRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(1);
		DokumentoversiktBrukerResponseTo responseTo = dokumentoversiktBrukerRest(request);

		assertThat(responseTo.getJournalposter(), hasSize(1));
		JournalpostDto journalpostDto = responseTo.getJournalposter().get(0);
		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertThat(journalpostDto.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
	}

	private DokumentoversiktBrukerRequestTo createRequest(JournalStatusCode journalStatusCode) {
		DokumentoversiktBrukerRequestTo requestTo = new DokumentoversiktBrukerRequestTo();
		requestTo.setAktoerId(AKTOER_ID);
		requestTo.setFraDato(LocalDate.ofYearDay(2019, 1));
		requestTo.setInkluderJournalStatus(Collections.singletonList(journalStatusCode));
		requestTo.setInkluderJournalpostType(Arrays.asList(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N));
		requestTo.setFoerste(1);
		return requestTo;
	}

	private DokumentoversiktBrukerResponseTo dokumentoversiktBrukerRest(DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo) {
		HttpEntity<DokumentoversiktBrukerRequestTo> requestEntity = new HttpEntity<>(dokumentoversiktBrukerRequestTo, createDefaultHeaders());
		ResponseEntity<Object> exchange = restTemplate.exchange(DOKUMENTOVERSIKTBRUKER_ENDPOINT, HttpMethod.POST, requestEntity, Object.class);
		if (exchange.getStatusCode() == HttpStatus.OK) {
			return objectMapper.convertValue(exchange.getBody(), DokumentoversiktBrukerResponseTo.class);
		} else {
			throw new HttpClientErrorException(exchange.getStatusCode());
		}
	}
}
