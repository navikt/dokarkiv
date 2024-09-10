package no.nav.dokarkiv.safintern.finnjournalposter;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.views.PaginatedAnyViewForTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.Long.parseLong;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.util.TestDataGenerator.API_GSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.API_PSAK_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.BRUKER_ID;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfo;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createDokumentInfoWithMoreData;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createPsakSaksrelasjon;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createVedleggRelasjon;
import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.FIXED_CLOCK;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static org.assertj.core.api.Assertions.assertThat;

public class FinnJournalposterIT extends AbstractSafinternTest {
	private static final String FINNJOURNALPOSTER = "/rest/internal/safintern/finnjournalposter";


	@Test
	public void shouldReturnEmptyResponseWhenNotFound_Feilregistrert() {
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		journalpost.getSaksrelasjon().setFeilregistrert(true);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		PaginatedAnyViewForTest responseTo = finnJournalposterRest(createRequest(FS, 1));
		assertThat(responseTo.journalposter()).hasSize(0);

		FinnJournalposterRequest secondRequest = new FinnJournalposterRequest(
				List.of(parseLong(API_GSAK_ID)),
				null,
				"2019-01-02",
				null,
				true,
				null,
				List.of(FS),
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				1,
				null
		);
		PaginatedAnyViewForTest secondResponse = finnJournalposterRest(secondRequest);
		assertThat(secondResponse.journalposter()).hasSize(1);

		FinnJournalposterRequest tecondRequest = new FinnJournalposterRequest(
				List.of(parseLong(API_GSAK_ID)),
				null,
				"2019-01-03",
				null,
				false,
				null,
				List.of(FS),
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				1,
				null
		);
		PaginatedAnyViewForTest tecondResponse = finnJournalposterRest(tecondRequest);
		assertThat(tecondResponse.journalposter()).hasSize(0);
	}

	@Test
	public void shouldReturnEmptyResponseWhenNotFound_FilteredTilDato() {
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		PaginatedAnyViewForTest responseTo = finnJournalposterRest(createRequest(FS, 1));
		assertThat(responseTo.journalposter()).hasSize(1);

		FinnJournalposterRequest secondRequest = new FinnJournalposterRequest(
				List.of(parseLong(API_GSAK_ID)),
				List.of(new Long[]{}),
				"2019-01-01",
				"2022-01-01",
				null,
				null,
				List.of(FS),
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				1,
				null
		);
		PaginatedAnyViewForTest secondResponse = finnJournalposterRest(secondRequest);
		assertThat(secondResponse.journalposter()).hasSize(0);
	}

	@Test
	public void shouldReturnEmptyResponseWhenNotFound_Journalposttype() {
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		PaginatedAnyViewForTest responseTo = finnJournalposterRest(createRequest(FS, 1));
		assertThat(responseTo.journalposter()).hasSize(1);

		FinnJournalposterRequest secondRequest = new FinnJournalposterRequest(
				List.of(parseLong(API_GSAK_ID)),
				List.of(new Long[]{}),
				"2019-01-01",
				"2022-01-01",
				null,
				null,
				List.of(FS),
				List.of(JournalpostTypeCode.I),
				1,
				null
		);
		PaginatedAnyViewForTest secondResponse = finnJournalposterRest(secondRequest);
		assertThat(secondResponse.journalposter()).hasSize(0);
	}

