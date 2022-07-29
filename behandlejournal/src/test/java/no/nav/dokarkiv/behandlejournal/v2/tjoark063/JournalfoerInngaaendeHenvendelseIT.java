package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalV2Itest;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerInngaaendeHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerInngaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseResponse;
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
public class JournalfoerInngaaendeHenvendelseIT extends AbstractBehandleJournalV2Itest {

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
		response = behandleJournalProvider.journalfoerInngaaendeHenvendelse(request);

		persistedJournalpost = joarkRepository.findById(Long.valueOf(response.getJournalpostId())).get();
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
