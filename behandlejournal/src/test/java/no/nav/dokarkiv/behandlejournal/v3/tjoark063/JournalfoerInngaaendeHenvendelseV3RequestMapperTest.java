package no.nav.dokarkiv.behandlejournal.v3.tjoark063;

import static org.mockito.Mockito.verify;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.v3.datautil.BehandleJournalCommonDataUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerInngaaendeHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerInngaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerinngaaendehenvendelse.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Test class for
 * DefaultJournalfoerInngaaendeHenvendelseRequestMapper.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@RunWith(MockitoJUnitRunner.class)
public class JournalfoerInngaaendeHenvendelseV3RequestMapperTest {

	private static final String SPORING_FORNAVN = JournalfoerInngaaendeHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerInngaaendeHenvendelseDataUtil.SPORING_ETTERNAVN;

	@Mock
	private SporingMapper sporingMapperMock;

	@InjectMocks
	private JournalfoerInngaaendeHenvendelseV3RequestMapper requestMapper;

	private JournalfoerInngaaendeHenvendelseRequest wsRequest;
	private no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseRequest domainRequest;
	private Journalpost inngaaendeWsJournalpost;


	@Before
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
	}

	private void createRequest() throws Exception {
		inngaaendeWsJournalpost = JournalfoerInngaaendeHenvendelseDataUtil.createJournalpost();
		wsRequest = new JournalfoerInngaaendeHenvendelseRequest();
		wsRequest.setJournalpost(inngaaendeWsJournalpost);
		wsRequest.setPersonFornavn(SPORING_FORNAVN);
		wsRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		wsRequest.setApplikasjonsID("applikasjonsid");
	}

	@Test
	public void shouldMapJournalfoerInngaaendeHenvendelseRequestToTransferObject() throws Exception {
		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		JournalfoerInngaaendeHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, inngaaendeWsJournalpost);
	}

	@Test
	public void shouldMapJournalfoerInngaaendeHenvendelseRequestWithBrukerOrganisasjonToTransferObject() throws Exception {
		inngaaendeWsJournalpost.getForBruker().clear();
		inngaaendeWsJournalpost.getForBruker().add(BehandleJournalCommonDataUtil.createOrganisasjon());

		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();
		JournalfoerInngaaendeHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, inngaaendeWsJournalpost);
	}

	@Test
	public void shouldDelegateToSporingmapper() {
		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();

		verify(sporingMapperMock).mapSporingsinfo(domainJournalpost, JournalfoerInngaaendeHenvendelseDataUtil.OPPRETTET_AV_NAVN);
	}
}
