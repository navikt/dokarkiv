package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.v3.datautil.BehandleJournalCommonDataUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerUtgaaendeHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerUtgaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

/**
 * Tests for DefaultJournalfoerUtgaaendeHenvendelseRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@ExtendWith(MockitoExtension.class)
public class JournalfoerUtgaaendeHenvendelseV3RequestMapperTest {
	private static final String SPORING_FORNAVN = JournalfoerUtgaaendeHenvendelseDataUtil.OPPRETTET_AV_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerUtgaaendeHenvendelseDataUtil.OPPRETTET_AV_ETTERNAVN;
	
	@Mock
	private SporingMapper sporingMapperMock;
	@InjectMocks
	private JournalfoerUtgaaendeHenvendelseV3RequestMapper requestMapper;

	private JournalfoerUtgaaendeHenvendelseRequest wsRequest;
	private no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseRequest domainRequest;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2014-08-27T12:00:00");
		createRequest();
	}

	private void createRequest() throws Exception {
		wsRequest = new JournalfoerUtgaaendeHenvendelseRequest();
		wsRequest.setJournalpost(JournalfoerUtgaaendeHenvendelseDataUtil.creatJournalpost());
		wsRequest.setPersonFornavn(SPORING_FORNAVN);
		wsRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		wsRequest.setApplikasjonsID("applikasjonsid");

	}

	@Test
	public void shouldMapJournalfoerUtgaaendeHenvendelseRequestToTransferObject() throws Exception {

		domainRequest = requestMapper.map(wsRequest);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();

		JournalfoerUtgaaendeHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, wsRequest.getJournalpost());
	}

	@Test
	public void shouldMapJournalfoerUtgaaendeHenvendelseRequestWithBrukerOrganisasjonToTransferObject() throws Exception {
		wsRequest.getJournalpost().getForBruker().clear();
		wsRequest.getJournalpost().getForBruker().add(BehandleJournalCommonDataUtil.createOrganisasjon());
		domainRequest = requestMapper.map(wsRequest);
		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();

		JournalfoerUtgaaendeHenvendelseAssertUtil.assertEqualJournalposts(domainJournalpost, wsRequest.getJournalpost());
	}

	@Test
	public void shouldDelegateToSporingmapper() {
		domainRequest = requestMapper.map(wsRequest);

		no.nav.dokarkiv.core.domain.entities.Journalpost domainJournalpost = domainRequest.getJournalpost();

		verify(sporingMapperMock).mapSporingsinfo(domainJournalpost, JournalfoerUtgaaendeHenvendelseDataUtil.SPORING_NAVN);
	}

}
