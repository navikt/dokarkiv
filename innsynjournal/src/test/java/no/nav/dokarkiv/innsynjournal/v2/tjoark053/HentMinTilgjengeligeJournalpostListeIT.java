package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider;
import no.nav.dokarkiv.core.datautil.SkannetInnholdTestDataProvider;
import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.jaxws.SubjectHandlerUtils;
import no.nav.dokarkiv.core.jaxws.ThreadLocalSubjectHandler;
import no.nav.dokarkiv.core.util.DateConverterUtil;
import no.nav.dokarkiv.innsynjournal.v2.AbstractInnsynJournalV2Itest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Sak;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.SkannetInnhold;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.transaction.TestTransaction;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static no.nav.dokarkiv.core.consumer.pdl.AktoerConsumerV2Mock.CURRENT_IDENT;
import static no.nav.dokarkiv.core.consumer.pdl.AktoerConsumerV2Mock.FAIL_IDENT;
import static no.nav.dokarkiv.core.consumer.pdl.AktoerConsumerV2Mock.HISTORICAL_IDENTS;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID;
import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID_SLADDET;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.DOKUMENT_TITTEL;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createDokumentInfo;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.FIL_TYPE;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.VARIANT_FORMAT;
import static no.nav.dokarkiv.core.datautil.FildetaljerTestDataProvider.createFildetaljer;
import static no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon;
import static no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider.createVedleggRelasjon;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.FNR;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JANUARY_1_2020;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JP_AVSENDER_MOTTAKER;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JP_FAGOMRADE;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.JP_TYPE;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpost;
import static no.nav.dokarkiv.core.datautil.JournalpostTestDataProvider.createJournalpostWithoutHoveddokument;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.SAK_FAGSYSTEM;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.SAK_ID;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.B;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.ELEKTRONISK_DIALOG;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.ES;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.FORVALTNINGSNOTAT;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.IB;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.IS;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.KD;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.KS;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.PUBL_BLANKETT_EOS;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.REFERAT;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.SED;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.TS;
import static no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.VB;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.KTR;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.A;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.OD;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.R;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.U;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.UB;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.ALTINN;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.SKAN_NETS;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.SKAN_PEN;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.HOVEDDOKUMENT;
import static no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode.VEDLEGG;
import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.JA;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.KAN_IKKE_AVGJOERES;
import static no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.InnsynDokument.NEI;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Integration test for TJOARK053 HentMinTilgjengeligeJournalpostListe.
 *
 * @author Ketill Fenne, Visma Consulting AS
 */
public class HentMinTilgjengeligeJournalpostListeIT extends AbstractInnsynJournalV2Itest {

	@BeforeClass
	public static void setUpSecurity() {
		System.setProperty("no.nav.modig.security.systemuser.username", "JOARK");
		System.setProperty("no.nav.modig.core.context.subjectHandlerImplementationClass", ThreadLocalSubjectHandler.class.getName());
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");
	}

	@Before
	public void setUpSubjectHandler() {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
	}

	@After
	public void resetSubjectHandler() {
		SubjectHandlerUtils.reset();
	}

