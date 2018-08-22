package no.nav.dokarkiv.behandlejournal.v3.tjoark065;

import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.v3.datautil.BehandleJournalCommonDataUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerNotatHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerNotatHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoernotat.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Tests for {@link JournalfoerNotatHenvendelseV3RequestMapper}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalfoerNotatHenvendelseV3RequestMapperTest {
	private static final String SPORING_FORNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_ETTERNAVN;

	@Mock
	private SporingMapper sporingMapperMock;
	@InjectMocks
	private JournalfoerNotatHenvendelseV3RequestMapper requestMapper;

	private JournalfoerNotatRequest wsRequest;
	private JournalfoerNotatHenvendelseRequest domainRequest;
	private Journalpost notatWsJournalpost;

	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
	}

	private void createRequest() throws Exception {
		notatWsJournalpost = JournalfoerNotatHenvendelseDataUtil.createJournalpost();
		wsRequest = new JournalfoerNotatRequest();
		wsRequest.setJournalpost(notatWsJournalpost);
		wsRequest.setPersonFornavn(SPORING_FORNAVN);
		wsRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		wsRequest.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldMapJournalfoerNotatHenvendelseRequestToTransferObject() throws Exception {
		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		JournalfoerNotatHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, notatWsJournalpost);
	}

	@Test
	public void shouldMapJournalfoerNotatHenvendelseRequestWithBrukerOrganisasjonToTransferObject() throws Exception {
		notatWsJournalpost.getForBruker().clear();
		notatWsJournalpost.getForBruker().add(BehandleJournalCommonDataUtil.createOrganisasjon());

		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		JournalfoerNotatHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, notatWsJournalpost);
	}

	@Test
	public void shouldDelegateToSporingmapper() {
		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();

		verify(sporingMapperMock).mapSporingsinfo(domainJournalpost, JournalfoerNotatHenvendelseDataUtil.OPPRETTET_AV_NAVN);
	}
}
