package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.toXMLGregorianCalendar;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.addFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.addVedlegg;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.createJournalpost;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractArkiverDokumentmottakItest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.entities.Kryssreferanse;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalTilstandEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2IT extends AbstractArkiverDokumentmottakItest {

	private static final String REFERANSE_ID = "789";
	private static final String OPPLYSNINGSNOEKKEL1 = "noekkel1";
	private static final String OPPLYSNINGSVERDI1 = "verdi1";
	private static final String OPPLYSNINGSNOEKKEL2 = "noekkel2";
	private static final String OPPLYSNINGSVERDI2 = "verdi2";
	private static JournalTilstandEnum JOURNALTILSTAND_ENDELIG = JournalTilstandEnum.ENDELIG;
	private static JournalTilstandEnum JOURNALTILSTAND_MIDLERTIDIG = JournalTilstandEnum.MIDLERTIDIG;

	private JournalforInngaaendeForsendelseRequest request;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@After
	public void tearDown() throws Exception {
		entityManager.flush();
	}

	@Before
	public void setUp() throws Exception {
		Journalpost journalpostRequest = createJournalpost();
		journalpostRequest.getJournalpostDokumentInfoRelasjon().add(addVedlegg());
		RequestContextSetter.setRequestContextForUnitTest();
		request = new JournalforInngaaendeForsendelseRequest();
		request.setJournalpost(journalpostRequest);
		request.setForsokEndeligJF(true);
	}

	private List<no.nav.dokarkiv.core.domain.entities.Journalpost> getAllJournalposts() {
		return StreamSupport.stream(joarkRepository.findAll().spliterator(), false).collect(Collectors.toList());
	}

	/**
	 * HVIS journalpost med journalStatus "J" opprettes og input.journalFEnhet er satt SÅ skal Joark.journalFEnhet settes lik input.journalFEnhet
	 * HVIS ForsokEndeligJF=true så skal Journalpost.JournalStatus = "J"
	 * HVIS journalpost opprettes så skal DokumentInfo.OriginalJournalpostId være satt lik Journalpost.JournalpostId for alle dokumenter som er tilknyttet journalposten.
	 * HVIS journalpost opprettes SÅ skal filstorrelse settes for alle Fildetaljer
	 * HVIS journalpost opprettes SÅ skal følgende attributter hardkodes for Journalposten: journalpostType = "I"
	 * HVIS journalpost opprettes SÅ skal alle attributtene som er med i input lagres på journalposten
	 * HVIS input inneholder nok informasjon til å endelig journalføre forsendelsen, så blir Journalpost.JournalStatus = "J" og journalTilstand "ENDELIG" returneres i output.
	 */
	@Test
	public void verifyResponseIsValid() throws Exception {
		request.getJournalpost().setJournalforendeEnhet("test test teest");
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);

		assertThat(persistedJournalpost.getBrukere(), hasSize(1));
		assertThat(persistedJournalpost.getJournalpostDokumentInfoRelasjoner(), hasSize(2));
		assertJournalpost(persistedJournalpost, request.getJournalpost());
	}

	@Test
	public void shouldRunOkTillegsopplysningerAndKryssreferanseIsNull() throws Exception {
		request.getJournalpost().getTilleggsopplysninger().clear();
		request.getJournalpost().setKryssreferanse(null);

		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);

		assertTrue(persistedJournalpost.getTilleggsopplysninger().isEmpty());
		assertThat(persistedJournalpost.getKryssreferanser(), hasSize(0));
	}

	/**
	 * HVIS journalpost med journalStatus "J" opprettes og input.journalFEnhet IKKE er satt SÅ skal Joark.journalFEnhet settes til "9999"
	 */
	@Test
	public void shouldSetJournalForendeEnhetIdTo9999WhenNull() throws Exception {
		request.getJournalpost().setJournalforendeEnhet(null);

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertThat(persistedJournalpost.getJournalForendeEnhetId(), is("9999"));
	}

	/**
	 * HVIS journalpost med journalStatus "M" opprettes og input.journalFEnhet IKKE er satt SÅ skal Joark.journalFEnhet IKKE settes
	 * HVIS input IKKE inneholder nok informasjon til å endelig journalføre forsendelsen, så blir Journalpost.JournalStatus = "M" og journalTilstand "MIDLERTIDIG" returneres i output.
	 */
	@Test
	public void shouldNotSetJournalForendeEnhetIdWhenJournalStatusIsM() throws Exception {
		request.getJournalpost().setJournalforendeEnhet(null);
		request.setForsokEndeligJF(false);

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertThat(persistedJournalpost.getJournalForendeEnhetId(), nullValue());
	}

	/**
	 * HVIS det finnes et eksisterende Journalpostobjekt i JOARK med journalpost.kanalReferanseId som tilsvarer input.Journalpost.kanalReferanseId OG mottakskanal = input.Journalpost.mottakskanal
	 * SÅ skal ny journalpost IKKE opprettes OG JournalpostId og DokumentInfoId`er på eksisterende journalpost skal returneres som respons på tjenesten
	 */
	@Test
	public void verifyEqualResponseWhenTryingToJournalforSameRequestTwice() throws Exception {
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		JournalforInngaaendeForsendelseResponse secondResponse = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		assertThat(response.getJournalpostId(), is(equalTo(secondResponse.getJournalpostId())));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(equalTo(secondResponse.getDokumentInfoIdHoveddokument())));
	}

	/**
	 * HVIS journalpost med journalStatus "M" opprettes og input.journalFEnhet er satt SÅ skal Joark.journalFEnhet settes lik input.journalFEnhet
	 * HVIS ForsokEndligJF=false så skal Journalpost.JournalStatus = "M"
	 */
	@Test
	public void shouldMidlertidigJournalforeWhenForsokEndeligJFIsFalse() throws Exception {
		request.setForsokEndeligJF(false);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
		assertThat(persistedJournalpost.getJournalForendeEnhetId(), is(request.getJournalpost().getJournalforendeEnhet()));

	}

	@Test
	public void shouldThrowExceptionOnNullJournalpost() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Journalpost");

		request.setJournalpost(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostTema() throws Exception {
		request.getJournalpost().setTema(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldThrowMissingOpprettetAvNavn() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("OpprettetAvNavn");

		request.getJournalpost().setOpprettetAvNavn(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostJournalfEnhet() throws Exception {
		request.getJournalpost().setTema(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullInnhold() throws Exception {
		request.getJournalpost().setInnhold(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostDatoDokument() throws Exception {
		request.getJournalpost().setDatoDokument(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostAvsendMottaker() throws Exception {
		request.getJournalpost().setAvsenderMottakerNavn(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldEndeligJournalforeWhenNullJournalpostAvsenderMottakerId() throws Exception {
		request.getJournalpost().setAvsenderMottakerId(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}

	@Test
	public void shouldThrowMissingMottattDato() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("MottattDato");

		request.getJournalpost().setDatoMottatt(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingMottakskanal() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Mottakskanal");

		request.getJournalpost().setMottakskanal(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingKanalReferanseId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("KanalReferanseId");

		request.getJournalpost().setKanalReferanseId(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullSaksrelasjon() throws Exception {
		request.getJournalpost().setSaksrelasjon(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldThrowEmptySaksrelasjon() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Saksrelasjon");

		request.getJournalpost()
				.setSaksrelasjon(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Saksrelasjon());
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingSaksNummer() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("SaksNummer");

		request.getJournalpost().getSaksrelasjon().setSaksnummer(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingFagsystem() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Fagsystem");

		request.getJournalpost().getSaksrelasjon().setFagsystem(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullBruker() throws Exception {
		request.getJournalpost().setBruker(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldThrowEmptyBruker() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Bruker");

		request.getJournalpost()
				.setBruker(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Bruker());
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingSakId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("BrukerId");

		request.getJournalpost().getBruker().setBrukerId(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowMissingBrukerType() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("BrukerType");

		request.getJournalpost().getBruker().setBrukerType(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingJournalpostInfoRelasjoner() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().clear();
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionEmptyJournalpostInfoRelasjoner() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.add(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalpostDokumentInfoRelasjon());
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingTilknyttetJournalpostSom() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("TilknyttetJournalpostSom");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().setTilknyttetJournalpostSom(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingDokumentInfo() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().setDokumentInfo(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingDokumentInfoKategori() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumentInfo.Kategori");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setKategori(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenDokumenttypeInfoNullTittel() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setTittel(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldEndeligJournalforeWhenDokumenttypeInfoNullbrevKode() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setBrevkode(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}

	@Test
	public void shouldJournalfoereWithMissingDokumentInfoDokumenttypeIdOnVedlegg() throws Exception {
		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(1)
				.getDokumentInfo()
				.setDokumentTypeId(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotMidlertidigJournalforeWhenDokumenttypeInfoNullSensitivt() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setSensitivt(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}


	@Test
	public void shouldThrowExceptionMissingFilDetaljer() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.clear();
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionEmptyFilDetaljer() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.getFildetaljerListe()
				.add(new Fildetaljer());
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljerFiltype() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Filtype");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setFiltype(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

	}

	@Test
	public void shouldEndeligJournalforeWhenFildetaljerNullFilNavn() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setFilNavn(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}

	@Test
	public void shouldEndeligJournalforeWhenFildetaljerNullBatchNavn() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setBatchNavn(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}


	@Test
	public void shouldThrowExceptionMissingFilDetaljerVariantFormat() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.VariantFormat");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setVariantformat(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingFilDetaljerDokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer.Dokument");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getFildetaljerListe().iterator().next().setDokument(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}


	@Test
	public void shouldEndeligJournalforeWhenNullSkannetInnhold() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo()
				.getSkannetInnholdListe().clear();
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}

	@Test
	public void shouldThrowExceptionEmptySkannetInnhold() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("SkannetInnhold");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.add(new no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.SkannetInnhold());
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionMissingSkannetInnholdVedleggInnhold() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("SkannetInnhold");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.setVedleggInnhold(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	/**
	 * HVIS operasjonen kalles med en ugyldig kodeverdi i input SÅ skal det returneres en feil (2)
	 * HVIS operasjonen kalles uten at alle påkrevde inputparametere er oppgitt SÅ skal det returneres en feil (1)
	 */
	@Test
	public void shouldThrowExceptionOnInvalidFagomraade() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagomradeCode.DEEZ_TEMA");

		request.getJournalpost().setTema("DEEZ_TEMA");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenInvalidBruker() throws Exception {
		request.getJournalpost().getBruker().setBrukerId("INVALID");
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = getAllJournalposts();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}


	@Test
	public void shouldThrowExceptionOnInvalidMottakskanal() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.DEEZ_MOTTAKSKANAL");

		request.getJournalpost().setMottakskanal("DEEZ_MOTTAKSKANAL");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFagsystem() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagsystemCode.DEEZ_FORSENDELSE");

		request.getJournalpost().getSaksrelasjon().setFagsystem("DEEZ_FORSENDELSE");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidBrukerType() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.DEEZ_BRUKERTYPE");

		request.getJournalpost().getBruker().setBrukerType("DEEZ_BRUKERTYPE");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidDokumentinfoKategori() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.DEEZ_KATEGORI");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.setKategori("DEEZ_KATEGORI");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFildetaljerFiltype() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FilTypeCode.DEEZ_FILTYPE");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setFiltype("DEEZ_FILTYPE");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFildetaljerVariantFormat() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage(
				"No enum constant no.nav.dokarkiv.core.domain.codes.VariantFormatCode.DEEZ_VARIANTFORMAT");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat("DEEZ_VARIANTFORMAT");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	/**
	 * HVIS operasjonen kalles med flere Fildetaljer for et DokumentInfo-objekt OG to av disse har identiske variantformater SÅ skal det returneres en feil (4)
	 */
	@Test
	public void shouldThrowExceptionOnMultipleOfSameArkivvariant() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates, found 2 ARKIV varianter");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.add(addFildetaljer(
						VariantFormatCode.ARKIV));

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	/**
	 * HVIS operasjonen kalles med en Fildetaljer for et DokumentInfo-objekt OG denne mangler variantformat = "ARKIV" SÅ skal det returneres en feil (3)
	 */
	@Test
	public void shouldThrowExceptionOnZeroArkivvariant() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant when endelig journalforing");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat("PRODUKSJON");

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	/**
	 * HVIS en journalpost ikke inneholder nøyaktig 1 JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom = ”Hoveddokument" SÅ returner en feil (5)
	 */
	@Test
	public void shouldThrowExceptionOnMissingHoveddokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost must contain a hoveddokument");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.setTilknyttetJournalpostSom(TilknyttetJournalpostEnum.VEDLEGG);

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnMoreThanOneHveddokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost cannot contain more than one hoveddokument");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(1)
				.setTilknyttetJournalpostSom(TilknyttetJournalpostEnum.HOVEDDOKUMENT);

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}


	private void assertJournalStatus(no.nav.dokarkiv.core.domain.entities.Journalpost domain, JournalStatusCode journalStatus) {
		assertThat(domain.getJournalstatus(), is(journalStatus));
	}

	private void assertJournalpost(no.nav.dokarkiv.core.domain.entities.Journalpost domain, Journalpost request) {
		assertThat(domain.getFagomrade().name(), is(request.getTema()));
		assertThat(domain.getBehandlingstema().name(), is(request.getBehandlingstema()));
		assertThat(domain.getAvsenderMottaker(), is(request.getAvsenderMottakerNavn()));
		assertThat(domain.getAvsenderMottakerId(), is(request.getAvsenderMottakerId()));
		assertThat(domain.getJournalForendeEnhetId(), is(request.getJournalforendeEnhet()));
		assertThat(domain.getInnhold(), is(request.getInnhold()));
		assertThat(toXMLGregorianCalendar(domain.getMottattDato()), is(request.getDatoMottatt()));
		assertThat(domain.getMottakskanal().name(), is(request.getMottakskanal()));
		assertThat(toXMLGregorianCalendar(domain.getDokumentDato()), is(request.getDatoDokument()));
		assertThat(domain.getOpprettetAvNavn(), is(request.getOpprettetAvNavn()));
		assertThat(domain.getKanalReferanseId(), is(request.getKanalReferanseId()));

		assertThat(domain.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(domain.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(domain.getJournalfortAvNavn(), is(request.getOpprettetAvNavn()));

		assertTrue((domain.getJournalDato().getTime() - Date.from(LocalDateTime.now()
				.atZone(ZoneOffset.systemDefault())
				.toInstant()).getTime()) < 1000);

		assertSaksrelasjon(domain.getSaksrelasjon(), request.getSaksrelasjon());
		assertBruker(domain.getBrukere().iterator().next(), request.getBruker());

		JournalpostDokumentInfoRelasjon domainDokumentRelasjon = domain.findHoveddokumentDokumentInfoRelasjon();

		assertThat(domainDokumentRelasjon.getTilknyttetAvNavn(), is(request.getOpprettetAvNavn()));

		assertJournalpostDokumentInfoRelasjon(domainDokumentRelasjon,
				request.getJournalpostDokumentInfoRelasjon().get(0), domain.getJournalpostId());

		assertKryssreferanse(domain.getKryssreferanser().iterator().next());

		Set<JournalpostDokumentInfoRelasjon> vedlegg = domain.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedlegg, hasSize(1));
		for (JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon : vedlegg) {
			assertJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon, request.getJournalpostDokumentInfoRelasjon()
					.get(1), domain.getJournalpostId());
		}
	}

	private void assertSaksrelasjon(Saksrelasjon domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Saksrelasjon request) {
		assertThat(domain.getFagsystem().name(), Matchers.is(request.getFagsystem()));
		assertThat(domain.getSakId(), Matchers.is(request.getSaksnummer()));
	}

	private void assertBruker(Bruker domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Bruker request) {
		assertThat(domain.getBrukerId(), Matchers.is(request.getBrukerId()));
		assertThat(domain.getBrukerType().name(), Matchers.is(request.getBrukerType()));
	}

	private void assertJournalpostDokumentInfoRelasjon(JournalpostDokumentInfoRelasjon domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalpostDokumentInfoRelasjon request, Long journalPostId) {
		assertThat(domain.getTilknyttetJournalpostSom().name(), is(request.getTilknyttetJournalpostSom().name()));
		assertDokumentInfo(domain.getDokumentInfo(), request.getDokumentInfo(), journalPostId);
	}

	private void assertDokumentInfo(DokumentInfo domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.DokumentInfo request, Long journalPostId) {
		assertThat(domain.getKategori().name(), is(request.getKategori()));
		assertThat(domain.getTittel(), is(request.getTittel()));
		assertThat(domain.getDokumenttypeId(), is(request.getDokumentTypeId()));
		assertThat(domain.getSensitivt(), is(request.isSensitivt()));
		assertThat(domain.getFildetaljerListe(), hasSize(1));
		assertThat(domain.getOriginalJournalpost().getJournalpostId(), is(journalPostId));

		assertSkannetInnhold(domain.getSkannetInnholdListe().iterator().next(), request.getSkannetInnholdListe().get(0));
		assertFilDetaljer(domain.getFildetaljerListe().iterator().next(), request.getFildetaljerListe().get(0));
	}

	private void assertFilDetaljer(FilDetaljer domain, Fildetaljer request) {
		assertThat(domain.getFiltype().name(), is(request.getFiltype()));
		assertThat(domain.getVariantFormat().name(), is(request.getVariantformat()));
		assertThat(domain.getFileContent(), is(request.getDokument()));
		assertThat(domain.getFilstorrelse(), notNullValue());
	}

	private void assertSkannetInnhold(SkannetInnhold domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.SkannetInnhold request) {
		assertThat(domain.getVedleggInnhold(), is(request.getVedleggInnhold()));
		assertThat(domain.getDokumenttypeid(), is(request.getDokumentTypeId()));
	}

	public static void assertTilleggsopplysninger(Map<String, String> domain) {
		assertThat(domain.get(OPPLYSNINGSNOEKKEL1), Matchers.is(OPPLYSNINGSVERDI1));
		assertThat(domain.get(OPPLYSNINGSNOEKKEL2), Matchers.is(OPPLYSNINGSVERDI2));
	}

	public static void assertKryssreferanse(Kryssreferanse kryssreferanse) {
		assertThat(kryssreferanse.getReferanseId(), Matchers.is(REFERANSE_ID));
		assertThat(kryssreferanse.getReferanseType().name(), Matchers.is(ReferanseTypeCode.SPOERSMAAL.name()));
	}

	private void assertResponse(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost, JournalforInngaaendeForsendelseResponse response, JournalTilstandEnum journalTilstand) {
		assertThat(response, notNullValue());
		assertThat(response.getJournalpostId(), is(journalpost.getId()));
		assertThat(response.getJournalTilstand(), is(journalTilstand));

		no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon pDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		assertThat(response.getDokumentInfoIdHoveddokument(), is(pDokumentInfoRelasjon.getDokumentInfo().getDokumentInfoId()));

		for (JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (jdir.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.VEDLEGG)) {
				assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentInfoId(), is(jdir.getDokumentInfo()
						.getDokumentInfoId()));
				assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentTypeId(), is(jdir.getDokumentInfo()
						.getDokumenttypeId()));
			}
		}
	}
}
