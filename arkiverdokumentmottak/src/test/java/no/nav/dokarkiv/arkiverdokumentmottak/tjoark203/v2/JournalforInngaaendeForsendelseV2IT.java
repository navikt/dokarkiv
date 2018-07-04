package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.toXMLGregorianCalendar;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.addFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.addVedlegg;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseV2RequestDataUtil.createJournalpost;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractArkiverDokumentmottakItest;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
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
import java.util.Set;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2IT extends AbstractArkiverDokumentmottakItest {

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

	@Test
	public void verifyResponseIsValid() throws Exception {
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);

		assertThat(persistedJournalpost.getBrukere(), hasSize(1));
		assertThat(persistedJournalpost.getJournalpostDokumentInfoRelasjoner(), hasSize(2));
		assertJournalpost(persistedJournalpost, request.getJournalpost());
	}

	@Test
	public void verifyEqualResponseWhenTryingToJournalforSameRequestTwice() throws Exception {
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		JournalforInngaaendeForsendelseResponse secondResponse = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);

		assertThat(response.getJournalpostId(), is(equalTo(secondResponse.getJournalpostId())));
		assertThat(response.getDokumentInfoIdHoveddokument(), is(equalTo(secondResponse.getDokumentInfoIdHoveddokument())));
	}

	@Test
	public void shouldMidlertidigJournalforeWhenForsokEndeligJFIsFalse() throws Exception {
		request.setForsokEndeligJF(false);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpost() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Journalpost. ");

		request.setJournalpost(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostTema() throws Exception {
		request.getJournalpost().setTema(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		request.getJournalpost().setJournalforendeEnhet(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullInnhold() throws Exception {
		request.getJournalpost().setInnhold(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostDatoDokument() throws Exception {
		request.getJournalpost().setDatoDokument(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenNullJournalpostAvsendMottaker() throws Exception {
		request.getJournalpost().setAvsenderMottakerNavn(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldEndeligJournalforeWhenNullJournalpostAvsenderMottakerId() throws Exception {
		request.getJournalpost().setAvsenderMottakerId(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}

	@Test
	public void shouldEndeligJournalforeWhenDokumenttypeInfoNullbrevKode() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setBrevkode(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_ENDELIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.J);
	}

	@Test
	public void shouldThrowExceptionMissingDokumentInfoDokumenttypeId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumentInfo.DokumenttypeId");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.setDokumentTypeId(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenDokumenttypeInfoNullSensitivt() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().iterator().next().getDokumentInfo().setSensitivt(null);
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
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

	@Test
	public void shouldThrowExceptionMissingSkannetInnholdDokumenttypeId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumenttypeId");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.iterator()
				.next()
				.getDokumentInfo()
				.getSkannetInnholdListe()
				.iterator()
				.next()
				.setDokumentTypeId(null);
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}


	@Test
	public void shouldThrowExceptionOnInvalidFagomraade() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagomradeCode.DEEZ_TEMA.");

		request.getJournalpost().setTema("DEEZ_TEMA");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldMidlertidigJournalforeWhenInvalidBruker() throws Exception {
		request.getJournalpost().getBruker().setBrukerId("INVALID");
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
		List<no.nav.dokarkiv.core.domain.entities.Journalpost> allJournalposts = (List) joarkRepository.findAll();
		assertThat(allJournalposts, hasSize(1));

		no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response, JOURNALTILSTAND_MIDLERTIDIG);
		assertJournalStatus(persistedJournalpost, JournalStatusCode.M);
	}


	@Test
	public void shouldThrowExceptionOnInvalidMottakskanal() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.DEEZ_MOTTAKSKANAL.");

		request.getJournalpost().setMottakskanal("DEEZ_MOTTAKSKANAL");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFagsystem() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagsystemCode.DEEZ_FORSENDELSE.");

		request.getJournalpost().getSaksrelasjon().setFagsystem("DEEZ_FORSENDELSE");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidBrukerType() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.DEEZ_BRUKERTYPE.");

		request.getJournalpost().getBruker().setBrukerType("DEEZ_BRUKERTYPE");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidDokumentinfoKategori() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode.DEEZ_KATEGORI.");

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
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FilTypeCode.DEEZ_FILTYPE.");

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
				"No enum constant no.nav.dokarkiv.core.domain.codes.VariantFormatCode.DEEZ_VARIANTFORMAT.");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat("DEEZ_VARIANTFORMAT");
		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

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

	@Test
	public void shouldThrowExceptionOnZeroArkivvariant() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("All the Journalpost's DokumentInfos must contain an arkiv variant when endelig journalforing.");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat("PRODUKSJON");

		arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

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
	}

	private void assertSkannetInnhold(SkannetInnhold domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.SkannetInnhold request) {
		assertThat(domain.getVedleggInnhold(), is(request.getVedleggInnhold()));
		assertThat(domain.getDokumenttypeid(), is(request.getDokumentTypeId()));
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
