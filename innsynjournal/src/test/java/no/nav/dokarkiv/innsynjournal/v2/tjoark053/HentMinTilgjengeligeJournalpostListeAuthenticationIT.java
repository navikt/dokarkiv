package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.innsynjournal.v2.AbstractInnsynJournalV2Itest;
import no.nav.dokarkiv.innsynjournal.v2.security.SubjectHandlerUtils;
import no.nav.dokarkiv.innsynjournal.v2.security.ThreadLocalSubjectHandler;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentTilgjengeligJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Sak;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the XACML-logic in InnsynJournalV1.hentMinTilgjengeligeJournalpostListe
 *
 * @author Ketill Fenne, Visma Consulting.
 */

public class HentMinTilgjengeligeJournalpostListeAuthenticationIT extends AbstractInnsynJournalV2Itest {

	private static final String FNR = "***gammelt_fnr***";

	@BeforeClass
	public static void setUpSecurity() throws Exception {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
	}

	@Test
	public void shouldAllowAccessToEksternbrukerWithAuthenticationlevel4() throws Exception {
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(createRequest());
		assertThat(response.getJournalpostListe().size(), is(0));
	}

	@Test
	public void shouldDenyAccessToSystemRessurs() throws Exception {
		testAccessDeniedForIdentType(IdentType.Systemressurs);
	}

	@Test
	public void shouldDenyAccessToSamhandler() throws Exception {
		testAccessDeniedForIdentType(IdentType.Samhandler);
	}

	@Test
	public void shouldDenyAccessToProsess() throws Exception {
		testAccessDeniedForIdentType(IdentType.Prosess);
	}

	@Test
	public void shouldDenyAccessToSikkerhet() throws Exception {
		testAccessDeniedForIdentType(IdentType.Sikkerhet);
	}

	@Test
	public void shouldDenyAccessToInternbruker() throws Exception {
		testAccessDeniedForIdentType(IdentType.InternBruker);
	}

	@Test
	public void shouldDenyAccessToEksternbrukerWithAuthenticationLevelBelow4() throws Exception {
		SubjectHandlerUtils.setEksternBruker("1", 3, "");

		expectedException.expect(HentTilgjengeligJournalpostListeSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentTilgjengeligJournalpostListe(createRequest());
	}

	@Test
	public void shouldDenyAccessToEksternbrukerWithAuthenticationLevelAbove4() throws Exception {
		SubjectHandlerUtils.setEksternBruker("1", 44, "");

		expectedException.expect(HentTilgjengeligJournalpostListeSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentTilgjengeligJournalpostListe(createRequest());
	}

	private HentTilgjengeligJournalpostListeRequest createRequest() {
		HentTilgjengeligJournalpostListeRequest hentTilgjengeligJournalpostListeRequest = new HentTilgjengeligJournalpostListeRequest();
		Sak sak = new Sak();
		sak.setSakId("1");
		Fagsystemer fagsystemer = new Fagsystemer();
		fagsystemer.setValue(FagsystemCode.PEN.name());
		sak.setFagsystem(fagsystemer);
		hentTilgjengeligJournalpostListeRequest.getSakListe().add(sak);
		return hentTilgjengeligJournalpostListeRequest;
	}


	private void testAccessDeniedForIdentType(IdentType identType) throws HentTilgjengeligJournalpostListeSikkerhetsbegrensning {
		setSubjectWithIdenttype(identType);

		expectedException.expect(HentTilgjengeligJournalpostListeSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentTilgjengeligJournalpostListe(createRequest());
	}

	private void setSubjectWithIdenttype(IdentType identType) {
		SubjectHandlerUtils.setSubject(new SubjectHandlerUtils.SubjectBuilder("222", identType).getSubject());
	}


}
