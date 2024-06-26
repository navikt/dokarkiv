package no.nav.dokarkiv.safintern.journalpost;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.safintern.AbstractSafinternTest;
import no.nav.dokarkiv.safintern.journalstatus.FinnJournalposterStatusRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static no.nav.dokarkiv.safintern.SafinternConstants.ROLE_CLAIM_TILGANG;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createDokumentInfoVedleggRelasjon;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.createGsak;
import static no.nav.dokarkiv.safintern.journalpost.TestdataFactory.formattedDate;
import static org.assertj.core.api.Assertions.assertThat;

public class FinnJournalpostJournalstatusIT extends AbstractSafinternTest {
	private static final String FINNJOURNALPOSTER_STATUS = "/rest/internal/safintern/finnjournalposterstatus";

	@Test
	public void shouldReturnEmptyResponseWhenNotFound() {
		FinnJournalposterStatusRequest finnJournalposterStatusRequestTo = createRequest(JournalStatusCode.U);
		String responseTo = finnJournalposterStatusRest(finnJournalposterStatusRequestTo).getBody();

		assertThat(responseTo).isEqualToIgnoringWhitespace("[]");
	}

	@Test
	public void shouldPaginateResultsCoherentlyAndIdempotently() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost utgaattJournalpost1 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost1.setJournalstatus(JournalStatusCode.U);
		Journalpost utgaattJournalpost2 = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost2.setJournalstatus(JournalStatusCode.U);
		utgaattJournalpost2.setKanalReferanseId("carnalreferanseid");
		journalpostTestRepository.persist(utgaattJournalpost1);
		journalpostTestRepository.persist(utgaattJournalpost2);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		FinnJournalposterStatusRequest finnJournalposterAllStatusRequest = createRequest(JournalStatusCode.U, 200, null);
		ResponseEntity<String> responseAll = finnJournalposterStatusRest(finnJournalposterAllStatusRequest);

