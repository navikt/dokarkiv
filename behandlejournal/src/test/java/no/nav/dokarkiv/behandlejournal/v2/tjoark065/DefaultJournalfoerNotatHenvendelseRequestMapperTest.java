package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.v2.datautil.BehandleJournalCommonDataUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerNotatHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v2.datautil.JournalfoerNotatHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DefaultJournalfoerNotatHenvendelseRequestMapper}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class DefaultJournalfoerNotatHenvendelseRequestMapperTest {
	private static final String SPORING_FORNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerNotatHenvendelseDataUtil.SPORING_ETTERNAVN;

	@Mock
	private SporingMapper sporingMapperMock;
	@InjectMocks
	private DefaultJournalfoerNotatHenvendelseRequestMapper requestMapper;

	private JournalfoerNotatRequest wsRequest;
	private JournalfoerNotatHenvendelseRequest domainRequest;
	private Journalpost notatWsJournalpost;

	@BeforeEach
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
