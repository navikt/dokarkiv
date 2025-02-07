package no.nav.dokarkiv.journalpost.v1.api.sak;

import no.nav.dokarkiv.core.domain.codes.AvleveringStatusCode;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.itest.AbstractJournalpostIT;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Date;

import static java.time.Instant.now;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AAPEN;
import static no.nav.dokarkiv.core.domain.codes.SakStatusCode.AVSLUTTET;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForAktoerId;
import static no.nav.dokarkiv.core.util.TestDataGenerator.createSakForOrgNr;
import static no.nav.dokarkiv.core.util.TestdataFactory.GSAK_ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;

public class GjenaapneSakIT extends AbstractJournalpostIT {

	private static final String URL_GJENAAPNE_SAK = "/rest/journalpostapi/v1/sak/gjenaapneSak";
	private static final String FAGSAK_ID = "0123A21";
	private static final String FAGSAK_SYSTEM = "IT01";
	private static final String TEMA = "SYK";
	private static final String AKTOER_ID = "1234567890123";
	private static final String AKTOER_ID_HISTORISK = "1234567890124";
	private static final String KALLENDE_APP = "dokarkiv-itest";

	@Test
	public void happyPathGjenaapneSakBruker() {
		setupStubs();
		long sakId = persistDefaultAvsluttetSak();

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertGjenaapnetSak(updatedSak);
	}

	@Test
	public void happyPathGjenaapneSakOrganisasjon() {
		setupStubs();
		long sakId = persistDefaultAvsluttetSakForOrganisasjon();

		var requestEntity = new HttpEntity<>(createOrganisasjonGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		Sak updatedSak = sakTestRepository.findById(sakId).get();
		assertGjenaapnetSak(updatedSak);
	}

	@Test
	public void shouldGjenaapneSakIfFagsakHasManyArkivsaks() {
		setupStubs();
		ArrayList<Long> sakIds = new ArrayList<>();
		for (int i = 0; i < 5; i++)
			sakIds.add(persistDefaultAvsluttetSak());

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));

		sakIds.forEach(sakId -> assertGjenaapnetSak(sakTestRepository.findById(sakId).get()));
	}

	@Test
	public void shouldGjenaapneSakIfBrukerHasManyAktoerids() {
		stubAzure();
		happyAktoerIdHistoriskStub();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);

		long sakId = persistDefaultAvsluttetSak();

		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID_HISTORISK, FAGSAK_SYSTEM, FAGSAK_ID);
		sak.setSakStatus(AVSLUTTET);
		sak.setDatoAvsluttet(Date.from(now()));
		long sakId2 = sakTestRepository.persist(sak).getSakId();

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(OK));
		assertGjenaapnetSak(sakTestRepository.findById(sakId).get());
		assertGjenaapnetSak(sakTestRepository.findById(sakId2).get());
	}

	@Test
	public void shouldNotGjenaapneSakIfAvlevert() {
		setupStubs();

		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID_HISTORISK, FAGSAK_SYSTEM, FAGSAK_ID);
		sak.setSakStatus(AVSLUTTET);
		sak.setDatoAvsluttet(Date.from(now()));
		sak.setAvleveringStatus(AvleveringStatusCode.AVLEVERT);
		sakTestRepository.persist(sak);

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString("Fant ingen arkivsak for fagsakId=0123A21 og fagsaksystem=IT01"));
	}

	@Test
	public void shouldNotGjenaapneSakIfRequestFagsaksystemIsNotSakFagsaksystem() {
		setupStubs();

		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID_HISTORISK, "PP01", FAGSAK_ID);
		sak.setSakStatus(AVSLUTTET);
		sak.setDatoAvsluttet(Date.from(now()));
		sakTestRepository.persist(sak);

		commitAndStartNewTransaction();

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString("Fant ingen arkivsak for fagsakId=0123A21 og fagsaksystem=IT01"));
	}

	@Test
	public void shouldReturnBadRequestWhenNoArkivsakSakFound() {
		setupStubs();

		var requestEntity = new HttpEntity<>(createAktoerIdGjenaapneSakRequest(), createHeadersWithClientCredentialTokenAndNavUserId());
		ResponseEntity<String> response = restTemplate.exchange(URL_GJENAAPNE_SAK, PATCH, requestEntity, String.class);
		assertThat(response.getStatusCode(), is(BAD_REQUEST));
		assertThat(response.getBody(), containsString("Fant ingen arkivsak for fagsakId=0123A21 og fagsaksystem=IT01"));
	}

	private void assertGjenaapnetSak(Sak updatedSak) {
		assertThat(updatedSak.getSakStatus(), is(AAPEN));
		assertThat(updatedSak.getEndretAv(), is(NAV_IDENT_SAKSBEHANDLER));
		assertThat(updatedSak.getEndretKildeNavn(), is(KALLENDE_APP));
		assertThat(updatedSak.getDatoEndret().getDay(), is(Date.from(now()).getDay()));
		assertThat(updatedSak.getDatoAvsluttet(), is(nullValue()));
	}

	private void setupStubs() {
		stubAzure();
		happyAktoerIdStub();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
	}

	private long persistDefaultAvsluttetSak() {
		Sak sak = createSakForAktoerId(TEMA, AKTOER_ID, FAGSAK_SYSTEM, FAGSAK_ID);
		sak.setSakStatus(AVSLUTTET);
		sak.setDatoAvsluttet(Date.from(now()));
		long sakId = sakTestRepository.persist(sak).getSakId();

		commitAndStartNewTransaction();
		return sakId;
	}

	private long persistDefaultAvsluttetSakForOrganisasjon() {
		Sak sak = createSakForOrgNr(TEMA, GSAK_ORGNR, FAGSAK_SYSTEM, FAGSAK_ID);
		sak.setSakStatus(AVSLUTTET);
		sak.setDatoAvsluttet(Date.from(now()));

		long sakId = sakTestRepository.persist(sak).getSakId();
		commitAndStartNewTransaction();

		return sakId;
	}

	private GjenaapneSakRequest.GjenaapneSakRequestBuilder createDefaultGjenaapneSakRequestBuilder() {
		return GjenaapneSakRequest.builder()
				.tema(TEMA)
				.fagsakId(FAGSAK_ID)
				.fagsaksystem(FAGSAK_SYSTEM);
	}

	private GjenaapneSakRequest createOrganisasjonGjenaapneSakRequest() {
		return createDefaultGjenaapneSakRequestBuilder()
				.bruker(new Bruker(ORGNR, GSAK_ORGNR))
				.build();
	}

	private GjenaapneSakRequest createAktoerIdGjenaapneSakRequest() {
		return createDefaultGjenaapneSakRequestBuilder()
				.bruker(new Bruker(AKTOERID, AKTOER_ID))
				.build();
	}
}
