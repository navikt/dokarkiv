package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.toXMLGregorianCalendar;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil.FORSENDELSE_MOTTA_VALUE;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil.addFildetaljer;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil.addVedlegg;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.JournalforInngaaendeForsendelseRequestDataUtil.createJournalpost;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.arkiverdokumentmottak.AbstractArkiverDokumentmottakItest;
import no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Bruker;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.Saksrelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Integration test for HentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class JournalforInngaaendeForsendelseIT extends AbstractArkiverDokumentmottakItest {

	private JournalforInngaaendeForsendelseRequest request;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@After
	public void tearDown() throws Exception {
		entityManager.flush();
	}

	@Before
	public void setUp() throws Exception {
		no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost journalpostRequest = createJournalpost();
		journalpostRequest.getJournalpostDokumentInfoRelasjon().add(addVedlegg());
		RequestContextSetter.setRequestContextForUnitTest();
		request = new JournalforInngaaendeForsendelseRequest();
		request.setJournalpost(journalpostRequest);
	}

	@Test
	public void verifyResponseIsValid() throws Exception {
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);

		List<Journalpost> allJournalposts = StreamSupport.stream(joarkRepository.findAll().spliterator(), false).collect(Collectors.toList());
		assertThat(allJournalposts, hasSize(1));

		Journalpost persistedJournalpost = allJournalposts.get(0);

		assertResponse(persistedJournalpost, response);

		assertThat(persistedJournalpost.getBrukere(), hasSize(1));
		assertThat(persistedJournalpost.getJournalpostDokumentInfoRelasjoner(), hasSize(2));

		assertJournalpost(persistedJournalpost, request.getJournalpost());
	}

	@Test
	public void verifyEqualResponseWhenTryingToJournalforSameRequestTwice() throws Exception {
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
		JournalforInngaaendeForsendelseResponse secondResponse = arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);

		assertThat(response, is(equalTo(secondResponse)));
	}

	@Test
	public void verifyNotEqualResponseWhenTryingToJournalforSameRequestTwiceAndIsMissingTilleggsopplysning() throws Exception {
		request.getJournalpost().getJournalpostTilleggsopplysninger().clear();
		JournalforInngaaendeForsendelseResponse response = arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
		JournalforInngaaendeForsendelseResponse secondResponse = arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
		assertThat(response.getJournalpostId(), is(not(equalTo(secondResponse.getJournalpostId()))));
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostTema() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost.fagomrade must be set");

		request.getJournalpost().setTema(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostOpprettetAvNavn() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost.opprettetAvNavn must be set");

		request.getJournalpost().setOpprettetAvNavn(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostJournalfEnhet() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost.journalForendeEnhetId must be set");

		request.getJournalpost().setJournalforendeEnhet(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullInnhold() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost.innhold must be set");

		request.getJournalpost().setInnhold(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostDatoDokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.DokumentDato");

		request.getJournalpost().setDatoDokument(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostAvsendMottaker() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost.avsenderMottaker must be set");

		request.getJournalpost().setAvsenderMottakerNavn(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotThrowExceptionOnNullJournalpostAvsenderMottakerId() throws Exception {
		request.getJournalpost().setAvsenderMottakerId(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostDatoMottatt() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.MottatDato");

		request.getJournalpost().setDatoMottatt(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostMottakskanaler() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Journalpost.Mottakskanal");

		request.getJournalpost().setMottakskanal(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFagomraade() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagomradeCode.DEEZ_TEMA");

		request.getJournalpost().setTema("DEEZ_TEMA");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidMottakskanal() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.DEEZ_MOTTAKSKANAL");

		request.getJournalpost().setMottakskanal("DEEZ_MOTTAKSKANAL");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullSaksrelasjon() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: Saksrelasjon");

		request.getJournalpost().setSaksrelasjon(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullSaksrelasjonSaksnummer() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Saksrelasjon.sakId must be set");

		request.getJournalpost().getSaksrelasjon().setSaksnummer(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullSaksrelasjonInnForsendelse() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Saksrelasjon.fagsystem must be set");

		request.getJournalpost().getSaksrelasjon().setFagsystem(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidSaksrelasjonInnForsendelse() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FagsystemCode.DEEZ_FORSENDELSE");

		request.getJournalpost().getSaksrelasjon().setFagsystem("DEEZ_FORSENDELSE");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullBruker() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing or empty list of required field in request: Brukere");

		request.getJournalpost().setBruker(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullBrukerId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Bruker.brukerId must be set");

		request.getJournalpost().getBruker().setBrukerId(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullBrukerType() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Bruker.brukerType must be set");

		request.getJournalpost().getBruker().setBrukerType(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidBrukerType() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.BrukerTypeCode.DEEZ_BRUKERTYPE");

		request.getJournalpost().getBruker().setBrukerType("DEEZ_BRUKERTYPE");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnEmptyJournalpostDokumentInfoRelasjon() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing or empty list of required field in request: JournalpostDokumentInfoRelasjoner");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().clear();
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullJournalpostDokumentInfoRelasjonTilknJournal() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom must be set");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).setTilknyttetJournalpostSom(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidJournalpostDokumentInfoRelasjonTilknJournal() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(
				"No enum constant no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum.DEEZ_TILKNJOURNAL");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).setTilknyttetJournalpostSom(
				TilknyttetJournalpostEnum.fromValue("DEEZ_TILKNJOURNAL"));
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullDokumentInfo() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: JournalpostDokumentInfoRelasjoner.DokumentInfo");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).setDokumentInfo(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullDokumentInfoKategori() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumentInfo.kategori must be set");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setKategori(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullDokumentInfoTittel() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("DokumentInfo.tittel must be set");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setTittel(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotThrowExceptionOnNullDokumentInfoBrevkode() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setBrevkode(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotThrowExceptionOnInvalidDokumentInfoBrevkode() throws Exception {
		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setBrevkode("DEEZ_BREVKODE");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullDokumentInfoDokumentTypeId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: DokumentInfo.DokumenttypeId");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setDokumentTypeId(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotThrowExceptionWhenNullSensitiv() throws Exception {

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().setSensitivt(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnEmptyFildetaljer() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage(
				"Missing or empty list of required field in request: JournalpostDokumentInfoRelasjoner.DokumentInfo.Fildetaljer");

		request.getJournalpost().getJournalpostDokumentInfoRelasjon().get(0).getDokumentInfo().getFildetaljerListe().clear();
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullFildetaljerFiltype() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("FilDetaljer.filtype must be set");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setFiltype(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldNotThrowExceptionOnNullFildetaljerFilNavn() throws Exception {
		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setFilNavn(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullFildetaljerVariantFormat() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("FilDetaljer.variantFormat must be set");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnNullFildetaljerDokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Missing required field in request: FilDetaljer.FileContent");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setDokument(null);
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFildetaljerFiltype() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage("No enum constant no.nav.dokarkiv.core.domain.codes.FilTypeCode.DEEZ_FILTYPE");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setFiltype("DEEZ_FILTYPE");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnInvalidFildetaljerVariantFormat() throws Exception {
		expectedException.expect(IllegalArgumentException.class);
		expectedException.expectMessage(
				"No enum constant no.nav.dokarkiv.core.domain.codes.VariantFormatCode.DEEZ_VARIANTFORMAT");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.getDokumentInfo()
				.getFildetaljerListe()
				.get(0)
				.setVariantformat("DEEZ_VARIANTFORMAT");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
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

		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldThrowExceptionOnMissingHoveddokument() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("Journalpost must contain either a hoveddokument or a sammensatt dokument when endelig journalforing");

		request.getJournalpost()
				.getJournalpostDokumentInfoRelasjon()
				.get(0)
				.setTilknyttetJournalpostSom(TilknyttetJournalpostEnum.VEDLEGG);

		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	@Test
	public void shouldFailOnInvalidBrukerId() throws Exception {
		expectedException.expect(KanIkkeJournalfores.class);
		expectedException.expectMessage("BrukerId is not a valid fnr: DEEZ_NAH");

		request.getJournalpost().getBruker().setBrukerId("DEEZ_NAH");
		arkiverDokumentmottakProviderV1.journalforInngaaendeForsendelse(request);
	}

	private void assertJournalpost(Journalpost domain, no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost request) {
		assertThat(domain.getFagomrade().name(), is(request.getTema()));
		assertThat(domain.getAvsenderMottaker(), is(request.getAvsenderMottakerNavn()));
		assertThat(domain.getAvsenderMottakerId(), is(request.getAvsenderMottakerId()));
		assertThat(domain.getJournalForendeEnhetId(), is(request.getJournalforendeEnhet()));
		assertThat(domain.getInnhold(), is(request.getInnhold()));
		assertThat(toXMLGregorianCalendar(domain.getMottattDato()), is(request.getDatoMottatt()));
		assertThat(domain.getMottakskanal().name(), is(request.getMottakskanal()));
		assertThat(domain.getJournalstatus(), is(JournalStatusCode.J));
		assertThat(domain.getJournalposttype(), is(JournalpostTypeCode.I));
		assertThat(toXMLGregorianCalendar(domain.getDokumentDato()), is(request.getDatoDokument()));
		assertThat(domain.getJournalfortAvNavn(), is(request.getOpprettetAvNavn()));
		assertThat(domain.getOpprettetAvNavn(), is(request.getOpprettetAvNavn()));
		assertThat(domain.getTilleggsopplysninger(), hasEntry(ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY, FORSENDELSE_MOTTA_VALUE));

//		assertEquals(domain.getJournalDato(), DateProvider.getToday());

		assertSaksrelasjon(domain.getSaksrelasjon(), request.getSaksrelasjon());
		assertBruker(domain.getBrukere().iterator().next(), request.getBruker());

		no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon domainDokumentRelasjon = domain.findHoveddokumentDokumentInfoRelasjon();

		assertThat(domainDokumentRelasjon.getTilknyttetAvNavn(), is(request.getOpprettetAvNavn()));

		assertJournalpostDokumentInfoRelasjon(domainDokumentRelasjon,
				request.getJournalpostDokumentInfoRelasjon().get(0));

		Set<no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon> vedlegg = domain.findDokumentInfoRelasjonByTilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG);
		assertThat(vedlegg, hasSize(1));
		for (no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon : vedlegg) {
			assertJournalpostDokumentInfoRelasjon(journalpostDokumentInfoRelasjon, request.getJournalpostDokumentInfoRelasjon()
					.get(1));
		}
	}

	private void assertSaksrelasjon(no.nav.dokarkiv.core.domain.entities.Saksrelasjon domain, Saksrelasjon request) {
		assertThat(domain.getFagsystem().name(), Matchers.is(request.getFagsystem()));
		assertThat(domain.getSakId(), Matchers.is(request.getSaksnummer()));
	}

	private void assertBruker(no.nav.dokarkiv.core.domain.entities.Bruker domain, Bruker request) {
		assertThat(domain.getBrukerId(), Matchers.is(request.getBrukerId()));
		assertThat(domain.getBrukerType().name(), Matchers.is(request.getBrukerType()));
	}

	private void assertJournalpostDokumentInfoRelasjon(no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon domain, JournalpostDokumentInfoRelasjon request) {
		assertThat(domain.getTilknyttetJournalpostSom().name(), is(request.getTilknyttetJournalpostSom().value()));
		assertDokumentInfo(domain.getDokumentInfo(), request.getDokumentInfo());
	}

	private void assertDokumentInfo(no.nav.dokarkiv.core.domain.entities.DokumentInfo domain, DokumentInfo request) {
		assertThat(domain.getKategori().name(), is(request.getKategori()));
		assertThat(domain.getTittel(), is(request.getTittel()));
		assertThat(domain.getDokumenttypeId(), is(request.getDokumentTypeId()));
		assertThat(domain.getSensitivt(), is(request.isSensitivt()));
		assertThat(domain.getFildetaljerListe(), hasSize(1));

		assertFilDetaljer(domain.getFildetaljerListe().iterator().next(), request.getFildetaljerListe().get(0));
	}

	private void assertFilDetaljer(FilDetaljer domain, Fildetaljer request) {
		assertThat(domain.getFiltype().name(), is(request.getFiltype()));
		assertThat(domain.getVariantFormat().name(), is(request.getVariantformat()));
		assertThat(domain.getFileContent(), is(request.getDokument()));
	}


	private JournalforInngaaendeForsendelseRequest createRequest() {
		return new JournalforInngaaendeForsendelseRequest()
				.withJournalpost(createJournalpost());
	}

	private void assertResponse(Journalpost journalpost, JournalforInngaaendeForsendelseResponse response) {
		Assert.assertThat(response, Matchers.notNullValue());
		Assert.assertThat(response.getJournalpostId(), is(journalpost.getId()));

		no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon pDokumentInfoRelasjon = journalpost.findHoveddokumentDokumentInfoRelasjon();
		Assert.assertThat(response.getDokumentInfoIdHoveddokument(), is(pDokumentInfoRelasjon.getDokumentInfo()
				.getDokumentInfoId()));

		for (no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon jdir : journalpost.getJournalpostDokumentInfoRelasjoner()) {
			if (jdir.getTilknyttetJournalpostSom().equals(TilknyttetJournalpostSomCode.VEDLEGG)) {
				Assert.assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentInfoId(), is(jdir.getDokumentInfo()
						.getDokumentInfoId()));
				Assert.assertThat(response.getDokumentInfoIdVedleggListe().get(0).getDokumentTypeId(), is(jdir.getDokumentInfo()
						.getDokumenttypeId()));
			}
		}
	}


}
