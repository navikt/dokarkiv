package no.nav.dokarkiv.innsynjournal.v2.tjoark054;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.createDokumentFil;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpost;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.innsynjournal.v2.AbstractInnsynJournalV2Itest;
import no.nav.modig.core.domain.IdentType;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests the XACML-logic in InnsynJournalV2.hentDokument
 *
 * @author Ketill Fenne, Visma Consulting.
 */
public class HentDokumentAuthenticationIT extends AbstractInnsynJournalV2Itest {

	private static final String DEFAULT_JOURNALPOST_ID = "1";
	private static final String DEFAULT_DOKUMENT_ID = "2";
	private static final String DOKUMENTITTEL = "Dokumentittel";
	private static final String FNR = "***gammelt_fnr***";

	@BeforeClass
	public static void setUpSecurity() throws Exception {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
	}

	@Test
	public void shouldAllowAccessToEksternbrukerWithAuthenticationlevel4() throws Exception {
		Journalpost journalpost = buildAndPersistJournalpost();
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(journalpost.getJournalpostId().toString());
		request.setDokumentId(journalpost.findAllDokumentInfos().iterator().next().getDokumentInfoId().toString());

		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");

		HentDokumentResponse response = innsynJournalV2Provider.hentDokument(request);
		assertThat(response.getVariantFormat().getValue(), is(VariantFormatCode.ARKIV.name()));
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

		expectedException.expect(HentDokumentSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentDokument(createRequest());
	}

	@Test
	public void shouldDenyAccessToEksternbrukerWithAuthenticationLevelAbove4() throws Exception {
		SubjectHandlerUtils.setEksternBruker("1", 44, "");

		expectedException.expect(HentDokumentSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentDokument(createRequest());
	}

	private HentDokumentRequest createRequest() {
		HentDokumentRequest request = new HentDokumentRequest();
		request.setJournalpostId(DEFAULT_JOURNALPOST_ID);
		request.setDokumentId(DEFAULT_DOKUMENT_ID);
		return request;
	}

	private Journalpost buildAndPersistJournalpost() {
		dokumentFilRepository.save(createDokumentFil().build());
		return joarkRepository.save(createJournalpost(DOKUMENTITTEL, FIL_UUID)
				.avsenderMottakerId(FNR).build());

	}

	private void testAccessDeniedForIdentType(IdentType identType) throws HentDokumentDokumentIkkeFunnet, HentDokumentSikkerhetsbegrensning {
		setSubjectWithIdenttype(identType);

		expectedException.expect(HentDokumentSikkerhetsbegrensning.class);
		expectedException.expectMessage(is("Access denied"));
		innsynJournalV2Provider.hentDokument(createRequest());
	}

	private void setSubjectWithIdenttype(IdentType identType) {
		SubjectHandlerUtils.setSubject(new SubjectHandlerUtils.SubjectBuilder("222", identType).getSubject());
	}

}
