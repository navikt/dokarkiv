package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Arrays;

public class Rjoark904IT extends AbstractHentjournalsakinfoItest {
	private static final String FINNJOURNALPOSTER_STATUS = "/hentjournalsakinfo/finnjournalposterstatus";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindOnlyOneJournalpostWhenMoreMatchingForPagination() {
		Journalpost utgaattJournalpost1 = createJournalpostWithHoveddokument();
		utgaattJournalpost1.setJournalstatus(JournalStatusCode.U);
		Journalpost utgaattJournalpost2 = createJournalpostWithHoveddokument();
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
		Journalpost utgaattJournalpost = createJournalpostWithHoveddokument();
		utgaattJournalpost.setJournalstatus(JournalStatusCode.U);
		Journalpost ferdigstiltJournalpost = createJournalpostWithHoveddokument();
		joarkRepository.save(utgaattJournalpost);
		joarkRepository.save(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.U));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
	}

	@Test
	public void shouldFindJournalpostWithJournalstatusUB() {
		Journalpost ukjentbrukerJournalpost = createJournalpostWithHoveddokument();
		ukjentbrukerJournalpost.setJournalstatus(JournalStatusCode.UB);
		Journalpost ferdigstiltJournalpost = createJournalpostWithHoveddokument();
		joarkRepository.save(ukjentbrukerJournalpost);
		joarkRepository.save(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusResponseTo responseTo = finnJournalposterStatusRest(createRequest(JournalStatusCode.UB));

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
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
