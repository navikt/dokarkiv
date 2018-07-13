package no.nav.dokarkiv.innsynjournal.v2;

import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.ALTINN;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.SKAN_NETS;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.SKAN_PEN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.innsynjournal.v2.exceptions.SecurityLimitationAttributeException;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentJournalpostListeToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligJournalpostListeV2ResponseMapper;
import no.nav.dokarkiv.innsynjournal.v2.tjoark053.HentMinTilgjengeligeJournalpostListeService;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostService;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostToRequest;
import no.nav.dokarkiv.innsynjournal.v2.tjoark059.IdentifiserJournalpostV2ResponseMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link InnsynJournalV2SecurityFacade}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class InnsynJournalSecurityFacadeTest {

	private static final byte[] DOK = "dok".getBytes();
	private static final String USER_ID = "***gammelt_fnr***";
//	private static Date DAY_BEFORE_LEGAL_DATE = DateUtil.createDate(2015, Calendar.DECEMBER, 31);
//	private static final Date LEGAL_DATE = DateUtil.createDate(2016, Calendar.JANUARY, 1);
//	private static Date DAY_AFTER_LEGAL_DATE = DateUtil.createDate(2016, Calendar.JANUARY, 2);

	private static final long JOURNALPOST_ID = 1L;
	private static final long DOKUMENT_INFO_ID = 2L;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

//	@Mock
//	private HentDokumentService hentDokumentService;
//	@Mock
//	public HentJournalpost hentJournalpost;
//	@Mock
//	private AktoerConsumerService aktoerConsumerService;
	@Mock
	private HentMinTilgjengeligeJournalpostListeService hentMinTilgjengeligeJournalpostListeService;
	@Mock
	private HentMinTilgjengeligJournalpostListeV2ResponseMapper hentMinTilgjengeligJournalpostListeV2ResponseMapper;
	@Mock
	private IdentifiserJournalpostService identifiserJournalpostService;
	@Mock
	private IdentifiserJournalpostV2ResponseMapper identifiserJournalpostV2ResponseMapper;

	@InjectMocks
	private InnsynJournalV2SecurityFacade securityFacade;

	@Before
	public void setUp() throws Exception {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
//		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
//		SubjectHandlerUtils.setEksternBruker(USER_ID, 4, null);
//		securityFacade.setEarliestAllowedDate(LEGAL_DATE);
	}

	@Test
	public void shouldHentDokument() throws Exception {
		mockJournalpost(createLegalJournalpost());

		byte[] dokument = securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
		assertThat(dokument, is(DOK));
	}

	@Test
	public void shouldThrowNoJournalpostFoundWhenJournalpostIdNotExists() throws Exception {
		String message = "Not found";
//		when(hentJournalpost.hentJournalpost(eq(new HentJournalpostRequest(JOURNALPOST_ID)))).thenThrow(new NoJournalpostFoundException(message, JOURNALPOST_ID));

		expectedException.expect(NoJournalpostFoundException.class);
		expectedException.expectMessage(message);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldThrowTechnicalErrorIfNoSaksrelasjon() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setSaksrelasjon(null);
		mockJournalpost(legalJournalpost);

		expectedException.expect(IllegalStateException.class);
		expectedException.expectMessage("Journalpost med journalpostId=" + JOURNALPOST_ID + " er ferdigstilt, men mangler saksrelasjon.");

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldThrowDocumentNotFoundExceptionWhenDokumentInfoIdNotExistsOnJournalpost() throws Exception {
		mockJournalpost(createLegalJournalpost());

		expectedException.expect(DocumentNotFoundException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID + 1);
	}

	@Test
	public void shouldThrowSikkerhetsbegrensningWhenJournalDatoIsTooEarly() throws Exception {
//		Date createdDate = DAY_AFTER_LEGAL_DATE;
//		Date journalDate = DAY_BEFORE_LEGAL_DATE;
//		mockJournalpost(createJournalpost(createdDate, journalDate, VariantFormatCode.ARKIV, false));
//
//		expectedException.expect(SecurityLimitationAttributeException.class);
//
//		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID); FIXME
	}

	@Test
	public void shouldThrowExceptionWhenOpprettetDatoIsTooEarly() throws Exception {
//		Date createdDate = DAY_BEFORE_LEGAL_DATE;
//		Date journalDate = DAY_AFTER_LEGAL_DATE;
//		mockJournalpost(createJournalpost(createdDate, journalDate, VariantFormatCode.ARKIV, false));
//
//		expectedException.expect(SecurityLimitationAttributeException.class);
//
//		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID); FIXME
	}

	@Test
	public void shouldHandleNullDateAsToEarly() throws Exception {
//		Date journalDate = DAY_BEFORE_LEGAL_DATE;
//		mockJournalpost(createJournalpost(null, journalDate, VariantFormatCode.ARKIV, false));
//
//		expectedException.expect(SecurityLimitationAttributeException.class);
//
//		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID); FIXME
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIsUnderArbeid() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.D));

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldThrowExceptionWhenJournalpostIsAvbrutt() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.A));

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowJournalstatusJournalfort() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.J));

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowJournalstatusFS() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.FS));

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowJournalstatusFL() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.FL));

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowJournalstatusE() throws Exception {
		mockJournalpost(createJournalpost(JournalStatusCode.E));

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldThrowSikkerhetsbegrensningWhenFeilregistrertSaksrelasjon() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setSaksrelasjon(getSaksrelasjonBuilder()
				.feilregistrert(true)
				.build());
		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowWhenFeilregistrertFalse() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setSaksrelasjon(getSaksrelasjonBuilder()
				.feilregistrert(false)
				.build());
		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldThrowSikkerhetsbegrensningWhenFagomradeKTR() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setFagomrade(FagomradeCode.KTR);

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowMottakskanalSkanPen() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setMottakskanal(SKAN_PEN);


		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowMottakskanalSkanNets() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setMottakskanal(SKAN_NETS);

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowMottakskanalAltinn() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setMottakskanal(ALTINN);

		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowNotatWithoutAvsenderMottakerId() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setAvsenderMottakerId(null);
		legalJournalpost.setJournalposttype(JournalpostTypeCode.N);

		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowNotatDokInfoKategoriOtherThanForvaltningsnotat() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.N);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setKategori(DokumentKategoriCode.E_BLANKETT);

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowDokInfoKategoriOtherThanForvaltningsnotatForUtgaaende() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.U);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setKategori(DokumentKategoriCode.E_BLANKETT);

		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowNotatDokInfoOrganInternt() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.N);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setOrganInternt(true);

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowUtgaaendeDokInfoOrganInternt() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.U);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setOrganInternt(true);

		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowOdIdNotNull() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		DokumentInfo dokumentInfo = legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID);
		dokumentInfo.clearFildetaljerListe();
		dokumentInfo.addFilDetaljer(
				getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.ARKIV)
						.onDemandId("not null").build());

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowWhenVariantArkivIsOk() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		DokumentInfo dokumentInfo = legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID);
		dokumentInfo.clearFildetaljerListe();
		dokumentInfo.addFilDetaljer(
				getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.BREVBESTILLING)
						.onDemandId("not null").build());
		dokumentInfo.addFilDetaljer(
				getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.ARKIV)
						.onDemandId(null).build());

		mockJournalpost(legalJournalpost);

		byte[] bytes = securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
		assertThat(bytes, is(DOK));
	}

	@Test
	public void shouldThrowWhenFildetaljerNotExists() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		DokumentInfo dokumentInfo = legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID);
		dokumentInfo.clearFildetaljerListe();

		mockJournalpost(legalJournalpost);

		expectedException.expect(DocumentNotFoundException.class);
		expectedException.expectMessage("DokumentInfo med dokumentinfoId="
				+ DOKUMENT_INFO_ID + " på Journalpost med journalpostId="
				+ JOURNALPOST_ID + " har ikke en fildetaljer med VariantFormat=ARKIV");

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowNotatDokInfoUferdig() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.N);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		mockJournalpost(legalJournalpost);

		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowUtgaaendeDokInfoUferdig() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.U);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		mockJournalpost(legalJournalpost);
		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowInngaaendeDokInfoUferdig() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setJournalposttype(JournalpostTypeCode.I);
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setDokumentstatus(DokumentStatusCode.UNDER_REDIGERING);

		mockJournalpost(legalJournalpost);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowDokInfoInnskrenketPartsInnsyn() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setInnskrenketPartsinnsyn(true);

		mockJournalpost(legalJournalpost);
		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowDokInfoSlettet() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.getDokumentInfoFromJpDokInfoRelasjonerByDokumentInfoId(DOKUMENT_INFO_ID)
				.setSlettet(true);
		mockJournalpost(legalJournalpost);
		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowDifferentAvsenderMottaker() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setAvsenderMottakerId("***gammelt_fnr***");

