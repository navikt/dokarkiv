package no.nav.dokarkiv.journalpost.v1.itest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVedlegg;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggRequest;
import no.nav.dokarkiv.journalpost.v1.api.TilknyttVedleggResponse;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggIT {

	/*@Test
	public void shouldTilknytteVedleggTilJournalpost() throws IOException {
		abacPermit();

		Journalpost journalpostVedlegg = createJournalpost();

		Journalpost journalpostOrg = createOrgJournalpost();

		Long journalpostIdVedlegg = joarkRepository.save(journalpostOrg).getJournalpostId();

		Long journalpostIdOrg = joarkRepository.save(journalpostVedlegg).getJournalpostId();

		Long dokumentInfoId = journalpostOrg.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().getDokumentInfoId();


		List<DokumentVedlegg> dokumentVedleggList = new ArrayList<>();
		dokumentVedleggList.add(DokumentVedlegg.builder()
				.kildeJournalpostId(journalpostIdOrg)
				.dokumentInfoId(Long.toString(dokumentInfoId))
				.build());

		TilknyttVedleggRequest request = createTilknyttVedleggRequest(dokumentVedleggList);

		HttpEntity<TilknyttVedleggRequest> requestHttpEntity = new HttpEntity<>(request, oidcHeaders());
		ResponseEntity<TilknyttVedleggResponse> responseEntity = restTemplate.exchange(
				URL_JOURNALPOST + journalpostIdVedlegg, HttpMethod.PUT, requestHttpEntity, TilknyttVedleggResponse.class);

		assertThat(responseEntity.getStatusCode(), is(HttpStatus.OK));


	}

	private TilknyttVedleggRequest createTilknyttVedleggRequest(List<DokumentVedlegg> dokumentVedleggList){
		return TilknyttVedleggRequest.builder()
				.tilknyttetAvNavn("Testus testesen")
				.dokument(dokumentVedleggList)
				.build();
	}
	private Journalpost createJournalpost() {
		return JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.D)
				.tilleggsopplysninger(new HashMap<String, String>() {{
					put("nokkel1", "verdi1");
					put("nokkel2", "verdi2"); }})
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.endretKildeNavn("endretKildeNavn")
				.endretAvNavn("endretAvNavn")
				.build();
	}

	private Journalpost createOrgJournalpost() {
		return JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J)
				.tilleggsopplysninger(new HashMap<String, String>() {{
					put("nokkel1", "verdi1");
					put("nokkel2", "verdi2"); }})
				.opprettetAvNavn("opprettetAvNavn")
				.opprettetKildeNavn("opprettetKildeNavn")
				.endretKildeNavn("endretKildeNavn")
				.endretAvNavn("endretAvNavn")
				.build();
	}
*/
}
