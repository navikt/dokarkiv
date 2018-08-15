package no.nav.dokarkiv.behandlejournal.v3.tjoark062;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.FerdigstillDokumentopplastingRequest;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link DefaultFerdigstillDokumentopplastingV3RequestMapper}
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public class DefaultFerdigstillDokumentopplastingV3RequestMapperTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String APPLIKASJONS_ID = "applikasjonsid";
	private static final String JOURNALPOST_ID = "100";

	private DefaultFerdigstillDokumentopplastingV3RequestMapper mapper;

	private FerdigstillDokumentopplastingRequest wsRequest;

	@Before
	public void setUp() {
		mapper = new DefaultFerdigstillDokumentopplastingV3RequestMapper();
		createRequest();
	}

	private void createRequest() {
		wsRequest = new FerdigstillDokumentopplastingRequest();
		wsRequest.setJournalpostId(JOURNALPOST_ID);
		wsRequest.setPersonFornavn(SPORING_FORNAVN);
		wsRequest.setPersonEtternavn(SPORING_ETTERNAVN);
		wsRequest.setApplikasjonsID(APPLIKASJONS_ID);
	}

	@Test
	public void shouldMapRequestFromWSToDomain() {
		no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequest  domainRequest = mapper
				.map(wsRequest);

		assertThat(domainRequest.getJournalpostId(), is(Long.parseLong(JOURNALPOST_ID)));
		assertThat(domainRequest.getSporingsMetaData().getPersonFornavn(), is(SPORING_FORNAVN));
		assertThat(domainRequest.getSporingsMetaData().getPersonEtternavn(), is(SPORING_ETTERNAVN));
		assertThat(domainRequest.getSporingsMetaData().getApplikasjonsID(), is(APPLIKASJONS_ID));
	}
}