	@Test
	public void shouldFindAllJournalpostWithJournalstatusFS() {
		Journalpost ferdigstiltJournalpost1 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		Journalpost ferdigstiltJournalpost2 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		ferdigstiltJournalpost2.setKanalReferanseId("REFERANSE_ID_2");
		journalpostTestRepository.persist(ferdigstiltJournalpost1);
		journalpostTestRepository.persist(ferdigstiltJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequest request = createRequest(FS, 2);
		PaginatedAnyViewForTest responseTo = finnJournalposterRest(request);

		assertThat(responseTo.journalposter()).hasSize(2);
	}

	@Test
	public void shouldFindAllJournalpostForGsakAndPsak() {
		Saksrelasjon psakSaksrelasjon = createPsakSaksrelasjon();
		Journalpost gsakJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		Journalpost psakJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(psakSaksrelasjon);
		psakJournalpost.setKanalReferanseId("REFERANSE_ID_2");
		journalpostTestRepository.persist(gsakJournalpost);
		journalpostTestRepository.persist(psakJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		FinnJournalposterRequest request = createRequest(FS, 2, parseLong(API_PSAK_ID));

		PaginatedAnyViewForTest responseTo = finnJournalposterRest(request);

		assertThat(responseTo.journalposter()).hasSize(2);
	}

	@Test
	public void shouldFindJournalpostForHugeGsakList() {
		Saksrelasjon psakSaksrelasjon = createPsakSaksrelasjon();
		Journalpost gsakJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		Journalpost psakJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(psakSaksrelasjon);
		psakJournalpost.setKanalReferanseId("REFERANSE_ID_2");
		journalpostTestRepository.persist(gsakJournalpost);
		journalpostTestRepository.persist(psakJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();
		FinnJournalposterRequest request = createRequest(FS, 10,
				Stream.concat(Stream.of(parseLong(API_GSAK_ID)), LongStream.range(0, 2000).boxed()).toList(), parseLong(API_PSAK_ID));

		PaginatedAnyViewForTest responseTo = finnJournalposterRest(request);

		assertThat(responseTo.journalposter()).hasSize(2);
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		populateInnsyn();
		DokumentInfo vedlegg2 = createDokumentInfo();
		dokumentInfoRepository.persist(vedlegg2);
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		DokumentInfo hoveddokument = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(0);
		journalpostTestRepository.persist(journalpost);
		DokumentInfo vedlegg1 = journalpost.getDokumentInfoFromJpDokInfoRelasjoner(1);
		createVedleggRelasjon(journalpost, vedlegg2);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequest request = createRequest(FS, 1);
		PaginatedAnyViewForTest responseTo = finnJournalposterRest(request);

		assertThat(responseTo.journalposter()).hasSize(1);
		var responseJournalpost = responseTo.journalposter().get(0);
		assertThat(responseJournalpost.dokumenter()).hasSize(3);
		List<PaginatedAnyViewForTest.MinimalViableDokumentinfoForTest> dokumenter = List.copyOf(responseJournalpost.dokumenter());
		assertThat(dokumenter.get(0).dokumentInfoId()).isEqualTo(hoveddokument.getDokumentInfoId());
		assertThat(dokumenter.get(1).dokumentInfoId()).isEqualTo(vedlegg1.getDokumentInfoId());
		assertThat(dokumenter.get(2).dokumentInfoId()).isEqualTo(vedlegg2.getDokumentInfoId());
		assertThat(responseJournalpost.innsyn()).isEqualTo(BRUK_STANDARDREGLER);
		assertThat(responseJournalpost.innsynsbeskrivelse()).isEqualToIgnoringCase("beskrivelse av " + BRUK_STANDARDREGLER);
		String expectedLestDato = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(Instant.now(FIXED_CLOCK).atZone(ZoneOffset.ofHours(0))) + "+00:00";
		assertThat(responseJournalpost.relevanteDatoer().lest()).isEqualTo(expectedLestDato);
	}

	@Test
	public void shouldReturnNewDokumenInfoValues() {
		DokumentInfo vedlegg = createDokumentInfoWithMoreData();
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(parseLong(API_GSAK_ID));
		createVedleggRelasjon(journalpost, vedlegg);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterRequest request = createRequest(FS, 1);
		PaginatedAnyViewForTest responseTo = finnJournalposterRest(request);

		PaginatedAnyViewForTest.MinimalViableDokumentinfoForTest dokumentInfo = responseTo.journalposter().get(0).dokumenter().get(2);

		assertThat(dokumentInfo.kategori()).isEqualTo(DokumentKategoriCode.B);
		assertThat(dokumentInfo.sensitivt()).isTrue();
	}

	@Test
	public void shouldReturnJournalpostsWithNullSaksrelasjon() {
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg((Saksrelasjon) null);
		// journalpost.setSaksrelasjon(null);
		journalpost.setJournalstatus(M);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();


		FinnJournalposterRequest safselvbetjeningLikeRequest = new FinnJournalposterRequest(
				null, //List.of(parseLong(API_GSAK_ID)),
				null,
				"2016-04-01",
				null,
				false,
				List.of(BRUKER_ID),
				List.of(MO, M, J, E, FL, FS),
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				99,
				null
		);

		PaginatedAnyViewForTest responseTo = finnJournalposterRest(safselvbetjeningLikeRequest);
		assertThat(responseTo.journalposter()).hasSize(1);
	}

	private FinnJournalposterRequest createRequest(JournalStatusCode journalStatusCode, int antallRader, Long... psakSakIds) {
		return createRequest(journalStatusCode, antallRader, List.of(parseLong(API_GSAK_ID)), psakSakIds);
	}

	private static FinnJournalposterRequest createRequest(JournalStatusCode journalStatusCode, int antallRader, List<Long> gsakSakids, Long... psakIds) {
		return new FinnJournalposterRequest(
				gsakSakids,
				List.of(psakIds),
				"2019-01-01",
				null,
				null,
				null,
				List.of(journalStatusCode),
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				antallRader,
				null
		);
	}

	private PaginatedAnyViewForTest finnJournalposterRest(FinnJournalposterRequest finnJournalposterRequestTo) {
		HttpEntity<FinnJournalposterRequest> requestEntity = new HttpEntity<>(finnJournalposterRequestTo, createHeadersWithServiceUserTokenAndRolesClaim(ROLE_CLAIM_TILGANG));
		ResponseEntity<Object> exchange = restTemplate.exchange(FINNJOURNALPOSTER, HttpMethod.POST, requestEntity, Object.class);
		if (exchange.getStatusCode() == HttpStatus.OK) {
			return objectMapper.convertValue(exchange.getBody(), PaginatedAnyViewForTest.class);
		} else {
			throw new HttpClientErrorException(exchange.getStatusCode());
		}
	}
}
