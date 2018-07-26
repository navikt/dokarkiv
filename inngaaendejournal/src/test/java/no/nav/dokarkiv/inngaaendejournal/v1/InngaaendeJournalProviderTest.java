package no.nav.dokarkiv.inngaaendejournal.v1;

import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalProvider.HENT_JOURNALPOST;
import static no.nav.dokarkiv.inngaaendejournal.v1.InngaaendeJournalProvider.UTLED_JOURNALFOERINGSBEHOV;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.core.security.abac.JdbcAbacSecurityRepository;
import no.nav.dokarkiv.inngaaendejournal.v1.common.DokumentInformasjonManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalfoeringsbehovTo;
import no.nav.dokarkiv.inngaaendejournal.v1.common.JournalpostManglerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.JournalpostKanIkkeBehandlesException;
import no.nav.dokarkiv.inngaaendejournal.v1.exceptions.UgyldigInputException;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.HentInngaaendeJournalpostService;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.AktoerTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.ArkivSakTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentInnholdTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumentinformasjonTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.DokumenttilstandTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.InngaaendeJournalpostTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to.JournaltilstandTo;
import no.nav.dokarkiv.inngaaendejournal.v1.tjoark057.UtledJournalfoeringsbehovService;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.feil.ForretningsmessigUnntak;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Aktoer;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Dokumenttilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journalfoeringsbehov;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.JournalpostMangler;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Journaltilstand;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import org.joda.time.LocalDateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@RunWith(MockitoJUnitRunner.class)
public class InngaaendeJournalProviderTest {

	public static final String AVSENDER_MOTTAKERID = "***gammelt_fnr***";
	public static final Long DOKUMENT_INFO_ID = 1L;
	public static final String ARKIV_SAKID = "1";
	public static final String INNHOLD = "Mitt innhold";
	public static final String ERRORMSG = "errormsg";
	static final LocalDateTime NOW = LocalDateTime.now();
	static final String FNR = "***gammelt_fnr***";
	static final String ORGNR = "999999999";
	static final String DOKUMENTTYPE_ID = "I00008";
	static final Long DOKUMENT_INFO_ID_VEDLEGG = 2L;
	static final String DOKUMENTTYPE_ID_VEDLEGG = "I00024";
	private static final String HENT_JOURNALPOST_OPERATION_NAME = "JOARK:" + HENT_JOURNALPOST;
	private static final String UTLED_JOURNALFOERINGSBEHOV_OPERATION_NAME = "JOARK:" + UTLED_JOURNALFOERINGSBEHOV;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	private UtledJournalfoeringsbehovRequest utledJournalfoeringsbehovRequest;
	private HentJournalpostRequest hentJournalpostRequest;
	@Mock
	private HentInngaaendeJournalpostService hentInngaaendeJournalpostService;
	@Mock
	private UtledJournalfoeringsbehovService utledJournalfoeringsbehovService;
	@Mock
	private AbacService abacServiceMock;

	@Mock
	private AbacContext abacContextMock;

	@Mock
	private XacmlRequest xacmlRequestMock;

	@Mock
	private XacmlResponse xacmlResponseMock;

	@Mock
	private AbacSecurityService abacSecurityServiceMock;

	@InjectMocks
	private InngaaendeJournalProvider provider;

	@Mock
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;

	@Test
	public void HentJournalpostShouldHandleDenyAccessFromAbac() throws Exception {
		hentJournalpostRequest = defaultHentJournalpostRequest();
		doThrow(new AuthorizationException("Access Denied")).when(abacSecurityServiceMock)
				.assertAccessToJournalpost(hentJournalpostRequest.getJournalpostId());

		expectedException.expect(HentJournalpostSikkerhetsbegrensning.class);
		expectedException.expectMessage("Access Denied");

		provider.hentJournalpost(hentJournalpostRequest);
	}

