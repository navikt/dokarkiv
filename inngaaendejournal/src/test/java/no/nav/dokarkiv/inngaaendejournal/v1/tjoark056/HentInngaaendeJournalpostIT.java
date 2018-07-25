package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.dokarkiv.inngaaendejournal.v1.AbstractInngaaendeJournalV1Itest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 * @author Martin Burheim Tingstad
 */
public class HentInngaaendeJournalpostIT extends AbstractInngaaendeJournalV1Itest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Before
	public void setUp() {
		RequestContextSetter.setRequestContextForUnitTest();
		SubjectHandlerUtils.setSystemressurs("srvfpsak");
	}

	@Test
	public void shouldFailWhenABACDenies() throws Exception {
		abacDeny();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		try {
			inngaaendeJournalProvider.hentJournalpost(defaultHentJournalpostRequest(journalpost.getJournalpostId().toString()));
			fail();
		} catch (HentJournalpostSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/hentinngaaendejournalpost.json"))));
	}

	@Test
	public void should_hentJournalpost_from_repository() throws Exception {
		abacPermit();

		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));
		HentJournalpostResponse response = inngaaendeJournalProvider.hentJournalpost(defaultHentJournalpostRequest(journalpost.getJournalpostId()
				.toString()));

		InngaaendeJournalpost inngaaendeJournalpost = response.getInngaaendeJournalpost();
		assertThat(inngaaendeJournalpost.getJournaltilstand(), is(Journaltilstand.ENDELIG));
		assertThat(inngaaendeJournalpost.getJournalfEnhet(), is("SesamStasjon"));
	}

	@Test
	public void should_throw_HentJournalpostJournalpostIkkeFunnet_when_journalpost_missing() throws Exception {
		thrown.expect(HentJournalpostJournalpostIkkeFunnet.class);

		inngaaendeJournalProvider.hentJournalpost(defaultHentJournalpostRequest("404"));
	}

	@Test
	public void should_throw_HentJournalpostJournalpostIkkeInngaaende_when_journalpost_ikke_inngaaende() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.J));

		thrown.expect(HentJournalpostJournalpostIkkeInngaaende.class);

		inngaaendeJournalProvider.hentJournalpost(defaultHentJournalpostRequest(journalpost.getJournalpostId().toString()));
	}

	@Test
	public void should_throw_HentJournalpostUgyldigInput_when_journalpostId_invalid() throws Exception {
		thrown.expect(HentJournalpostUgyldigInput.class);

		inngaaendeJournalProvider.hentJournalpost(defaultHentJournalpostRequest(""));
	}

	private HentJournalpostRequest defaultHentJournalpostRequest(String journalpostId) {
		HentJournalpostRequest request = new HentJournalpostRequest();
		request.setJournalpostId(journalpostId);
		return request;
	}

}
