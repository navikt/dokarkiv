package no.nav.dokarkiv.behandlejournal.v3.tjoark064;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3Itest;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerInngaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerUtgaaendeHenvendelseAssertUtil;
import no.nav.dokarkiv.behandlejournal.v3.datautil.JournalfoerUtgaaendeHenvendelseDataUtil;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerutgaaendehenvendelse.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Integration tests for the journalfoerUtgaaendeHenvendelse
 * operation.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerUtgaaendeHenvendelseV3IT extends AbstractBehandleJournalV3Itest {
	private static final String SPORING_FORNAVN = JournalfoerUtgaaendeHenvendelseDataUtil.OPPRETTET_AV_FORNAVN;
	private static final String SPORING_ETTERNAVN = JournalfoerUtgaaendeHenvendelseDataUtil.OPPRETTET_AV_ETTERNAVN;

	private JournalfoerUtgaaendeHenvendelseRequest request;
	private JournalfoerUtgaaendeHenvendelseResponse response;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private Journalpost wsJournalpost;

	@BeforeEach
	public void setUp() throws Exception {
		DateProvider.configure(true, "2018-07-11T12:00");
		RequestContextSetter.setRequestContextForUnitTest();
		wsJournalpost = JournalfoerUtgaaendeHenvendelseDataUtil.creatJournalpost();
		createRequest();

		response = behandleJournalV3Provider.journalfoerUtgaaendeHenvendelse(request);
		persistedJournalpost = journalpostRepositorySkjermet.findById(Long.valueOf(response.getJournalpostId())).get();
	}

	private void createRequest() {
		request = new JournalfoerUtgaaendeHenvendelseRequest();
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
		JournalfoerUtgaaendeHenvendelseAssertUtil.assertEqualJournalposts(persistedJournalpost, wsJournalpost);
		assertThat(persistedJournalpost.getJournalstatus(), is(JournalStatusCode.FS));
		assertThat(persistedJournalpost.getJournalDato(), is(DateProvider.getToday()));
		assertThat(persistedJournalpost.findHoveddokumentDokumentInfoRelasjon().getTilknyttetAvNavn(),
				is(JournalfoerInngaaendeHenvendelseDataUtil.SPORING_NAVN));
	}
}