	@Test
	public void utledJournalfoeringsbehovShouldHandleDenyAccessFromAbac() throws Exception {
		utledJournalfoeringsbehovRequest = defaultUtledJournalfoeringsbehovRequest();
		doThrow(new AuthorizationException("Access Denied")).when(abacSecurityServiceMock)
				.assertAccessToJournalpost(utledJournalfoeringsbehovRequest.getJournalpostId());

		expectedException.expect(UtledJournalfoeringsbehovSikkerhetsbegrensning.class);
		expectedException.expectMessage("Access Denied");

		provider.utledJournalfoeringsbehov(utledJournalfoeringsbehovRequest);
	}

	@Test
	public void should_hentJournalpost() throws Exception {
		when(hentInngaaendeJournalpostService.hentJournalpost(any(String.class))).thenReturn(buildInngaaendeJournalpostTo());

		HentJournalpostResponse response = provider.hentJournalpost(defaultHentJournalpostRequest());
		InngaaendeJournalpost inngaaendeJournalpost = response.getInngaaendeJournalpost();

		assertThat(inngaaendeJournalpost.getAvsenderId(), is(AVSENDER_MOTTAKERID));
		assertThat(inngaaendeJournalpost.getForsendelseMottatt(), notNullValue());
		assertThat(inngaaendeJournalpost.getMottakskanal().getValue(), is(NAV_NO.name()));
		assertThat(inngaaendeJournalpost.getTema().getValue(), is(FagomradeCode.FOR.name()));
		assertThat(inngaaendeJournalpost.getJournaltilstand(), is(Journaltilstand.ENDELIG));
		assertThat(inngaaendeJournalpost.getArkivSak().getArkivSakId(), is(ARKIV_SAKID));
		assertThat(inngaaendeJournalpost.getArkivSak().getArkivSakSystem(), is(FagsystemCode.PEN.name()));

		assertThat(inngaaendeJournalpost.getBrukerListe(), hasSize(2));
		List<Aktoer> listeBrukere = inngaaendeJournalpost.getBrukerListe();
		assertThat((Person) listeBrukere.get(0), isA(Person.class));
		assertThat(((Person) listeBrukere.get(0)).getIdent(), is(FNR));
		assertThat((Organisasjon) listeBrukere.get(1), isA(Organisasjon.class));
		assertThat(((Organisasjon) listeBrukere.get(1)).getOrganisasjonsnummer(), is(ORGNR));

		assertThat(inngaaendeJournalpost.getHoveddokument()
				.getDokumentkategori()
				.getValue(), is(DokumentKategoriCode.ES.name()));
		assertThat(inngaaendeJournalpost.getHoveddokument().getDokumenttypeId().getValue(), is(DOKUMENTTYPE_ID));
		assertThat(inngaaendeJournalpost.getHoveddokument().getDokumentId(), is(DOKUMENT_INFO_ID.toString()));
		assertThat(inngaaendeJournalpost.getHoveddokument().getDokumenttilstand(), is(Dokumenttilstand.FERDIGSTILT));
		assertThat(inngaaendeJournalpost.getHoveddokument()
				.getDokumentInnholdListe()
				.get(0)
				.getArkivfiltype()
				.getValue(), is(FilTypeCode.PDFA.name()));
		assertThat(inngaaendeJournalpost.getHoveddokument()
				.getDokumentInnholdListe()
				.get(0)
				.getVariantformat()
				.getValue(), is(VariantFormatCode.ARKIV.name()));
		assertThat(inngaaendeJournalpost.getVedleggListe()
				.get(0)
				.getDokumentkategori()
				.getValue(), is(DokumentKategoriCode.ES.name()));
		assertThat(inngaaendeJournalpost.getVedleggListe().get(0).getDokumenttypeId().getValue(), is(DOKUMENTTYPE_ID_VEDLEGG));
		assertThat(inngaaendeJournalpost.getVedleggListe().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG.toString()));
		assertThat(inngaaendeJournalpost.getVedleggListe().get(0).getDokumenttilstand(), is(Dokumenttilstand.FERDIGSTILT));
		assertThat(inngaaendeJournalpost.getVedleggListe()
				.get(0)
				.getDokumentInnholdListe()
				.get(0)
				.getArkivfiltype()
				.getValue(), is(FilTypeCode.PDF.name()));
		assertThat(inngaaendeJournalpost.getVedleggListe()
				.get(0)
				.getDokumentInnholdListe()
				.get(0)
				.getVariantformat()
				.getValue(), is(VariantFormatCode.ARKIV.name()));
	}

	@Test
	public void should_throw_HentJournalpostUgyldigInput_when_UgyldigInputException_is_caught() throws Exception {
		UgyldigInputException cause = new UgyldigInputException(ERRORMSG);
		when(hentInngaaendeJournalpostService.hentJournalpost(any(String.class))).thenThrow(cause);

		try {
			provider.hentJournalpost(defaultHentJournalpostRequest());
			fail();
		} catch (HentJournalpostUgyldigInput e) {
			assertHentJournalpostFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_HentJournalpostUgyldigInput_when_JournalpostId_not_provided() throws Exception {
		UgyldigInputException cause = new UgyldigInputException(ERRORMSG);
		doThrow(cause).when(hentInngaaendeJournalpostService).assertJournalpostIdIsNotNull(Mockito.<String>any());

		try {
			HentJournalpostRequest hentJournalpostRequest = defaultHentJournalpostRequest();
			hentJournalpostRequest.setJournalpostId(null);
			provider.hentJournalpost(hentJournalpostRequest);
			fail();
		} catch (HentJournalpostUgyldigInput e) {
			assertHentJournalpostFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_HentJournalpostJournalpostIkkeFunnet_when_JournalpostIkkeFunnetException_is_caught() throws Exception {
		JournalpostIkkeFunnetException cause = new JournalpostIkkeFunnetException(ERRORMSG);
		when(hentInngaaendeJournalpostService.hentJournalpost(any(String.class))).thenThrow(cause);
		try {
			provider.hentJournalpost(defaultHentJournalpostRequest());
			fail();
		} catch (HentJournalpostJournalpostIkkeFunnet e) {
			assertHentJournalpostFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_HentJournalpostJournalpostIkkeInngaaende_when_JournalpostIkkeInngaaendeException_is_caught() throws Exception {
		JournalpostIkkeInngaaendeException cause = new JournalpostIkkeInngaaendeException(ERRORMSG);
		when(hentInngaaendeJournalpostService.hentJournalpost(any(String.class))).thenThrow(cause);
		try {
			provider.hentJournalpost(defaultHentJournalpostRequest());
			fail();
		} catch (HentJournalpostJournalpostIkkeInngaaende e) {
			assertHentJournalpostFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_utledeJournalfoeringsbehov() throws Exception {
		when(utledJournalfoeringsbehovService.utledJournalfoeringsbehov(any(String.class))).thenReturn(buildJournalpostManglerTo());

		UtledJournalfoeringsbehovResponse response = provider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest());
		JournalpostMangler journalpostMangler = response.getJournalfoeringsbehov();
		assertThat(journalpostMangler.getAvsenderId(), is(Journalfoeringsbehov.MANGLER_IKKE));
		assertThat(journalpostMangler.getAvsenderNavn(), is(Journalfoeringsbehov.MANGLER));
		assertThat(journalpostMangler.getArkivSak(), is(Journalfoeringsbehov.MANGLER_IKKE));
		assertThat(journalpostMangler.getInnhold(), is(Journalfoeringsbehov.MANGLER_IKKE));
		assertThat(journalpostMangler.getTema(), is(Journalfoeringsbehov.MANGLER));
		assertThat(journalpostMangler.getBruker(), is(Journalfoeringsbehov.MANGLER_IKKE));
		assertThat(journalpostMangler.getHoveddokument().getDokumentId(), is(DOKUMENT_INFO_ID.toString()));
		assertThat(journalpostMangler.getHoveddokument().getDokumentkategori(), is(Journalfoeringsbehov.MANGLER));
		assertThat(journalpostMangler.getHoveddokument().getTittel(), is(Journalfoeringsbehov.MANGLER_IKKE));
		assertThat(journalpostMangler.getVedleggListe().get(0).getDokumentId(), is(DOKUMENT_INFO_ID_VEDLEGG.toString()));
		assertThat(journalpostMangler.getVedleggListe().get(0).getDokumentkategori(), is(Journalfoeringsbehov.MANGLER));
		assertThat(journalpostMangler.getVedleggListe().get(0).getTittel(), is(Journalfoeringsbehov.MANGLER_IKKE));
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovUgyldigInput_when_UgyldigInputException_is_caught() throws Exception {
		UgyldigInputException cause = new UgyldigInputException(ERRORMSG);
		when(utledJournalfoeringsbehovService.utledJournalfoeringsbehov(any(String.class))).thenThrow(cause);

		try {
			provider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest());
			fail();
		} catch (UtledJournalfoeringsbehovUgyldigInput e) {
			assertUtledJournalfoeringsbehovFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostIkkeFunnet_when_JournalpostIkkeFunnetException_is_caught() throws Exception {
		JournalpostIkkeFunnetException cause = new JournalpostIkkeFunnetException(ERRORMSG);
		when(utledJournalfoeringsbehovService.utledJournalfoeringsbehov(any(String.class))).thenThrow(cause);
		try {
			provider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest());
			fail();
		} catch (UtledJournalfoeringsbehovJournalpostIkkeFunnet e) {
			assertUtledJournalfoeringsbehovFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostIkkeInngaaende_when_JournalpostIkkeInngaaendeException_is_caught() throws Exception {
		JournalpostIkkeInngaaendeException cause = new JournalpostIkkeInngaaendeException(ERRORMSG);
		when(utledJournalfoeringsbehovService.utledJournalfoeringsbehov(any(String.class))).thenThrow(cause);
		try {
			provider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest());
			fail();
		} catch (UtledJournalfoeringsbehovJournalpostIkkeInngaaende e) {
			assertUtledJournalfoeringsbehovFaultInfo(e.getFaultInfo(), cause);
		}
	}

	@Test
	public void should_throw_UtledJournalfoeringsbehovJournalpostKanIkkeBehandles_when_JournalpostKanIkkeBehandlesException_is_caught() throws Exception {
		JournalpostKanIkkeBehandlesException cause = new JournalpostKanIkkeBehandlesException(ERRORMSG);
		when(utledJournalfoeringsbehovService.utledJournalfoeringsbehov(any(String.class))).thenThrow(cause);
		try {
			provider.utledJournalfoeringsbehov(defaultUtledJournalfoeringsbehovRequest());
			fail();
		} catch (UtledJournalfoeringsbehovJournalpostKanIkkeBehandles e) {
			assertUtledJournalfoeringsbehovFaultInfo(e.getFaultInfo(), cause);
		}
	}

	private void assertHentJournalpostFaultInfo(ForretningsmessigUnntak faultInfo, Throwable expectedFeilaarsak) {
		assertFaultInfo(faultInfo, expectedFeilaarsak, HENT_JOURNALPOST_OPERATION_NAME);
	}

	private void assertUtledJournalfoeringsbehovFaultInfo(ForretningsmessigUnntak faultInfo, Throwable expectedFeilaarsak) {
		assertFaultInfo(faultInfo, expectedFeilaarsak, UTLED_JOURNALFOERINGSBEHOV_OPERATION_NAME);
	}

	private void assertFaultInfo(ForretningsmessigUnntak faultInfo, Throwable expectedFeilaarsak, String operationName) {
		assertThat(faultInfo.getFeilaarsak(), containsString(expectedFeilaarsak.toString()));
		assertThat(faultInfo.getFeilmelding(), is(ERRORMSG));
		assertThat(faultInfo.getTidspunkt(), notNullValue());
		assertThat(faultInfo.getFeilkilde(), is(operationName));
	}

	private HentJournalpostRequest defaultHentJournalpostRequest() {
		hentJournalpostRequest = new HentJournalpostRequest();
		hentJournalpostRequest.setJournalpostId("1");
		return hentJournalpostRequest;
	}

	private UtledJournalfoeringsbehovRequest defaultUtledJournalfoeringsbehovRequest() {
		utledJournalfoeringsbehovRequest = new UtledJournalfoeringsbehovRequest();
		utledJournalfoeringsbehovRequest.setJournalpostId("1");
		return utledJournalfoeringsbehovRequest;
	}

	private InngaaendeJournalpostTo buildInngaaendeJournalpostTo() {
		return InngaaendeJournalpostTo.builder()
				.avsenderId(AVSENDER_MOTTAKERID)
				.forsendelseMottatt(NOW)
				.mottakskanal(NAV_NO)
				.tema(FagomradeCode.FOR)
				.journaltilstand(JournaltilstandTo.ENDELIG)
				.arkivSak(ArkivSakTo.builder()
						.arkivSakId(ARKIV_SAKID)
						.fagsystem(FagsystemCode.PEN)
						.build())
				.brukere(Arrays.asList(
						AktoerTo.builder()
								.aktoerId(FNR)
								.aktoerType(BrukerTypeCode.PERSON)
								.build(),
						AktoerTo.builder()
								.aktoerId(ORGNR)
								.aktoerType(BrukerTypeCode.ORGANISASJON)
								.build()
						)
				)
				.hoveddokument(DokumentinformasjonTo.builder()
						.dokumentkategori(DokumentKategoriCode.ES)
						.dokumenttypeId(DOKUMENTTYPE_ID)
						.dokumentId(DOKUMENT_INFO_ID)
						.dokumenttilstand(DokumenttilstandTo.FERDIGSTILT)
						.dokumentInnhold(Collections.singletonList(DokumentInnholdTo.builder()
								.arkivFiltype(FilTypeCode.PDFA)
								.variantFormat(VariantFormatCode.ARKIV)
								.build()))
						.build())
				.vedlegg(Collections.singletonList(DokumentinformasjonTo.builder()
						.dokumentkategori(DokumentKategoriCode.ES)
						.dokumenttypeId(DOKUMENTTYPE_ID_VEDLEGG)
						.dokumentId(DOKUMENT_INFO_ID_VEDLEGG)
						.dokumenttilstand(DokumenttilstandTo.FERDIGSTILT)
						.dokumentInnhold(Collections.singletonList(DokumentInnholdTo.builder()
								.arkivFiltype(FilTypeCode.PDF)
								.variantFormat(VariantFormatCode.ARKIV)
								.build()))
						.build()))
				.build();
	}

	private JournalpostManglerTo buildJournalpostManglerTo() {
		return JournalpostManglerTo.builder()
				.avsenderId(JournalfoeringsbehovTo.MANGLER_IKKE)
				.avsenderNavn(JournalfoeringsbehovTo.MANGLER)
				.arkivSak(JournalfoeringsbehovTo.MANGLER_IKKE)
				.innhold(JournalfoeringsbehovTo.MANGLER_IKKE)
				.tema(JournalfoeringsbehovTo.MANGLER)
				.bruker(JournalfoeringsbehovTo.MANGLER_IKKE)
				.hoveddokument(DokumentInformasjonManglerTo.builder()
						.dokumentId(DOKUMENT_INFO_ID)
						.dokumentKategori(JournalfoeringsbehovTo.MANGLER)
						.tittel(JournalfoeringsbehovTo.MANGLER_IKKE)
						.build())
				.vedlegg(Collections.singletonList(DokumentInformasjonManglerTo.builder()
						.dokumentId(DOKUMENT_INFO_ID_VEDLEGG)
						.dokumentKategori(JournalfoeringsbehovTo.MANGLER)
						.tittel(JournalfoeringsbehovTo.MANGLER_IKKE)
						.build()))
				.build();
	}

}