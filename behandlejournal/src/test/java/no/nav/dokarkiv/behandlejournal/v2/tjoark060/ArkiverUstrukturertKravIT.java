package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import no.nav.dokarkiv.behandlejournal.v2.AbstractBehandleJournalV2Itest;
import no.nav.dokarkiv.behandlejournal.v2.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokument;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringDokumentType;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagringStatus;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.EksternPart;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Kommunikasjonskanaler;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.NorskIdent;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Person;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Signatur;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Variantformater;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravResponse;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for the MOD operation: ArkiverUtstrukturertKrav.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class ArkiverUstrukturertKravIT extends AbstractBehandleJournalV2Itest {

	private static final String FNR_BRUKER = "***gammelt_fnr***";
	private static final String KANAL_ALTINN = "ALTINN";
	private static final boolean SIGNERT_TRUE = true;
	private static final String TEMAVALUE_PEN = "PEN";
	private static final String TEMAVALUE_BID = "BID";
	private static final byte[] DOKUMENT = "dette er et dokument".getBytes();
	private static final String NAVN = "navn";
	private static final String ORGNUMMER = "1234422222";

	private ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest;
	private ArkiverUstrukturertKravResponse arkiverUstrukturertKravResponse;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;
	private BidragMellomlagring persistedBidragMellomlagring;

	@Before
	public void setUp() {
		DateProvider.configure(true, "2018-07-11T12:00");
		RequestContextSetter.setRequestContextForUnitTest();
		createRequest();
	}

	private void createRequest() {
		arkiverUstrukturertKravRequest = new ArkiverUstrukturertKravRequest();
		arkiverUstrukturertKravRequest.setPersonFornavn("fornavn");
		arkiverUstrukturertKravRequest.setPersonEtternavn("etternavn");
		arkiverUstrukturertKravRequest.setApplikasjonsID("applikasjonsId");
	}

	public void setUpJoark() {
		arkiverUstrukturertKravRequest.setJournalpost(createJournalpost(TEMAVALUE_PEN));
		arkiverUstrukturertKravResponse = behandleJournalProvider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);

		persistedJournalpost = joarkRepository.findById(Long.parseLong(arkiverUstrukturertKravResponse
				.getJournalpostId())).get();
	}

	public void setUpBidrag() {
		arkiverUstrukturertKravRequest.setJournalpost(createJournalpost(TEMAVALUE_BID));
		arkiverUstrukturertKravResponse = behandleJournalProvider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);

		persistedBidragMellomlagring = bidragMellomlagringRepository.findById(BidragMellomlagring.removePrefixFromId(Long
				.parseLong(arkiverUstrukturertKravResponse.getJournalpostId()))).get();
	}

	@Test
	public void shouldHaveArkivertAndReturnAJournalpost() {
		setUpJoark();

		assertNotNull(persistedJournalpost);
	}

	@Test
	public void shouldVerifyPersistedJournalpostPropertiesAgaintInputValues() {
		setUpJoark();

		assertJournalpostProperties(persistedJournalpost);
	}

	@Test
	public void shouldSaveAvsenderMottaker() {
		Journalpost journalpost = createJournalpost(TEMAVALUE_PEN);
		journalpost.setEksternPart(createEksternPart());

		arkiverUstrukturertKravRequest.setJournalpost(journalpost);
		arkiverUstrukturertKravResponse = behandleJournalProvider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);

		persistedJournalpost = getPersistedJournalposterById(Long.parseLong(arkiverUstrukturertKravResponse
				.getJournalpostId()));

		assertThat(persistedJournalpost.getAvsenderMottaker(), is(NAVN));
		assertThat(persistedJournalpost.getAvsenderMottakerId(), is(ORGNUMMER));
	}

	@Test
	public void shouldFailInAvsenderMottakerValidation() {
		Journalpost journalpost = createJournalpost(TEMAVALUE_PEN);
		EksternPart eksternPart = new EksternPart();
		eksternPart.setNavn(NAVN);
		journalpost.setEksternPart(eksternPart);

		arkiverUstrukturertKravRequest.setJournalpost(journalpost);

		expectedException.expect(ApplicationException.class);
		expectedException.expectMessage("Journalpost.AvsenderMottakerId must be set when Journalpost.AvsenderMottaker is set");
		arkiverUstrukturertKravResponse = behandleJournalProvider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);
	}

	@Test
	public void shouldVerifyThatPersistedDokumentMatchInputDokument() {
		setUpJoark();

		assertDokumentSaved(persistedJournalpost);
	}

	@Test
	public void shouldReturnJournalpostIdAndDokumentId() {
		setUpJoark();

		assertThat(arkiverUstrukturertKravResponse.getJournalpostId(), is(notNullValue()));
		assertThat(arkiverUstrukturertKravResponse.getDokumentId(), is(notNullValue()));
	}

	@Test
	public void shouldVerifyPersistedBidragMellomlagring() {
		setUpBidrag();

		assertBidragMellomlagringProperties(persistedBidragMellomlagring);
	}

	@Test
	public void shouldVerifyThatPersistedBidragMellomlagringDokumentMatchesInputDokument() {
		setUpBidrag();

		assertBidragMellomlagringDokumentSaved(persistedBidragMellomlagring.getBidragMellomlagringDokuments()
				.iterator().next());
	}

	@Test
	public void shouldReturnBidragMellomlagringIdWithPrefixAndBidragMellomlagrindDokumentId() {
		setUpBidrag();

		assertTrue(BidragMellomlagring.isBidragMellomLagringId(Long.parseLong(arkiverUstrukturertKravResponse
				.getJournalpostId())));
		assertThat(arkiverUstrukturertKravResponse.getDokumentId(), is(notNullValue()));
	}


	private void assertJournalpostProperties(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		assertThat(journalpost.getBrukere().iterator().next().getBrukerId(), is(FNR_BRUKER));
		assertThat(journalpost.getMottakskanal().name(), is(KANAL_ALTINN));
		assertThat(journalpost.getSignatur(), is(SIGNERT_TRUE));
		assertThat(journalpost.getFagomrade().name(), is(TEMAVALUE_PEN));
	}

	private void assertBidragMellomlagringProperties(BidragMellomlagring bidragMellomlagring) {
		assertThat(bidragMellomlagring.getAvsenderFnr(), is(FNR_BRUKER));
		assertThat(bidragMellomlagring.getMottattDato(), is(DateProvider.getToday()));
		assertThat(bidragMellomlagring.getStatus(), is(BidragMellomlagringStatus.DOKUMENTOPPLASTING));
	}

	private void assertDokumentSaved(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		String filUuid = journalpost.findAllFilDetaljer().get(0).getFilUuid();
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
		assertThat(dokumentFil.getFil(), is(DOKUMENT));
	}

	private void assertBidragMellomlagringDokumentSaved(BidragMellomlagringDokument bidragMellomlagringDokument) {
		assertThat(bidragMellomlagringDokument.getDokumentType(), is(BidragMellomlagringDokumentType.HOVEDDOKUMENT));
		assertThat(bidragMellomlagringDokument.getDokument(), is(DOKUMENT));
	}

	private no.nav.dokarkiv.core.domain.entities.Journalpost getPersistedJournalposterById(Long id) {
		return joarkRepository.findById(id).get();
	}

	private Journalpost createJournalpost(String temaId) {
		Journalpost journalpost = new Journalpost();
		journalpost.setKanal(KodeverdiHelper.kodeVerdi(KANAL_ALTINN, Kommunikasjonskanaler.class));
		journalpost.setSignatur(createSignatur(SIGNERT_TRUE));
		journalpost.setArkivtema(KodeverdiHelper.kodeVerdi(temaId, Arkivtemaer.class));
		journalpost.setDokumentDato(getXmlTimestamp());
		journalpost.setMottattDato(getXmlTimestamp());
		journalpost.getForBruker().add(createBruker());
		journalpost.setJournalfoertDokument(createJournalfortDokumentInfo());
		return journalpost;
	}

	private JournalfoertDokumentInfo createJournalfortDokumentInfo() {
		JournalfoertDokumentInfo dokumentInfo = new JournalfoertDokumentInfo();
		NoekkelVerdiPar noekkelVerdiPar = new NoekkelVerdiPar();
		noekkelVerdiPar.setNoekkel("Test noekkel");
		noekkelVerdiPar.setVerdi("Test verdi");
		NoekkelVerdiSett noekkelVerdiSett = new NoekkelVerdiSett();
		noekkelVerdiSett.getInneholderNoekkelVerdiPar().add(noekkelVerdiPar);
		dokumentInfo.setTilleggsopplysninger(noekkelVerdiSett);
		dokumentInfo.setDokumentType(KodeverdiHelper.kodeVerdi("dokumenttypeId", Dokumenttyper.class));
		dokumentInfo.getBeskriverInnhold().add(createDokumentInnhold());
		return dokumentInfo;
	}

	private DokumentInnhold createDokumentInnhold() {
		UstrukturertInnhold innhold = new UstrukturertInnhold();
		innhold.setFilnavn("test.pdf");
		innhold.setFiltype(KodeverdiHelper.kodeVerdi("PDF", Arkivfiltyper.class));
		innhold.setVariantformat(KodeverdiHelper.kodeVerdi("ARKIV", Variantformater.class));
		innhold.setInnhold(DOKUMENT);
		return innhold;
	}

	private Aktoer createBruker() {
		Person bruker = new Person();
		NorskIdent ident = new NorskIdent();
		ident.setIdent(FNR_BRUKER);
		bruker.setIdent(ident);
		return bruker;
	}

	private Signatur createSignatur(boolean signert) {
		Signatur signatur = new Signatur();
		signatur.setSignert(signert);
		return signatur;
	}

	private EksternPart createEksternPart() {
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setOrgnummer(ORGNUMMER);

		EksternPart eksternPart = new EksternPart();
		eksternPart.setNavn(NAVN);
		eksternPart.setEksternAktoer(organisasjon);
		return eksternPart;

	}
}
