package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.hentjournalsakinfo.AbstractHentjournalsakinfoItest;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.util.TestDataGenerator.PSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.SAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createPsakSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class Rjoark900IT extends AbstractHentjournalsakinfoItest {
	private static final String FINNJOURNALPOSTER_STATUS = "/hentjournalsakinfo/finnjournalposter";
	private static final Date LESTDATO = Date.from(LocalDate.now().minusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant());


	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(createRequest(JournalStatusCode.U));
		assertThat(responseTo.getTilgangJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindAllJournalpostWithJournalstatusFS() {
		Journalpost ferdigstiltJournalpost1 = createUniqueJournalpost();
		Journalpost ferdigstiltJournalpost2 = createUniqueJournalpost();
		journalpostRepository.save(ferdigstiltJournalpost1);
		journalpostRepository.save(ferdigstiltJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(2));
	}

	@Test
	public void shouldFindAllJournalpostForGsakAndPsak() {
		Journalpost gsakJournalpost = createUniqueJournalpost();
		Journalpost psakJournalpost = createUniqueJournalpost();
		psakJournalpost.setSaksrelasjon(createPsakSaksrelasjon());
		journalpostRepository.save(gsakJournalpost);
		journalpostRepository.save(psakJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		request.setPsakSakIds(Collections.singletonList(PSAK_ID));

		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(2));
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg1);
		Journalpost journalpost = createUniqueJournalpost();
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		journalpostRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(1);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(1));
		JournalpostDto journalpostDto = responseTo.getTilgangJournalposter().get(0);
		assertThat(journalpostDto.getDokumenter(), hasSize(3));
		assertThat(journalpostDto.getDokumenter().get(0).getDokumentInfoId(), is(hoveddokument.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(1).getDokumentInfoId(), is(vedlegg1.getDokumentInfoId()));
		assertThat(journalpostDto.getDokumenter().get(2).getDokumentInfoId(), is(vedlegg2.getDokumentInfoId()));
		assertThat(journalpostDto.getInnsyn(), is(BRUK_STANDARDREGLER));
		assertEquals(LESTDATO, journalpostDto.getLestDato());
	}

	@Test
	public void shouldReturnNewDokumenInfoValues() {
		DokumentInfo vedlegg = createDokumentInfoWithMoreData();
		Journalpost journalpost = createUniqueJournalpost();
		createVedleggRelasjon(journalpost, vedlegg);
		journalpostRepository.save(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(1);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		DokumentInfoDto dokumentInfoDto = responseTo.getTilgangJournalposter().get(0).getDokumenter().get(1);

		assertThat(dokumentInfoDto.getKategori(), is(DokumentKategoriCode.B));
		assertThat(dokumentInfoDto.getOrganInternt(), is(true));
		assertThat(dokumentInfoDto.getInnskrPartsinnsyn(), is(true));
		assertThat(dokumentInfoDto.getInnskrTredjepart(), is(true));
	}

	private FinnJournalposterRequestTo createRequest(JournalStatusCode journalStatusCode) {
		FinnJournalposterRequestTo requestTo = new FinnJournalposterRequestTo();
		requestTo.setFraDato("2019-01-01");
		requestTo.setGsakSakIds(Collections.singletonList(SAK_ID));
		requestTo.setInkluderJournalStatus(Collections.singletonList(journalStatusCode));
		requestTo.setInkluderJournalpostType(Arrays.asList(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N));
		requestTo.setFoerste(1);
		return requestTo;
	}

	private FinnJournalposterResponseTo finnJournalposterRest(FinnJournalposterRequestTo finnJournalposterRequestTo) {
		HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(finnJournalposterRequestTo, createDefaultHeaders());
		ResponseEntity<Object> exchange = restTemplate.exchange(FINNJOURNALPOSTER_STATUS, HttpMethod.POST, requestEntity, Object.class);
		if (exchange.getStatusCode() == HttpStatus.OK) {
			return objectMapper.convertValue(exchange.getBody(), FinnJournalposterResponseTo.class);
		} else {
			throw new HttpClientErrorException(exchange.getStatusCode());
		}
	}
}