	/**
	 * Hvis journalpost.opprettetDato og journalpost.datoJournal er tidligere enn datoen i feltet {@code innsyn.earliest.date}
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereCreatedDateAndJournalDatoAreBothEarlierThan1stOfJanuary2016() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.changeStamp(createChangeStamp(2015, Month.DECEMBER, 31))
				.journalDato(createDate(2015, Month.DECEMBER, 31)));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis journalpost.opprettetDato er tidligere enn datoen i feltet {@code innsyn.earliest.date} og journalpost.datoJournal er {@code null}
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereCreatedDateIsEarlierThan1stJanuary2016AndJournalDatoIsNull() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.changeStamp(createChangeStamp(2015, Month.DECEMBER, 31))
				.journalDato(null));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis journalpost.journalpostStatus er noe annet enn &quot;J&quot;, &quot;FS&quot;, &quot;FL&quot; eller &quot;E&quot;
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereStatusIsNotJOrFSOrFLOrE() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost().journalStatus(A));
		buildAndPersist(aJournalpost().journalStatus(D));
		buildAndPersist(aJournalpost().journalStatus(M));
		buildAndPersist(aJournalpost().journalStatus(MO));
		buildAndPersist(aJournalpost().journalStatus(OD));
		buildAndPersist(aJournalpost().journalStatus(R));
		buildAndPersist(aJournalpost().journalStatus(U));
		buildAndPersist(aJournalpost().journalStatus(UB));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis saksrelasjon.feilregistrert er lik {@code true}
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereSaksrelasjonIsFeilregistrert() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.saksrelasjon(SaksrelasjonTestDataProvider.createSaksrelasjon()
						.feilregistrert(true).build())
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis journalpost.fagomraade er lik {@link FagomradeCode#KTR}
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereFagomraadeIsKTR() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost().fagomrade(KTR));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis journalpostType er lik &quot;N&quot; og hoveddokumentets kategori er noe annet enn &quot;FORVALTNINGSNOTAT&quot;
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhereTypeIsNAndHoveddokumentKategoriIsNotFORVALTNINGSNOTAT() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithHoveddokumentKategori(null).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(B).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(ELEKTRONISK_DIALOG).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(ES).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(IB).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(IS).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(KD).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(KS).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(PUBL_BLANKETT_EOS).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(REFERAT).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(SED).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(TS).journalpostType(N));
		buildAndPersist(aJournalpostWithHoveddokumentKategori(VB).journalpostType(N));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis journalpostType er lik &quot;N&quot; og et vedleggs kategori er noe annet enn &quot;FORVALTNINGSNOTAT&quot;
	 * s&aring; skal vedlegget ikke returneres som en del av resultatet.
	 */
	@Test
	public void shouldNotIncludeDokumentInfoOnJournalpostWhenJournalpostTypeIsNAndKategoriIsNotFORVALTNINGSNOTAT() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.journalpostType(N)
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon(createDokumentInfo(FORVALTNINGSNOTAT).build()).build(),
						createVedleggRelasjon(createDokumentInfo(null).build()).build(),
						createVedleggRelasjon(createDokumentInfo(B).build()).build(),
						createVedleggRelasjon(createDokumentInfo(ELEKTRONISK_DIALOG).build()).build(),
						createVedleggRelasjon(createDokumentInfo(ES).build()).build(),
						createVedleggRelasjon(createDokumentInfo(IB).build()).build(),
						createVedleggRelasjon(createDokumentInfo(IS).build()).build(),
						createVedleggRelasjon(createDokumentInfo(KD).build()).build(),
						createVedleggRelasjon(createDokumentInfo(KS).build()).build(),
						createVedleggRelasjon(createDokumentInfo(PUBL_BLANKETT_EOS).build()).build(),
						createVedleggRelasjon(createDokumentInfo(REFERAT).build()).build(),
						createVedleggRelasjon(createDokumentInfo(SED).build()).build(),
						createVedleggRelasjon(createDokumentInfo(TS).build()).build(),
						createVedleggRelasjon(createDokumentInfo(VB).build()).build()
				));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThatJournalpostOnlyHasHoveddokument(response.getJournalpostListe().get(0));
	}

	/**
	 * Hvis journalposts hoveddokument er organinternt
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhenHoveddokumentIsOrganinternt() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon(
								createDokumentInfo().organInternt(true).build()).build())
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * Hvis et journalpostvedlegg er organinternt
	 * s&aring; skal vedlegget ikke returneres som en del av resultatet.
	 */
	@Test
	public void shouldNotIncludeOrganinterntVedleggOnJournalpost() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(createDokumentInfo().organInternt(true).build()).build(),
						createVedleggRelasjon(createDokumentInfo().organInternt(true).build()).build())
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(1));
		assertThatJournalpostOnlyHasHoveddokument(response.getJournalpostListe().get(0));
	}

	/**
	 * # 10: Hvis journalposttype er lik &quot;U&quot; eller &quot;N&quot;, og hoveddokumentstatus ikke er &quot;FERDIGSTILT&quot;
	 * s&aring; skal journalpost ikke returneres.
	 */
	@Test
	public void shouldNotReturnJournalpostWhenTypeIsUOrNAndHoveddokumentIsFerdigstilt() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.journalpostType(JournalpostTypeCode.N)
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon(
								createDokumentInfo().dokumentstatus(UNDER_REDIGERING).build()).build())
		);
		Journalpost utgaaendeJournalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.journalpostType(JournalpostTypeCode.U)
				.dokumentInfoRelasjoner(
						createHoveddokumentRelasjon(
								createDokumentInfo().dokumentstatus(AVBRUTT).build()).build())
		);
		String sakId = utgaaendeJournalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, journalpost.getSaksrelasjon().getSakId(), sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertEmptyJournalpostListeIn(response);
	}

	/**
	 * # 11: Hvis journalposttype er lik &quot;U&quot; eller &quot;N&quot;, og et journalpostvedlegg ikke er ferdigstilt
	 * s&aring; skal vedlegget ikke returneres som en del av resultatet.
	 */
	@Test
	public void shouldNotIncludeFerdigstiltVedleggOnJournalpostWhenTypeIsUOrN() throws Exception {
		Journalpost utgaaendeJournalpost = buildAndPersist(aJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(createDokumentInfo().dokumentstatus(AVBRUTT).build()).build(),
						createVedleggRelasjon(createDokumentInfo().dokumentstatus(UNDER_REDIGERING).build()).build())
		);

		buildAndPersist(aJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(createDokumentInfo().dokumentstatus(AVBRUTT).build()).build(),
						createVedleggRelasjon(createDokumentInfo().dokumentstatus(AVBRUTT).build()).build())
		);

		String sakId = utgaaendeJournalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		assertThat(response.getJournalpostListe(), hasSize(2));
		assertThatJournalpostOnlyHasHoveddokument(response.getJournalpostListe().get(0));
		assertThatJournalpostOnlyHasHoveddokument(response.getJournalpostListe().get(1));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code false}
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} ikke settes.
	 */
	@Test
	public void shouldNotSetDokumentInnsynWhenMerkInnsynDokumentIsFalse() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(createDokumentInfo().build()).build(),
						createVedleggRelasjon(createDokumentInfo().build()).build())
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is((InnsynDokument) null));
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(1), is((InnsynDokument) null));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.avsenderMottakerId er lik eksternbruker
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#JA}.
	 */
	@Test
	public void shouldSetDokumentInnsynToJAWhenMottakskanalIsNAVAndAvsenderMottakerIdIsEksternBruker() throws Exception {
		SubjectHandlerUtils.setEksternBruker(FNR, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost().mottakskanal(NAV_NO));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(JA));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.avsenderMottakerId er ulik eksternbruker
	 * og journalpost.avsenderMottakerId finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#JA}.
	 */
	@Test
	public void shouldSetDokumentInnsynToJAWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternBrukerAndInAktoerId() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.avsenderMottakerId(HISTORICAL_IDENTS.get(0)));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(JA));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.avsenderMottakerId er ulik eksternbruker
	 * og journalpost.avsenderMottakerId ikke finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternBrukerAndNotInAktoerId() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.avsenderMottakerId("13333333337"));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(NEI));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.avsenderMottakerId er lik eksternbruker og AktoerId feiler
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#KAN_IKKE_AVGJOERES}.
	 */
	@Test
	public void shouldSetDokumentInnsynToKANIKKEAVGJOERESWhenMottakskanalIsNAVAndAvsenderMottakerIdIsNotEksternAndAktoerIdFeiler() throws Exception {
		SubjectHandlerUtils.setEksternBruker(FAIL_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost().mottakskanal(NAV_NO));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(KAN_IKKE_AVGJOERES));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.journalpostType er ulik {@link JournalpostTypeCode#N}
	 * og journalpost.avsenderMottakerId er ulik eksternbruker og journalpost.avsenderMottakerId ikke finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenJournalpostTypeIsNotNAndAvsenderMottakerIdIsNotEksternBrukerAndNotInAktoerId() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.mottakskanal(ALTINN)
				.journalpostType(I)
				.avsenderMottakerId(FNR));
		buildAndPersist(aJournalpost()
				.mottakskanal(ALTINN)
				.journalpostType(JournalpostTypeCode.U)
				.avsenderMottakerId(FNR));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner1 = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		List<DokumentinfoRelasjon> dokumentinfoRelasjoner2 = response.getJournalpostListe().get(1).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner1.get(0), is(NEI));
		assertDokumentInnsyn(dokumentinfoRelasjoner2.get(0), is(NEI));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.journalpostType er ulik {@link JournalpostTypeCode#N}
	 * og journalpost.avsenderMottakerId er ulik eksternbruker og journalpost.avsenderMottakerId ikke finnes i listen som er returnert fra AktoerId
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenMottakskanalIsSKAN_NETSOrSKAN_PEN() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.mottakskanal(SKAN_NETS)
				.journalpostType(I)
				.avsenderMottakerId(FNR));
		buildAndPersist(aJournalpost()
				.mottakskanal(SKAN_PEN)
				.journalpostType(JournalpostTypeCode.U)
				.avsenderMottakerId(FNR));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner1 = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		List<DokumentinfoRelasjon> dokumentinfoRelasjoner2 = response.getJournalpostListe().get(1).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner1.get(0), is(NEI));
		assertDokumentInnsyn(dokumentinfoRelasjoner2.get(0), is(NEI));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og dokumentInfo.fildetaljer.onDemandId er noe annet enn {@code null}
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenFildetaljerOnDemandIdIsNotNull() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(createDokumentInfo(DOKUMENT_TITTEL, createFildetaljer(FIL_UUID, ""), FIL_UUID_SLADDET).build()).build(),
						createVedleggRelasjon(createDokumentInfo(DOKUMENT_TITTEL, createFildetaljer(FIL_UUID, "1234"), FIL_UUID_SLADDET).build()).build()
				)
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(NEI));
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(1), is(NEI));
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(2), is(NEI));
	}

	/**
	 * Hvis inputparameter {@code merkInnsynDokument} er lik {@code true} og journalpost.dokumentInfo.innskrenketPartsinnsyn er lik {@code true}
	 * s&aring; skal {@code Dokumentbeskrivelse.innsynDokument} settes til {@link InnsynDokument#NEI}.
	 */
	@Test
	public void shouldSetDokumentInnsynToNEIWhenInnskrenketPartsinnsynIsTrue() throws Exception {
		SubjectHandlerUtils.setEksternBruker(CURRENT_IDENT, 4, "");
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.mottakskanal(NAV_NO)
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(
								createDokumentInfo().innskrenketPartsinnsyn(true).build()).build()));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> dokumentinfoRelasjoner = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(0), is(NEI));
		assertDokumentInnsyn(dokumentinfoRelasjoner.get(1), is(NEI));
	}

	/**
	 * Hvis {@code journalpost.ekspedertDato} er ulik {@code null}
	 * s&aring; skal {@code Journalpost.sendt} settes lik {@code journalpost.ekspedertDato}.
	 */
	@Test
	public void shouldSetSendtDatoToEkspedertDatoWhenEkspedertDatoIsNotNull() throws Exception {
		Date ekspedertDato = Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
		Journalpost journalpost = buildAndPersist(aJournalpost().ekspedertDato(ekspedertDato));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		Date responseSendtDato = convertXMLGregorianCalendarToDate(response.getJournalpostListe().get(0).getSendt());
		assertThat(responseSendtDato, is(equalTo(ekspedertDato)));
	}

	/**
	 * Hvis {@code journalpost.ekspedertDato} er lik {@code null} og {@code journalpost.sendtPrintDato} er ulik {@code null}
	 * s&aring; skal {@code Journalpost.sendt} settes lik {@code journalpost.sendtPrintDato}.
	 */
	@Test
	public void shouldSetSendtDatoToSendtPrintDatoWhenEkspedertDatoIsNullAndSendtPrintDatoIsNotNull() throws Exception {
		Date sendtPrintDato = Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.ekspedertDato(null)
				.sendtPrintDato(sendtPrintDato)
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		Date responseSendtDato = convertXMLGregorianCalendarToDate(response.getJournalpostListe().get(0).getSendt());
		assertThat(responseSendtDato, is(equalTo(sendtPrintDato)));
	}

	/**
	 * Hvis {@code journalpost.ekspedertDato} og {@code journalpost.sendtPrintDato} begge er lik {@code null}
	 * s&aring; skal {@code Journalpost.sendt} settes lik {@code journalpost.journalDato}.
	 */
	@Test
	public void shouldSetSendtDatoToJournalDatoWhenEkspedertDatoAndSendtPrintDatoAreBothNull() throws Exception {
		Date journalDato = Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.ekspedertDato(null)
				.sendtPrintDato(null)
				.journalDato(journalDato)
		);

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		Date responseSendtDato = convertXMLGregorianCalendarToDate(response.getJournalpostListe().get(0).getSendt());
		assertThat(responseSendtDato, is(equalTo(journalDato)));
	}

	/**
	 * Hvis {@code journalpost.journalDato} er ulik {@code null}
	 * s&aring; skal {@code Journalpost.ferdigstilt} settes lik {@code journalpost.journalDato}.
	 */
	@Test
	public void shouldSetFerdigstiltToJournalDatoWhenJournalDatoIsNotNull() throws Exception {
		Date journalDato = Date.from(LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant());
		Journalpost journalpost = buildAndPersist(aJournalpost()
				.journalDato(journalDato));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		Date responseFerdigstilt = convertXMLGregorianCalendarToDate(response.getJournalpostListe().get(0).getFerdigstilt());
		assertThat(responseFerdigstilt, is(equalTo(journalDato)));
	}

	/**
	 * Hvis {@code journalpost} har relasjoner til flere {@code dokumentInfo}er,
	 * s&aring; skal hoveddokumentet komme f&oslash;rst i resultatlisten, etterfulgt av vedleggene, sortert stigende p&aring;
	 * {@code DokumentinfoRelasjon.dokumentinfoRelasjonId}.
	 */
	@Test
	public void shouldReturnHoveddokumentFirstAndSortVedleggAscendinglyByDokumentinfoRelasjonId() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(
								createDokumentInfo("Tidligste_vedlegg", FIL_UUID, FIL_UUID_SLADDET)
										.build()).build(),
						createHoveddokumentRelasjon(
								createDokumentInfo("Hoveddokument", FIL_UUID, FIL_UUID_SLADDET)
										.build()).build(),
						createVedleggRelasjon(
								createDokumentInfo("Seneste_vedlegg", FIL_UUID, FIL_UUID_SLADDET)
										.build()).build()
				));

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);
		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<DokumentinfoRelasjon> relasjonListe = response.getJournalpostListe().get(0).getDokumentinfoRelasjonListe();
		assertThat(relasjonListe, hasSize(3));
		assertThat(relasjonListe.get(0).getDokumentTilknyttetJournalpost().getValue(),
				is(TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name()));
		assertThat(relasjonListe.get(2).getDokumentinfoRelasjonId(),
				is(greaterThan(relasjonListe.get(1).getDokumentinfoRelasjonId())));
	}

	@Test
	public void happyPath() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpost());

		String sakId = journalpost.getSaksrelasjon().getSakId();

		HentTilgjengeligJournalpostListeRequest request = createRequest(false, sakId);

		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListe = response.getJournalpostListe();
		assertThat(journalpostListe, hasSize(1));
	}

	/**
	 * Assert that reponse from {@link no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2} contains values
	 *
	 * @throws Exception
	 */
	@Test
	public void shouldVerifyResponseValues() throws Exception {
		Journalpost journalpost = buildAndPersist(journalpostMaxResponse());
		String sakId = journalpost.getSaksrelasjon().getSakId();
		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);
		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListe = response.getJournalpostListe();
		assertThat(journalpostListe, hasSize(1));
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost responseJp = journalpostListe.get(0);

		XMLGregorianCalendar expectedMottattDato = DateConverterUtil.convertDateToXMLGregorianCalendar(JANUARY_1_2020);
		XMLGregorianCalendar expectedSendtDato = DateConverterUtil.convertDateToXMLGregorianCalendar(JANUARY_1_2020);

		assertThat(responseJp.getArkivtema().getValue(), is(JP_FAGOMRADE.name()));
		assertThat(responseJp.getEksternPart(), is(JP_AVSENDER_MOTTAKER));
		assertThat(responseJp.getMottatt(), is(expectedMottattDato));
		assertThat(responseJp.getSendt(), is(expectedSendtDato));
		assertThat(Long.valueOf(responseJp.getJournalpostId()), is(journalpost.getJournalpostId()));
		assertThat(responseJp.getKommunikasjonsretning().getValue(), is(JP_TYPE.name()));
		assertThat(responseJp.getOpprettet(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(journalpost.getChangeStamp().getCreatedDate())));

		assertSak(responseJp.getGjelderSak());
		assertDokumentInfoRels(responseJp.getDokumentinfoRelasjonListe());
	}

	@Test
	public void shouldVerifyResponseValuesKassert() throws Exception {
		Journalpost journalpost = buildAndPersist(journalpostMaxResponse());
		skjermingService.setDokumentKassert(journalpost.findHoveddokumentDokumentInfoRelasjon()
				.getDokumentInfo(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		String sakId = journalpost.getSaksrelasjon().getSakId();
		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);
		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListe = response.getJournalpostListe();
		assertThat(journalpostListe, hasSize(1));
		no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost responseJp = journalpostListe.get(0);

		XMLGregorianCalendar expectedMottattDato = DateConverterUtil.convertDateToXMLGregorianCalendar(JANUARY_1_2020);
		XMLGregorianCalendar expectedSendtDato = DateConverterUtil.convertDateToXMLGregorianCalendar(JANUARY_1_2020);

		assertThat(responseJp.getArkivtema().getValue(), is(JP_FAGOMRADE.name()));
		assertThat(responseJp.getEksternPart(), is(JP_AVSENDER_MOTTAKER));
		assertThat(responseJp.getMottatt(), is(expectedMottattDato));
		assertThat(responseJp.getSendt(), is(expectedSendtDato));
		assertThat(Long.valueOf(responseJp.getJournalpostId()), is(journalpost.getJournalpostId()));
		assertThat(responseJp.getKommunikasjonsretning().getValue(), is(JP_TYPE.name()));
		assertThat(responseJp.getOpprettet(), is(DateConverterUtil.convertDateToXMLGregorianCalendar(journalpost.getChangeStamp().getCreatedDate())));

		assertSak(responseJp.getGjelderSak());
		assertThat(responseJp.getDokumentinfoRelasjonListe().get(0).getJournalfoertDokument().getBeskriverInnhold().getVariantformat().getValue(), is("ARKIV"));
	}

	@Test
	public void shouldNotReturnJournalpostWhenSkjermet() throws Exception {

		Journalpost journalpost = buildAndPersist(journalpostMaxResponse());
		Journalpost journalpost2 = buildAndPersist(journalpostMaxResponse());


		String sakId = journalpost.getSaksrelasjon().getSakId();
		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);
		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListe = response.getJournalpostListe();
		assertThat(journalpostListe, hasSize(2));

		skjermingService.setJournalpostSkjerming(journalpost.getJournalpostId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentTilgjengeligJournalpostListeResponse responseAfter = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);
		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListeAfter = responseAfter.getJournalpostListe();
		assertThat(journalpostListeAfter, hasSize(1));
		assertThat(journalpostListeAfter.get(0).getJournalpostId(), is(journalpost2.getJournalpostId().toString()));
	}

	@Test
	public void shouldNotReturnJournalpostRelasjonWhenSkjermet() throws Exception {
		Journalpost journalpost = buildAndPersist(aJournalpostWithoutHoveddokument()
				.dokumentInfoRelasjoner(
						createVedleggRelasjon(
								createDokumentInfo("Tidligste_vedlegg", FIL_UUID, FIL_UUID_SLADDET)
										.build()).build(),
						createHoveddokumentRelasjon(
								createDokumentInfo("Hoveddokument", FIL_UUID, FIL_UUID_SLADDET)
										.build()).build()
				));
		String sakId = journalpost.getSaksrelasjon().getSakId();
		HentTilgjengeligJournalpostListeRequest request = createRequest(true, sakId);
		HentTilgjengeligJournalpostListeResponse response = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListe = response.getJournalpostListe();
		assertThat(journalpostListe, hasSize(1));
		assertThat(journalpostListe.get(0).getDokumentinfoRelasjonListe().size(), is(2));

		skjermingService.setJpDokInfoRelSkjerming(journalpost.findDokumentInfoRelasjonByTilknyttetJournalpostSom(VEDLEGG)
				.iterator()
				.next()
				.getJournalpostDokumentInfoRelasjonId(), SkjermingTypeCode.POL);
		TestTransaction.flagForCommit();
		TestTransaction.end();

		HentTilgjengeligJournalpostListeResponse responseAfter = innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);

		List<no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost> journalpostListeAfter = responseAfter.getJournalpostListe();
		assertThat(journalpostListeAfter, hasSize(1));
		assertThat(journalpostListeAfter.get(0).getDokumentinfoRelasjonListe().size(), is(1));
		assertThat(journalpostListeAfter.get(0)
				.getDokumentinfoRelasjonListe()
				.get(0)
				.getDokumentTilknyttetJournalpost()
				.getValue(), is("HOVEDDOKUMENT"));
	}

	private void assertSak(Sak gjelderSak) {
		assertThat(gjelderSak.getFagsystem().getValue(), is(SAK_FAGSYSTEM.name()));
		assertThat(gjelderSak.getSakId(), is(SAK_ID));
	}

	public void assertDokumentInfoRels(List<DokumentinfoRelasjon> dokumentinfoRelasjonListe) {
		assertThat(dokumentinfoRelasjonListe.size(), is(2));

		DokumentinfoRelasjon hoveddok = dokumentinfoRelasjonListe.get(0);
		DokumentinfoRelasjon vedlegg = dokumentinfoRelasjonListe.get(1);
		assertThat(hoveddok.getDokumentTilknyttetJournalpost().getValue(), is(HOVEDDOKUMENT.name()));
		assertThat(vedlegg.getDokumentTilknyttetJournalpost().getValue(), is(VEDLEGG.name()));
		assertDokument(hoveddok.getJournalfoertDokument());
		assertDokument(vedlegg.getJournalfoertDokument());
	}

	private void assertDokument(JournalfoertDokumentInfo journalfoertDokument) {
		assertThat(journalfoertDokument.getTittel(), is(DOKUMENT_TITTEL));
		DokumentInnhold beskriverInnhold = journalfoertDokument.getBeskriverInnhold();
		assertThat(beskriverInnhold.getVariantformat().getValue(), is(VARIANT_FORMAT.name()));
		assertThat(beskriverInnhold.getFiltype().getValue(), is(FIL_TYPE.name()));
		assertSkannetInnhold(journalfoertDokument.getSkannetInnholdListe());
	}

	private void assertSkannetInnhold(List<SkannetInnhold> skannetInnholds) {
		assertThat(skannetInnholds.size(), is(1));
		assertThat(skannetInnholds.get(0).getVedleggInnhold(), is(SkannetInnholdTestDataProvider.VEDLEGG_INNHOLD));
	}

	private void assertEmptyJournalpostListeIn(HentTilgjengeligJournalpostListeResponse response) {
		assertThat(response.getJournalpostListe(), hasSize(0));
	}

	private void assertThatJournalpostOnlyHasHoveddokument(no.nav.tjeneste.virksomhet.innsynjournal.v2.informasjon.Journalpost journalpost) {
		List<DokumentinfoRelasjon> relasjonsliste = journalpost.getDokumentinfoRelasjonListe();
		assertThat(relasjonsliste, hasSize(1));

		String tilknytningstype = relasjonsliste.get(0).getDokumentTilknyttetJournalpost().getValue();
		assertThat(tilknytningstype, is(equalTo(HOVEDDOKUMENT.name())));
	}

	private void assertDokumentInnsyn(DokumentinfoRelasjon dokumentinfoRelasjon, Matcher<InnsynDokument> matcher) {
		assertThat(dokumentinfoRelasjon.getJournalfoertDokument().getInnsynDokument(), matcher);
	}

	private JournalpostBuilder aJournalpost() {
		return createJournalpost(FIL_UUID)
				.avsenderMottakerId(FNR)
				.changeStamp(createChangeStamp(JANUARY_1_2020))
				.journalDato(JANUARY_1_2020);
	}

	private JournalpostBuilder journalpostMaxResponse() {
		return createJournalpost(FIL_UUID)
				.dokumentInfoRelasjoner(createVedleggRelasjon().dokumentInfo(createDokumentInfo().build())
						.build());
	}

	private JournalpostBuilder aJournalpostWithHoveddokumentFerdigDato(Date hoveddokumentFerdigDato) {
		return createJournalpost(DOKUMENT_TITTEL, FIL_UUID, hoveddokumentFerdigDato)
				.avsenderMottakerId(FNR)
				.changeStamp(createChangeStamp(JANUARY_1_2020))
				.journalDato(JANUARY_1_2020);
	}

	private JournalpostBuilder aJournalpostWithoutHoveddokument() {
		return createJournalpostWithoutHoveddokument()
				.avsenderMottakerId(FNR)
				.changeStamp(createChangeStamp(JANUARY_1_2020))
				.journalDato(JANUARY_1_2020);
	}

	private JournalpostBuilder aJournalpostWithHoveddokumentKategori(DokumentKategoriCode kategori) {
		return createJournalpost(kategori)
				.avsenderMottakerId(FNR)
				.changeStamp(createChangeStamp(JANUARY_1_2020))
				.journalDato(JANUARY_1_2020);
	}

	private HentTilgjengeligJournalpostListeRequest createRequest(boolean merkInnsynDokument, String... sakIdList) {
		HentTilgjengeligJournalpostListeRequest request = new HentTilgjengeligJournalpostListeRequest();

		request.setMerkInnsynDokument(merkInnsynDokument);

		for (String sakId : sakIdList) {
			request.getSakListe().add(createSak(sakId));
		}

		return request;
	}

	private Sak createSak(String sakId) {
		Sak sak = new Sak();
		sak.setSakId(sakId);

		Fagsystemer fagsystem = new Fagsystemer();
		fagsystem.setValue(FagsystemCode.FS22.name());

		sak.setFagsystem(fagsystem);

		return sak;
	}

	private ChangeStamp createChangeStamp(int createdYear, Month createdMonthOfYear, int createdDayOfMonth) {
		return createChangeStamp(createDate(createdYear, createdMonthOfYear, createdDayOfMonth));
	}

	private Date createDate(int createdYear, Month createdMonthOfYear, int createdDayOfMonth) {
		return Date.from(LocalDate.of(createdYear, createdMonthOfYear, createdDayOfMonth).atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	private ChangeStamp createChangeStamp(Date createdDate) {
		return new ChangeStamp("test", createdDate, null, null);
	}

	private Journalpost buildAndPersist(JournalpostBuilder journalpost) {
		return joarkRepository.save(journalpost.build());
	}
}
