package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3Itest;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerInngaaendeHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerInngaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerinngaaendehenvendelse.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration test of operation JournalfoerInngaaendeHenvendelse.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseV3IT extends AbstractBehandleJournalV3Itest {

	private static final String SPORING_FORNAVN = JournalfoerInngaaendeHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerInngaaendeHenvendelseDataUtil.SPORING_ETTERNAVN;

	private JournalfoerInngaaendeHenvendelseRequest request;
	private JournalfoerInngaaendeHenvendelseResponse response;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private Journalpost wsJournalpost;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2013-12-24T12:00:00");
		RequestContextSetter.setRequestContextForUnitTest();
		wsJournalpost = JournalfoerInngaaendeHenvendelseDataUtil.createJournalpost();
		createRequest();
		response = behandleJournalV3Provider.journalfoerInngaaendeHenvendelse(request);

		persistedJournalpost = journalpostRepositorySkjermet.findById(Long.valueOf(response.getJournalpostId())).get();
	}

	private void createRequest() throws Exception {
		request = new JournalfoerInngaaendeHenvendelseRequest();
		request.setJournalpost(wsJournalpost);
		request.setPersonFornavn(SPORING_FORNAVN);
		request.setPersonEtternavn(SPORING_ETTERNAVN);
		request.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldReturnJournalpostIdWhenJournalpostIsOpprettet() {
		assertThat(response.getJournalpostId(), is(notNullValue()));
	}

	@Test
	public void shouldVerifyCorrectFieldsInJournalpost() throws Exception {
		JournalfoerInngaaendeHenvendelseAssertUtil.assertEqualJournalposts(persistedJournalpost, wsJournalpost);
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(persistedJournalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getTilknyttetAvNavn(),
				is(JournalfoerInngaaendeHenvendelseDataUtil.OPPRETTET_AV_NAVN));
	}
}