//		HentAktoerIdForIdentResponseTo identResponseTo = new HentAktoerIdForIdentResponseTo("001", createIdentDetaljerList(USER_ID));
//		when(aktoerConsumerService.hentAktoerIdForIdent(eq(new HentAktoerIdForIdentRequestTo(USER_ID)))).thenReturn(identResponseTo); FIXME

		mockJournalpost(legalJournalpost);
		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldNotAllowWhenAvsenderMottakerIdIsNull() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setAvsenderMottakerId(null);

		mockJournalpost(legalJournalpost);
		expectedException.expect(SecurityLimitationAttributeException.class);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldReturnDocumentWhenAvsenderMottakerIdMatchesAndMottakskanalNavNo() throws Exception {
		mockJournalpost(createNAVNOJournalpost(USER_ID));

		byte[] bytes = securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
		assertThat(bytes, is(equalTo(DOK)));
	}

	@Test
	public void shouldReturnDocumentIfAvsenderMottakerExistsInHistoricalList() throws Exception {
		String historicalFnr = "***gammelt_fnr***";

//		HentAktoerIdForIdentResponseTo identResponseTo = new HentAktoerIdForIdentResponseTo("001",
//				createIdentDetaljerList("***gammelt_fnr***", historicalFnr, "***gammelt_fnr***"));
//		when(aktoerConsumerService.hentAktoerIdForIdent(eq(new HentAktoerIdForIdentRequestTo(USER_ID)))).thenReturn(identResponseTo); FIXME

		mockJournalpost(createNAVNOJournalpost(historicalFnr));

		byte[] bytes = securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
		assertThat(bytes, is(equalTo(DOK)));
	}

	@Test
	public void shouldFailIfPersonNotFound() throws Exception {
		Journalpost legalJournalpost = createLegalJournalpost();
		legalJournalpost.setAvsenderMottakerId("***gammelt_fnr***444");
		mockJournalpost(legalJournalpost);

//		doThrow(new PersonIkkeFunnetException(new Exception(), "Person not found"))
//				.when(aktoerConsumerService).hentAktoerIdForIdent(eq(new HentAktoerIdForIdentRequestTo(USER_ID))); FIXME

		expectedException.expect(RuntimeException.class);
		expectedException.expectMessage("Kan ikke utføre tilgangskontroll for pålogget bruker med fnr=" + USER_ID + " "
				+ "for journalpost med journalpostId=" + JOURNALPOST_ID);

		securityFacade.hentDokument(JOURNALPOST_ID, DOKUMENT_INFO_ID);
	}

	@Test
	public void shouldAllowAllDokumentInfoInnsyn() {
		HentJournalpostListeToRequest request = createRequest();
		Journalpost journalpost = createLegalJournalpost();
		Journalpost journalpost1 = createLegalJournalpost();
		mockHentMinJournalpostListe(request, journalpost, journalpost1);

		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
		assertThat(innsynJournalpostTos.size(), is(2));
	}

