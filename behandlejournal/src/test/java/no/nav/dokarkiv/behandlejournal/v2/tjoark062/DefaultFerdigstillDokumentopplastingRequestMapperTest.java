package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Tests for {@link DefaultFerdigstillDokumentopplastingRequestMapper}
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public class DefaultFerdigstillDokumentopplastingRequestMapperTest {
	private static final String SPORING_FORNAVN = "fornavn";
	private static final String SPORING_ETTERNAVN = "etternavn";
	private static final String APPLIKASJONS_ID = "applikasjonsid";
	private static final String JOURNALPOST_ID = "100";

	private DefaultFerdigstillDokumentopplastingRequestMapper mapper;

	private FerdigstillDokumentopplastingRequest wsRequest;

	@BeforeEach
	public void setUp() {
		mapper = new DefaultFerdigstillDokumentopplastingRequestMapper();
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
		no.nav.dokarkiv.behandlejournal.v2.tjoark062.FerdigstillDokumentopplastingRequest  domainRequest = mapper
				.map(wsRequest);

		assertThat(domainRequest.getJournalpostId(), is(Long.parseLong(JOURNALPOST_ID)));
		assertThat(domainRequest.getSporingsMetaData().getPersonFornavn(), is(SPORING_FORNAVN));
		assertThat(domainRequest.getSporingsMetaData().getPersonEtternavn(), is(SPORING_ETTERNAVN));
		assertThat(domainRequest.getSporingsMetaData().getApplikasjonsID(), is(APPLIKASJONS_ID));
	}
}
