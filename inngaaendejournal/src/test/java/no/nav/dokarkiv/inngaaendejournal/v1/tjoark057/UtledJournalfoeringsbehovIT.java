package no.nav.dokarkiv.inngaaendejournal.v1.tjoark057;

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
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journalfoeringsbehov;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 * @author Joakim Bjørnstad, Jbit AS
 */
public class UtledJournalfoeringsbehovIT extends AbstractInngaaendeJournalV1Itest {

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
			inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest(journalpost.getJournalpostId().toString()));
			fail();
		} catch (UtledJournalfoeringsbehovSikkerhetsbegrensning e) {
			assertThat(e.getMessage(), equalTo("Bruker har ikke tilgang til journalpost"));
		}
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/utledjournalfoeringsbehov.json"))));
	}

	@Test
	public void should_utledJournalfoeringsbehov() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.M).avsenderMottaker(null));
		UtledJournalfoeringsbehovResponse response = inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest(journalpost.getJournalpostId().toString()));

		JournalpostMangler journalpostMangler = response.getJournalfoeringsbehov();
		assertThat(journalpostMangler.getAvsenderNavn(), is(Journalfoeringsbehov.MANGLER));
		assertThat(journalpostMangler.getAvsenderId(), is(Journalfoeringsbehov.MANGLER_IKKE));
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostIkkeFunnet_when_journalpost_missing() throws Exception {
		thrown.expect(UtledJournalfoeringsbehovJournalpostIkkeFunnet.class);

		inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest("404"));
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostIkkeInngaaende_when_journalpost_ikke_inngaaende() throws Exception {
		abacPermit();
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.U, JournalStatusCode.M));

		thrown.expect(UtledJournalfoeringsbehovJournalpostIkkeInngaaende.class);

		inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest(journalpost.getJournalpostId().toString()));
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovUgyldigInput_when_journalpostId_invalid() throws Exception {
		thrown.expect(UtledJournalfoeringsbehovUgyldigInput.class);

		inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest(""));
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostKanIkkeBehandles_when_journalpost_not_midlertidig() throws Exception {
		Journalpost journalpost = buildAndCommit(JournalpostTestDataProvider.buildJournalpost(JournalpostTypeCode.I, JournalStatusCode.J));

		thrown.expect(UtledJournalfoeringsbehovJournalpostKanIkkeBehandles.class);

		inngaaendeJournalProvider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest(journalpost.getJournalpostId().toString()));
	}

	private UtledJournalfoeringsbehovRequest defaultUtledJournalfoeringsbehovRequest(String journalpostId) {
		UtledJournalfoeringsbehovRequest request = new UtledJournalfoeringsbehovRequest();
		request.setJournalpostId(journalpostId);
		return request;
	}

}