//	@Test
//	public void shouldSetAvsenderMottakerAndDokumentInnsynToCannotBeDecidedOnDkiPersonNotFound() throws PersonIkkeFunnetException {
//		Journalpost journalpost = createLegalJournalpost();
//		journalpost.setAvsenderMottakerId("notLoggedOnUser");
//		journalpost.setMottakskanal(NAV_NO);
//		HentJournalpostListeToRequest request = createRequest();
//		when(aktoerConsumerService.hentAktoerIdForIdent(any(HentAktoerIdForIdentRequestTo.class)))
//				.thenThrow(new PersonIkkeFunnetException(new Throwable(""), "person not found"));
//		mockHentMinJournalpostListe(request, journalpost);
//
//		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
//		assertThat(innsynJournalpostTos.get(0).getAvsenderMottaker(), is(InnsynJournalpostTo.AvsenderMottaker.KAN_IKKE_AVGJOERES));
//		assertDokumenInfoInnsyn(innsynJournalpostTos, DokumentInnsyn.KAN_IKKE_AVGJOERES); FIXME
//	}


//	@Test
//	public void shouldNotAllowDokumentInnsynWhenNotInnsendtByBruker() throws PersonIkkeFunnetException {
//		Journalpost journalpost = createLegalJournalpost();
//		journalpost.setAvsenderMottakerId("notLoggedOnUser");
//		journalpost.setMottakskanal(NAV_NO);
//		HentJournalpostListeToRequest request = createRequest();
//
//		mockHentMinJournalpostListe(request, journalpost);
//		HentAktoerIdForIdentResponseTo identResponseTo = new HentAktoerIdForIdentResponseTo("001", createIdentDetaljerList(USER_ID));
//		when(aktoerConsumerService.hentAktoerIdForIdent(eq(new HentAktoerIdForIdentRequestTo(USER_ID)))).thenReturn(identResponseTo);
//
//		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
//		assertDokumenInfoInnsyn(innsynJournalpostTos, DokumentInnsyn.NEI);
//	} FIXME

	@Test
	public void shouldAllowInnsynWhenInnsendtByBrukerAndMottaksKanalNav_no() {
		HentJournalpostListeToRequest request = createRequest();
		Journalpost journalpost = createLegalJournalpost();
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		mockHentMinJournalpostListe(request, journalpost);

		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
		assertDokumenInfoInnsyn(innsynJournalpostTos, InnsynJournalpostTo.DokumentInnsyn.JA);
	}

	@Test
	public void shouldNotAllowInnsynWhenMottakskanalSkanPen() throws Exception {
		HentJournalpostListeToRequest request = createRequest();
		Journalpost journalpost = createLegalJournalpost();
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setMottakskanal(SKAN_PEN);
		mockHentMinJournalpostListe(request, journalpost);

		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
		assertDokumenInfoInnsyn(innsynJournalpostTos, InnsynJournalpostTo.DokumentInnsyn.NEI);
	}

	@Test
	public void shouldNotAllowInnsynWhenMottakskanalSkanNets() throws Exception {
		HentJournalpostListeToRequest request = createRequest();
		Journalpost journalpost = createLegalJournalpost();
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		journalpost.setMottakskanal(SKAN_NETS);
		mockHentMinJournalpostListe(request, journalpost);

		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
		assertDokumenInfoInnsyn(innsynJournalpostTos, InnsynJournalpostTo.DokumentInnsyn.NEI);
	}

	@Test
	public void shouldNotAllowInnsynIfNoArkivVariant() {
//		HentJournalpostListeToRequest request = createRequest();
//		Journalpost journalpost = createJournalpost(DAY_AFTER_LEGAL_DATE, DAY_AFTER_LEGAL_DATE, VariantFormatCode.BREVBESTILLING, false);
//		journalpost.setJournalposttype(JournalpostTypeCode.U);
//		mockHentMinJournalpostListe(request, journalpost);
//
//		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
//		assertDokumenInfoInnsyn(innsynJournalpostTos, DokumentInnsyn.NEI); FIXME
	}

	@Test
	public void shouldNotAllowIfInnskrenketPartsinnsyn() {
//		HentJournalpostListeToRequest request = createRequest();
//		Journalpost journalpost = createJournalpost(DAY_AFTER_LEGAL_DATE, DAY_AFTER_LEGAL_DATE, VariantFormatCode.BREVBESTILLING, true);
//		journalpost.setJournalposttype(JournalpostTypeCode.U);
//		mockHentMinJournalpostListe(request, journalpost);
//
//		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
//		assertDokumenInfoInnsyn(innsynJournalpostTos, DokumentInnsyn.NEI); FIXME
	}

	@Test
	public void shouldNotSetDokumentInfoInnsynWhenNotMerkInnsynDokument() {
		HentJournalpostListeToRequest request = createRequest();
		request.setMerkInnsynDokument(false);
		Journalpost journalpost = createLegalJournalpost();
		mockHentMinJournalpostListe(request, journalpost);

		List<InnsynJournalpostTo> innsynJournalpostTos = securityFacade.hentMineTilgjengeligeJournalpostListe(request);
		assertThat(innsynJournalpostTos.size(), is(1));
		assertThat(innsynJournalpostTos.get(0).getDokumentInnsyn().size(), is(0));
	}

	@Test
	public void shouldAllowIdentifiserJpurnalpost() throws Exception {
		IdentifiserJournalpostToRequest request = new IdentifiserJournalpostToRequest();
		Journalpost journalpost = createLegalJournalpost();
		when(identifiserJournalpostService.identifiserJournalpost(request)).thenReturn(journalpost);
		InnsynJournalpostTo innsynJournalpostTo = securityFacade.identifiserJournalpost(request);
		assertThat(innsynJournalpostTo.getJournalpost(), is(journalpost));
	}

	private void assertDokumenInfoInnsyn(List<InnsynJournalpostTo> innsynJournalposts, InnsynJournalpostTo.DokumentInnsyn dokumentInnsyn) {
		assertThat(innsynJournalposts.size(), is(1));
		Map<Long, InnsynJournalpostTo.DokumentInnsyn> dokumentInnsyns = innsynJournalposts.get(0).getDokumentInnsyn();
		assertThat(dokumentInnsyns.size(), is(1));
		Long key = dokumentInnsyns.keySet().iterator().next();
		InnsynJournalpostTo.DokumentInnsyn innsyn = dokumentInnsyns.get(key);
		assertThat(innsyn, is(dokumentInnsyn));
	}

	private void mockHentMinJournalpostListe(HentJournalpostListeToRequest request, Journalpost... journalpost) {
		when(hentMinTilgjengeligeJournalpostListeService.hentMineTilgjengeligeJournalposter(request)).thenReturn(createJournalpostList(journalpost));
	}

	private HentJournalpostListeToRequest createRequest() {
		HentJournalpostListeToRequest hentJournalpostListeToRequest = new HentJournalpostListeToRequest();
		hentJournalpostListeToRequest.setMerkInnsynDokument(true);
		return hentJournalpostListeToRequest;
	}

	private List<Journalpost> createJournalpostList(Journalpost... journalpost) {
		List<Journalpost> journalposts = new ArrayList<>();
		journalposts.addAll(Arrays.asList(journalpost));
		return journalposts;
	}

