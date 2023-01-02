package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalV2Itest;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerNotatHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerNotatHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integrations tests for the journalfoerNotatHenvendelse operation.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseIT extends AbstractBehandleJournalV2Itest {
	private static final String SPORING_FORNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_ETTERNAVN;

	private JournalfoerNotatRequest request;
	private JournalfoerNotatResponse response;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private Journalpost wsJournalpost;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-07-11T12:00");
		RequestContextSetter.setRequestContextForUnitTest();
		wsJournalpost = JournalfoerNotatHenvendelseDataUtil.createJournalpost();
		createRequest();

		response = behandleJournalProvider.journalfoerNotat(request);
		persistedJournalpost = journalpostRepositorySkjermet.findById(Long.valueOf(response.getJournalpostId())).get();
	}

	private void createRequest() throws Exception {
		request = new JournalfoerNotatRequest();
		request.setJournalpost(wsJournalpost);
		request.setPersonFornavn(SPORING_FORNAVN);
		request.setPersonEtternavn(SPORING_ETTERNAVN);
		request.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldReturnJournalpostIdAndDokumentIdForHoveddokumentWhenJournalpostIsOpprettet() {
		assertThat(response.getJournalpostId(), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		JournalfoerNotatHenvendelseAssertUtil.assertEqualJournalposts(persistedJournalpost, wsJournalpost);
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.FL));
		assertThat(persistedJournalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getTilknyttetAvNavn(),
				is(JournalfoerNotatHenvendelseDataUtil.OPPRETTET_AV_NAVN));
	}

}