		assertThat(responseAll.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responseAll.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost1, utgaattJournalpost2, classpathResourceToString("/journalstatus/journalpost-journalstatus-response-2-journalposter.json")));

		FinnJournalposterStatusRequest finnJournalposterStatusPage1Request = createRequest(JournalStatusCode.U, 1, null);
		ResponseEntity<String> responsePage1 = finnJournalposterStatusRest(finnJournalposterStatusPage1Request);

		assertThat(responsePage1.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responsePage1.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost2, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json")));

		FinnJournalposterStatusRequest finnJournalposterStatusPage2Request = createRequest(JournalStatusCode.U, 1, utgaattJournalpost2.getJournalpostId());
		ResponseEntity<String> responsePage2 = finnJournalposterStatusRest(finnJournalposterStatusPage2Request);

		assertThat(responsePage2.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responsePage2.getBody()).isEqualToIgnoringWhitespace(mapStringResponse(utgaattJournalpost1, classpathResourceToString("/journalstatus/journalpost-journalstatus-response.json")));

		FinnJournalposterStatusRequest finnJournalposterStatusPage3Request = createRequest(JournalStatusCode.U, 1, utgaattJournalpost1.getJournalpostId());
		ResponseEntity<String> responsePage3 = finnJournalposterStatusRest(finnJournalposterStatusPage3Request);

		assertThat(responsePage3.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(responsePage3.getBody()).isEqualToIgnoringWhitespace("[]");
	}

	@Test
	public void shouldFailWhenJournalstatusNotUorUB() {
		var xyzzy = finnJournalposterStatusRest(createRequest(JournalStatusCode.J));
		assertThat(xyzzy.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	public void shouldFindJournalpostWithJournalstatusU() {
		Sak persistedSak = sakTestRepository.persist(createGsak());
		Long sakId = persistedSak.getSakId();
		Journalpost utgaattJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		utgaattJournalpost.setJournalstatus(JournalStatusCode.U);
		Journalpost ferdigstiltJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		ferdigstiltJournalpost.setKanalReferanseId("carnalreferanseid");
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
		Journalpost ferdigstiltJournalpost = createFullyPopulatedJournalpostWithHoveddokumentAndVedlegg(sakId);
		ferdigstiltJournalpost.setKanalReferanseId("carnalreferanseid");
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

	private FinnJournalposterStatusRequest createRequest(JournalStatusCode journalStatusCode, Integer foerste, Long lastseen) {
		return new FinnJournalposterStatusRequest(
				journalStatusCode,
				"2019-01-01",
				List.of(JournalpostTypeCode.I, JournalpostTypeCode.U, JournalpostTypeCode.N),
				foerste, lastseen
		);
	}

	private ResponseEntity<String> finnJournalposterStatusRest(FinnJournalposterStatusRequest finnJournalposterStatusRequestTo) {
		HttpEntity<FinnJournalposterStatusRequest> requestEntity = new HttpEntity<>(finnJournalposterStatusRequestTo, createHeadersWithServiceUserTokenAndRolesClaim(ROLE_CLAIM_TILGANG));
		return restTemplate.exchange(FINNJOURNALPOSTER_STATUS, HttpMethod.POST, requestEntity, String.class);
	}

	private static String mapStringResponse(Journalpost originalJournalpost, String responseTemplate) {
		return mapStringResponse(originalJournalpost, null, responseTemplate);
	}

	private static String mapStringResponse(Journalpost foerste, Journalpost andre, String responseTemplate) {
		Date createdDate = foerste.getChangeStamp().getCreatedDate();
		String nowIso = formattedDate().toFormatter().format(createdDate.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";

		DokumentInfo hoved = getDokumentInfo(foerste, JournalpostDokumentInfoRelasjon::isHoveddokument);
		DokumentInfo vedlegg = getDokumentInfo(foerste, JournalpostDokumentInfoRelasjon::isVedlegg);
		assertThat(hoved).isNotNull();
		assertThat(vedlegg).isNotNull();

		if (andre != null) {
			DokumentInfo andreHoved = getDokumentInfo(andre, JournalpostDokumentInfoRelasjon::isHoveddokument);
			DokumentInfo andreVedlegg = getDokumentInfo(andre, JournalpostDokumentInfoRelasjon::isVedlegg);
			assertThat(andreHoved).isNotNull();
			assertThat(andreVedlegg).isNotNull();

			Date createdDateGjenbrukt = andre.getChangeStamp().getCreatedDate();
			String gjenbruktNowIso = formattedDate().toFormatter().format(createdDateGjenbrukt.toInstant().atZone(ZoneId.of("UTC"))) + "+00:00";
			return responseTemplate
					.replace("opprettet_replace", nowIso)
					.replace("opprettet_b_replace", gjenbruktNowIso)
					.replace("status_replace", foerste.getJournalstatus().toString())
					.replace("referanseId_replace", foerste.getKanalReferanseId())
					.replace("referanseId_b_replace", andre.getKanalReferanseId())
					.replace("journalpostId_replace", foerste.getJournalpostId().toString())
					.replace("journalpostId_b_replace", andre.getJournalpostId().toString())
					.replace("dokumentInfoId_hoveddokument_replace", hoved.getDokumentInfoId().toString())
					.replace("dokumentInfoId_hoveddokument_b_replace", andreHoved.getDokumentInfoId().toString())
					.replace("dokumentInfoId_vedlegg_replace", vedlegg.getDokumentInfoId().toString())
					.replace("dokumentInfoId_vedlegg_b_replace", andreVedlegg.getDokumentInfoId().toString())
					.replace("sakId_replace", foerste.getSaksrelasjon().getSakId().toString())
					.replace("logiskVedleggId_hoveddokument_replace", hoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
					.replace("logiskVedleggId_vedlegg_replace", vedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
					.replace("logiskVedleggId_hoveddokument_b_replace", andreHoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
					.replace("logiskVedleggId_vedlegg_b_replace", andreVedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());
		}
		Optional<DokumentInfo> vedlegg2 = foerste.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(JournalpostDokumentInfoRelasjon::isVedlegg)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo)
				.filter(dok -> !dok.getDokumentInfoId().equals(vedlegg.getDokumentInfoId()))
				.findFirst();
		return responseTemplate
				.replace("dokumentInfoId_vedleggb_replace", vedlegg2.map(DokumentInfo::getDokumentInfoId).map(String::valueOf).orElse(""))
				.replace("logiskVedleggId_vedleggb_replace", vedlegg2.map(x -> x.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString()).orElse(""))
				.replace("opprettet_replace", nowIso)
				.replace("status_replace", foerste.getJournalstatus().toString())
				.replace("referanseId_replace", foerste.getKanalReferanseId())
				.replace("journalpostId_replace", foerste.getJournalpostId().toString())
				.replace("dokumentInfoId_hoveddokument_replace", hoved.getDokumentInfoId().toString())
				.replace("dokumentInfoId_vedlegg_replace", vedlegg.getDokumentInfoId().toString())
				.replace("sakId_replace", foerste.getSaksrelasjon() != null ? foerste.getSaksrelasjon().getSakId().toString() : "")
				.replace("logiskVedleggId_hoveddokument_replace", hoved.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString())
				.replace("logiskVedleggId_vedlegg_replace", vedlegg.getSkannetInnholdListe().iterator().next().getSkannetInnholdId().toString());
	}

	private static @NotNull DokumentInfo getDokumentInfo(Journalpost foerste, Predicate<JournalpostDokumentInfoRelasjon> isHoveddokument) {
		return foerste.getJournalpostDokumentInfoRelasjonerAdmin().stream()
				.filter(isHoveddokument)
				.map(JournalpostDokumentInfoRelasjon::getDokumentInfo).findFirst().get();
	}
}