//	private List<IdentDetaljerTo> createIdentDetaljerList(String... fnrs) {
//		List<IdentDetaljerTo> identDetaljerToList = new ArrayList<>();
//		for (String fnr : fnrs) {
//			identDetaljerToList.add(new IdentDetaljerTo(fnr, new Date()));
//		}
//		return identDetaljerToList; FIXME
//	}

	private Journalpost createNAVNOJournalpost(String avsenderMottakerId) {
		return JournalpostBuilder
				.getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.avsenderMottakerId(avsenderMottakerId)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
								.dokumentInfoId(DOKUMENT_INFO_ID).build()).build())
				.build();
	}

	private Journalpost createLegalJournalpost() {
//		return createJournalpost(DAY_AFTER_LEGAL_DATE, DAY_AFTER_LEGAL_DATE, null, false); FIXME
		return null;
	}

	private Journalpost createJournalpost(JournalStatusCode journalStatusCode) {
//		Journalpost journalpost = createJournalpost(DAY_AFTER_LEGAL_DATE, DAY_AFTER_LEGAL_DATE, null, false);
//		journalpost.setJournalstatus(journalStatusCode);
//		return journalpost;
		return null;
	}

	private Journalpost createJournalpost(Date createdDate, Date journalDate, VariantFormatCode variantFormatCode, boolean innskrenketPartsinnsyn) {
		return JournalpostBuilder
				.getJournalpostBuilder()
//				.changeStamp(new ChangeStamp("test", createdDate, "test", DateUtil.createDate(2016, Calendar.JUNE, 30)))
				.journalpostId(JOURNALPOST_ID)
				.journalDato(journalDate)
				.journalStatus(JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.I)
				.avsenderMottakerId(USER_ID)
				.saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder().build())
				.dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
						.dokumentInfo(createDokumentInfo(variantFormatCode, innskrenketPartsinnsyn))
						.build())
				.build();
	}

	private DokumentInfo createDokumentInfo(VariantFormatCode variantFormatCode, boolean innskrenketPartsinnsyn) {
		return DokumentInfoBuilder.getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
						.variantFormat(variantFormatCode == null ? VariantFormatCode.ARKIV : variantFormatCode)
						.build())
				.dokumentInfoId(DOKUMENT_INFO_ID)
				.kategori(DokumentKategoriCode.FORVALTNINGSNOTAT)
				.organInternt(innskrenketPartsinnsyn)
				.innskrenketPartsinnsyn(innskrenketPartsinnsyn)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.build();
	}

	private void mockJournalpost(Journalpost journalpost) throws NoJournalpostFoundException, DocumentNotFoundException {
//		when(hentJournalpost.hentJournalpost(eq(new HentJournalpostRequest(JOURNALPOST_ID))))
//				.thenReturn(new HentJournalpostResponse(journalpost));
//		when(hentDokumentService.hentDokument(eq(new HentDokumentRequestTo(JOURNALPOST_ID, DOKUMENT_INFO_ID, VariantFormatCode.ARKIV))))
//				.thenReturn(DOK);
	}
}
