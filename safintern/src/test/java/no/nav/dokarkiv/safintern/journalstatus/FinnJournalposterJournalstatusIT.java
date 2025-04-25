package no.nav.dokarkiv.safintern.journalstatus;

import com.blazebit.persistence.DefaultKeyset;
import com.blazebit.persistence.DefaultKeysetPage;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.KeysetPageSerializerDeserializer;
import no.nav.dokarkiv.safintern.views.PaginatedAnyViewForTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_UTC;
import static no.nav.dokarkiv.core.util.TestdataFactory.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.core.util.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.core.util.TestdataFactory.createGsak;
import static no.nav.dokarkiv.core.util.TestdataFactory.setSkjermingVedlegg;
import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static org.assertj.core.api.Assertions.assertThat;

public class FinnJournalposterJournalstatusIT extends AbstractSafinternTest {
	private static final String FINNJOURNALPOSTER_STATUS = "/rest/internal/safintern/finnjournalposterstatus";
	private static final KeysetPageSerializerDeserializer<Long> keysetPageSerializerDeserializer = new KeysetPageSerializerDeserializer.JournalpostIdKeysetPageSerializerDeserializer();

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterStatusRequest finnJournalposterStatusRequestTo = createRequest(JournalStatusCode.U);
		String responseTo = finnJournalposterStatusRest(finnJournalposterStatusRequestTo).getBody();

