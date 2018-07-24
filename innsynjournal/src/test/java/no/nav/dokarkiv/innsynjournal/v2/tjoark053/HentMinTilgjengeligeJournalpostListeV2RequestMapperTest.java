package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.util.AssertionErrors.assertTrue;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.repository.SakFagsystem;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Sak;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * Unit tests for {@link HentMinTilgjengeligeJournalpostListeV2RequestMapper}
 *
 * @author Ketill Fenne, Visma Consulting.
 *
 */
public class HentMinTilgjengeligeJournalpostListeV2RequestMapperTest {

	public static final FagsystemCode FAGSYSTEM = FagsystemCode.BID;
	public static final String SAKS_ID = "0";
	public static final boolean MERK_INNSYN = true;

	private HentMinTilgjengeligeJournalpostListeV2RequestMapper mapper;

	@Before
	public void setUp() throws Exception {
		mapper = new HentMinTilgjengeligeJournalpostListeV2RequestMapper();
	}

	@Test
	public void shouldMap() {
		HentJournalpostListeToRequest requestTo = mapper.map(createRequest(MERK_INNSYN));
		assertTrue("MerkInnsyn should be true", requestTo.isMerkInnsynDokument());
		assertThat(requestTo.getSaksListe().size(), is(2));
		for (SakFagsystem sakFagsystem : requestTo.getSaksListe()) {
			assertThat(sakFagsystem.getSakId(), is(SAKS_ID));
			assertThat(sakFagsystem.getFagsystem(), is(FAGSYSTEM));
		}
	}

	private HentTilgjengeligJournalpostListeRequest createRequest(boolean merkInnsyn) {
		HentTilgjengeligJournalpostListeRequest request = new HentTilgjengeligJournalpostListeRequest();
		request.setMerkInnsynDokument(merkInnsyn);
		request.getSakListe().add(createSak(FAGSYSTEM, SAKS_ID));
		request.getSakListe().add(createSak(FAGSYSTEM, SAKS_ID));
		return request;
	}

	private Sak createSak(FagsystemCode fagsystemCode, String saksId) {
		Sak sak = new Sak();
		sak.setFagsystem(createFagsystem(fagsystemCode));
		sak.setSakId(saksId);
		return sak;
	}

	private Fagsystemer createFagsystem(FagsystemCode fagsystemCode) {
		Fagsystemer fagsystemer = new Fagsystemer();
		fagsystemer.setValue(fagsystemCode.name());
		return fagsystemer;
	}
}