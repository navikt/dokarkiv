package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3Itest;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerNotatHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerNotatHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integrations tests for the journalfoerNotatHenvendelse operation.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseV3IT extends AbstractBehandleJournalV3Itest {
	private static final String SPORING_FORNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_ETTERNAVN;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-07-11T12:00");
		RequestContextSetter.setRequestContextForUnitTest();
		SubjectHandlerUtils.setInternBruker("userId");
	}

	private JournalfoerNotatRequest createRequest() throws Exception {
		JournalfoerNotatRequest request = new JournalfoerNotatRequest();
		request.setJournalpost(JournalfoerNotatHenvendelseDataUtil.createJournalpost());
		request.setPersonFornavn(SPORING_FORNAVN);
		request.setPersonEtternavn(SPORING_ETTERNAVN);
		request.setApplikasjonsID("applikasjonsid");
		return request;
	}

	@Test
	public void shouldReturnJournalpostIdAndDokumentIdForHoveddokumentWhenJournalpostIsOpprettet() throws Exception {
		abacPermit();

		JournalfoerNotatRequest request = createRequest();
		JournalfoerNotatResponse response = behandleJournalV3Provider.journalfoerNotat(request);

		assertThat(response.getJournalpostId(), is(notNullValue()));
		verify(postRequestedFor(urlEqualTo("/abac")).withRequestBody(equalToJson(stringFromClasspath("abac/v3/tjoark065.json"))));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		abacPermit();

		JournalfoerNotatRequest request = createRequest();

		JournalfoerNotatResponse response = behandleJournalV3Provider.journalfoerNotat(request);
		Journalpost persistedJournalpost = journalpostTestRepository.findById(Long.valueOf(response.getJournalpostId())).get();

		JournalfoerNotatHenvendelseAssertUtil.assertEqualJournalposts(persistedJournalpost, request.getJournalpost());
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(persistedJournalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getTilknyttetAvNavn(),
				is(JournalfoerNotatHenvendelseDataUtil.OPPRETTET_AV_NAVN));
	}

	@Test
	public void shouldThrowSikkerhetsbegrensningWhenAbacDenies() throws Exception {
		abacDeny();

		JournalfoerNotatRequest request = createRequest();

		assertThrows(JournalfoerNotatSikkerhetsbegrensning.class,
				() -> behandleJournalV3Provider.journalfoerNotat(request));
	}
}