		assertThat(responseTo).isEqualToIgnoringWhitespace("{\"journalposter\":[],\"antallRader\":0,\"totaltAntallRader\":0,\"page\":1,\"totalPages\":0,\"nextPage\":\"\"}");
	}

	@Test
	public void shouldPaginateResultsCoherentlyAndIdempotently() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost utgaattJournalpost1 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost1.setJournalstatus(JournalStatusCode.U);
		setSkjermingVedlegg(utgaattJournalpost1);
		Journalpost utgaattJournalpost2 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost2.setJournalstatus(JournalStatusCode.U);
		utgaattJournalpost2.setKanalReferanseId("REFERANSE_ID_2");
		setSkjermingVedlegg(utgaattJournalpost2);
		journalpostTestRepository.persist(utgaattJournalpost1);
		journalpostTestRepository.persist(utgaattJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusRequest finnJournalposterAllStatusRequest = createRequest(JournalStatusCode.U, 200, null);
		ResponseEntity<String> responseAll = finnJournalposterStatusRest(finnJournalposterAllStatusRequest);

		assertThat(responseAll.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseAll.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost1, utgaattJournalpost2, classpathResourceToString("/journalstatus/journalpost-journalstatus-response-2-journalposter.json"), 2, 2, 1, 1, ""));

		FinnJournalposterStatusRequest finnJournalposterStatusPage1Request = createRequest(JournalStatusCode.U, 1, null);
		ResponseEntity<String> responsePage1 = finnJournalposterStatusRest(finnJournalposterStatusPage1Request);

		assertThat(responsePage1.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responsePage1.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost2, null, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json"), 1, 2, 1, 2, null));

		FinnJournalposterStatusRequest finnJournalposterStatusPage2Request = createRequest(JournalStatusCode.U, 1, generateNextPage(1, utgaattJournalpost2));
		ResponseEntity<String> responsePage2 = finnJournalposterStatusRest(finnJournalposterStatusPage2Request);

		assertThat(responsePage2.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responsePage2.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost1, null, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json"), 1, 2, 2, 2, ""));
	}

	@Test
	public void shouldPaginateResultsCorrectlyForVariousPagesizes() {

		IntStream.range(0, 400).mapToObj(i -> {
			var jp = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg();
			jp.setKanalReferanseId("kanalref" + i);
			jp.setJournalstatus(JournalStatusCode.U);
			return jp;
		}).forEach(journalpostTestRepository::persist);

		TestTransaction.flagForCommit();
		TestTransaction.end();

		for (int pageSize : new int[]{2, 10, 15, 100}) {
			String lastSeen = null;
			int countReturned = 0;
			PaginatedAnyViewForTest.MinimalViableJournalpostForTest previousJournalpost = null;
			int totalPages, currentPage;
			do {
				FinnJournalposterStatusRequest finnJournalposterAllStatusRequest = createRequest(JournalStatusCode.U, pageSize, lastSeen);
				ResponseEntity<PaginatedAnyViewForTest> response = finnJournalposterStatusRest(finnJournalposterAllStatusRequest, PaginatedAnyViewForTest.class);
				assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
				PaginatedAnyViewForTest body = response.getBody();

				if (countReturned > 0 && body.antallRader() > 0) {
					assertThat(body.journalposter().get(0).journalpostId()).isEqualTo(previousJournalpost.journalpostId() - 1);
				}

				if (body.antallRader() > 0) {
					previousJournalpost = body.journalposter().get(body.journalposter().size() - 1);
				}

				lastSeen = body.nextPage();
				countReturned = body.antallRader();
				totalPages = body.totalPages();
				currentPage = body.page();
			} while (countReturned > 0 && lastSeen != null && !lastSeen.isEmpty());
			assertThat(currentPage)
					.as("Sjekk at vi har iterert til siste side når vi får tomt resultat")
					.isEqualTo(totalPages);
		}
	}

	@Test
	public void shouldFailWhenJournalstatusNotUorUB() {
		var xyzzy = finnJournalposterStatusRest(createRequest(JournalStatusCode.J));
		assertThat(xyzzy.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void shouldFailWhenRequestedPageSizeTooLarge() {
		var xyzzy = finnJournalposterStatusRest(createRequest(JournalStatusCode.U, SafinternJournalStatusService.MAX_PAGE_SIZE + 1, null));
		assertThat(xyzzy.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}


	@Test
	public void shouldFindJournalpostWithJournalstatusU() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost utgaattJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost.setJournalstatus(JournalStatusCode.U);
		setSkjermingVedlegg(utgaattJournalpost);
		Journalpost ferdigstiltJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		ferdigstiltJournalpost.setKanalReferanseId("REFERANSE_ID_2");
		journalpostTestRepository.persist(utgaattJournalpost);
		journalpostTestRepository.persist(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusRequest finnJournalposterStatusRequestTo = createRequest(JournalStatusCode.U);
		ResponseEntity<String> response = finnJournalposterStatusRest(finnJournalposterStatusRequestTo);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json")));
	}

	@Test
	public void shouldFindJournalpostWithJournalstatusUB() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost ukjentbrukerJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		ukjentbrukerJournalpost.setJournalstatus(JournalStatusCode.UB);
		setSkjermingVedlegg(ukjentbrukerJournalpost);
		Journalpost ferdigstiltJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		ferdigstiltJournalpost.setKanalReferanseId("REFERANSE_ID_2");
		journalpostTestRepository.persist(ukjentbrukerJournalpost);
		journalpostTestRepository.persist(ferdigstiltJournalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusRequest finnJournalposterStatusRequestTo = createRequest(JournalStatusCode.UB);
		var response = finnJournalposterStatusRest(finnJournalposterStatusRequestTo);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(ukjentbrukerJournalpost, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json")));
	}

	@Test
	public void shouldReturnVedleggOrderedByRelasjonId() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost journalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		journalpost.setJournalstatus(JournalStatusCode.U);
		createDokumentInfoVedleggRelasjon(journalpost);
		setSkjermingVedlegg(journalpost);
		journalpostTestRepository.persist(journalpost);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusRequest finnJournalposterStatusRequestTo = createRequest(JournalStatusCode.U);
		ResponseEntity<String> response = finnJournalposterStatusRest(finnJournalposterStatusRequestTo);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(journalpost, classpathResourceToString("/journalstatus/journalpost-journalstatus-3-vedlegg-response.json")));
	}

	private FinnJournalposterStatusRequest createRequest(JournalStatusCode journalStatusCode) {
		return createRequest(journalStatusCode, null, null);
	}

	private FinnJournalposterStatusRequest createRequest(JournalStatusCode journalStatusCode, Integer foerste, String lastseen) {
		return new FinnJournalposterStatusRequest(
				journalStatusCode,
				"2019-01-01",
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				foerste, lastseen
		);
	}


	private ResponseEntity<String> finnJournalposterStatusRest(FinnJournalposterStatusRequest finnJournalposterStatusRequestTo) {
		return finnJournalposterStatusRest(finnJournalposterStatusRequestTo, String.class);
	}

	private <T> ResponseEntity<T> finnJournalposterStatusRest(FinnJournalposterStatusRequest finnJournalposterStatusRequestTo, Class<T> tClass) {
		HttpEntity<FinnJournalposterStatusRequest> requestEntity = new HttpEntity<>(finnJournalposterStatusRequestTo, createHeadersWithServiceUserTokenAndRolesClaim(ROLE_CLAIM_TILGANG));
		return restTemplate.exchange(FINNJOURNALPOSTER_STATUS, HttpMethod.POST, requestEntity, tClass);
	}

	private static String mapStringResponse(Journalpost originalJournalpost, String responseTemplate) {
		return mapStringResponse(originalJournalpost, null, responseTemplate, 1, 1, 1, 1, "");
	}

	private static String mapStringResponse(Journalpost foerste, Journalpost andre, String responseTemplate, int count, int totalcount, int currentPage, int totalPages, String nextPage) {
		DokumentInfo hoved = getDokumentInfo(foerste, JournalpostDokumentInfoRelasjon::isHoveddokument);
		DokumentInfo vedlegg = getDokumentInfo(foerste, JournalpostDokumentInfoRelasjon::isVedlegg);
		assertThat(hoved).isNotNull();
		assertThat(vedlegg).isNotNull();

		Optional<DokumentInfo> vedlegg2 = foerste.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isVedlegg)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.filter(dok -> !dok.getDokumentInfoId().equals(vedlegg.getDokumentInfoId()))
				.findFirst();
		String replace = responseTemplate
				.replace("nextpage_replace", nextPage != null ? nextPage : generateNextPage(currentPage, foerste))
				.replace("dokumentInfoId_vedleggb_replace", vedlegg2.map(DokumentInfo::getDokumentInfoId).map(String::valueOf).orElse(""))
				.replace("logiskVedleggId_vedleggb_replace", vedlegg2.map(x -> x.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString()).orElse(""))
				.replace("count_total_replace", "" + totalcount)
				.replace("count_replace", "" + count)
				.replace("current_page_replace", "" + currentPage)
				.replace("total_pages_replace", "" + totalPages)
				.replace("status_replace", foerste.getJournalstatus().toString())
				.replace("referanseId_replace", foerste.getKanalReferanseId())
				.replace("journalpostId_replace", foerste.getJournalpostId().toString())
				.replace("dokumentInfoId_hoveddokument_replace", hoved.getDokumentInfoId().toString())
				.replace("dokumentInfoId_vedlegg_replace", vedlegg.getDokumentInfoId().toString())
				.replace("sakId_replace", foerste.getSaksrelasjon() != null ? foerste.getSaksrelasjon().getSakId().toString() : "")
				.replace("logiskVedleggId_hoveddokument_replace", hoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
				.replace("logiskVedleggId_vedlegg_replace", vedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());

		if (andre != null) {
			return mapStringResponseSecondJournalpost(andre, replace);
		}
		return replace;
	}

	private static String generateNextPage(int currentpage, Journalpost... journalposter) {
		return keysetPageSerializerDeserializer.serializeKeysetPage(
				new DefaultKeysetPage(
						0, 0,
						new DefaultKeyset(new Serializable[]{journalposter[0].getJournalpostId()}),
						new DefaultKeyset(new Serializable[]{journalposter[journalposter.length - 1].getJournalpostId()})
				),
				0, currentpage
		);
	}

	private static String mapStringResponseSecondJournalpost(Journalpost andre, String responseTemplate) {
		DokumentInfo andreHoved = getDokumentInfo(andre, JournalpostDokumentInfoRelasjon::isHoveddokument);
		DokumentInfo andreVedlegg = getDokumentInfo(andre, JournalpostDokumentInfoRelasjon::isVedlegg);
		assertThat(andreHoved).isNotNull();
		assertThat(andreVedlegg).isNotNull();

		LocalDateTime createdDateGjenbrukt = andre.getChangeStamp().getCreatedDate();
		String gjenbruktNowIso = createdDateGjenbrukt.atZone(ZONEID_UTC).toOffsetDateTime().toString();
		return responseTemplate
				.replace("opprettet_b_replace", gjenbruktNowIso)
				.replace("referanseId_b_replace", andre.getKanalReferanseId())
				.replace("journalpostId_b_replace", andre.getJournalpostId().toString())
				.replace("dokumentInfoId_hoveddokument_b_replace", andreHoved.getDokumentInfoId().toString())
				.replace("dokumentInfoId_vedlegg_b_replace", andreVedlegg.getDokumentInfoId().toString())
				.replace("logiskVedleggId_hoveddokument_b_replace", andreHoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
				.replace("logiskVedleggId_vedlegg_b_replace", andreVedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());
	}

	private static DokumentInfo getDokumentInfo(Journalpost foerste, Predicate<JournalpostDokumentInfoRelasjon> isHoveddokument) {
		return foerste.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(isHoveddokument)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
	}

}
