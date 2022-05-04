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

import java.util.Arrays;
import java.util.Collections;

import static no.nav.dokarkiv.core.util.TestDataGenerator.PSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.SAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createJournalpostWithHoveddokument;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createPsakSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class Rjoark900IT extends AbstractHentjournalsakinfoItest {
	private static final String FINNJOURNALPOSTER_STATUS = "/hentjournalsakinfo/finnjournalposter";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(createRequest(JournalStatusCode.U));
		assertThat(responseTo.getTilgangJournalposter(), hasSize(0));
	}

	@Test
	public void shouldFindAllJournalpostWithJournalstatusFS() {
		Journalpost ferdigstiltJournalpost1 = createJournalpostWithHoveddokument();
		Journalpost ferdigstiltJournalpost2 = createJournalpostWithHoveddokument();
		joarkRepository.save(ferdigstiltJournalpost1);
		joarkRepository.save(ferdigstiltJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequestTo request = createRequest(JournalStatusCode.FS);
		request.setFoerste(2);
		FinnJournalposterResponseTo responseTo = finnJournalposterRest(request);

		assertThat(responseTo.getTilgangJournalposter(), hasSize(2));
	}

	@Test
	public void shouldFindAllJournalpostForGsakAndPsak() {
		Journalpost gsakJournalpost = createJournalpostWithHoveddokument();
		Journalpost psakJournalpost = createJournalpostWithHoveddokument();
		psakJournalpost.setSaksrelasjon(createPsakSaksrelasjon());
		joarkRepository.save(gsakJournalpost);
		joarkRepository.save(psakJournalpost);
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
		dokumentInfoRepository.save(vedlegg2);
		DokumentInfo vedlegg1 = createDokumentInfo();
		dokumentInfoRepository.save(vedlegg1);
		Journalpost journalpost = createJournalpostWithHoveddokument();
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		createVedleggRelasjon(journalpost, vedlegg1);
		joarkRepository.save(journalpost);
		createVedleggRelasjon(journalpost, vedlegg2);
		joarkRepository.save(journalpost);
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
	}

	@Test
	public void shouldReturnNewDokumenInfoValues() {
		DokumentInfo vedlegg = createDokumentInfoWithMoreData();
		dokumentInfoRepository.save(vedlegg);
		Journalpost journalpost = createJournalpostWithHoveddokument();
		createVedleggRelasjon(journalpost, vedlegg);
		joarkRepository.save(journalpost);
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
