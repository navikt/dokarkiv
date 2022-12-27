package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

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

import java.util.Arrays;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.fail;

public class Rjoark904IT extends AbstractHentjournalsakinfoItest {
	private static final String FINNJOURNALPOSTER_STATUS = "/hentjournalsakinfo/finnjournalposterstatus";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindOnlyOneJournalpostWhenMoreMatchingForPagination() {
		Journalpost utgaattJournalpost1 = createUniqueJournalpost();
		utgaattJournalpost1.setJournalstatus(JournalStatusCode.U);
		Journalpost utgaattJournalpost2 = createUniqueJournalpost();
		utgaattJournalpost2.setJournalstatus(JournalStatusCode.U);
		joarkRepository.save(utgaattJournalpost1);
		joarkRepository.save(utgaattJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
	}

	@Test
	public void shouldFailWhenJournalstatusNotUorUB() {
		try {
			finnJournalposterStatusRest(createRequest(JournalStatusCode.J));
			fail();
		} catch (HttpClientErrorException e) {
			assertThat(e.getStatusCode(), is(HttpStatus.BAD_REQUEST));
		}
	}

	@Test
	public void shouldFindJournalpostWithJournalstatusU() {
		Journalpost utgaattJournalpost = createUniqueJournalpost();
		utgaattJournalpost.setJournalstatus(JournalStatusCode.U);
		Journalpost ferdigstiltJournalpost = createUniqueJournalpost();
		joarkRepository.save(utgaattJournalpost);
		joarkRepository.save(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
	}

	@Test
	public void shouldFindJournalpostWithJournalstatusUB() {
		Journalpost ukjentbrukerJournalpost = createUniqueJournalpost();
		ukjentbrukerJournalpost.setJournalstatus(JournalStatusCode.UB);
		Journalpost ferdigstiltJournalpost = createUniqueJournalpost();
		joarkRepository.save(ukjentbrukerJournalpost);
		joarkRepository.save(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.UB));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg1);
		Journalpost journalpost = createUniqueJournalpost();
		journalpost.setJournalstatus(JournalStatusCode.U);
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		joarkRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		joarkRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
		JournalpostDto journalpostDto = responseTo.getTilgangJournalposter().get(0);
		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertThat(journalpostDto.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
	}

	private FinnJournalposterStatusRequestTo createRequest(JournalStatusCode journalStatusCode) {
		FinnJournalposterStatusRequestTo requestTo = new FinnJournalposterStatusRequestTo();
		requestTo.setFraDato("2019-01-01");
		requestTo.setJournalstatus(journalStatusCode);
		requestTo.setJournalposttyper(Arrays.asList(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N));
		requestTo.setFoerste(1);
		return requestTo;
	}

	private FinnJournalposterStatusResponseTo finnJournalposterStatusRest(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		HttpEntity<FinnJournalposterStatusRequestTo> requestEntity = new HttpEntity<>(finnJournalposterStatusRequestTo, createDefaultHeaders());
		ResponseEntity<Object> exchange = restTemplate.exchange(FINNJOURNALPOSTER_STATUS, HttpMethod.POST, requestEntity, Object.class);
		if (exchange.getStatusCode() == HttpStatus.OK) {
			return objectMapper.convertValue(exchange.getBody(), FinnJournalposterStatusResponseTo.class);
		} else {
			throw new HttpClientErrorException(exchange.getStatusCode());
		}
	}
}
