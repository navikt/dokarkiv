package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.dokarkiv.behandlejournal.v3.AbstractBehandleJournalV3Itest;
import no.nav.dokarkiv.behandlejournal.v3.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.stelvio.RequestContextSetter;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Aktoer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Arkivfiltyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.DokumentInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.EksternPart;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Kommunikasjonskanaler;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiPar;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NoekkelVerdiSett;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.NorskIdent;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Organisasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Person;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Signatur;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.UstrukturertInnhold;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Variantformater;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration test for the MOD operation: ArkiverUtstrukturertKrav.
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class ArkiverUstrukturertKravV3IT extends AbstractBehandleJournalV3Itest {

	private static final String FNR_BRUKER = "01054512313";
	private static final String KANAL_ALTINN = "ALTINN";
	private static final boolean SIGNERT_TRUE = true;
	private static final String TEMAVALUE_PEN = "PEN";
	private static final String TEMAVALUE_FOR = "FOR";
	private static final String TEMAVALUE_BID = "BID";
	private static final byte[] DOKUMENT = "dette er et dokument".getBytes();
	private static final String NAVN = "navn";
	private static final String ORGNUMMER = "1234422222";

	private ArkiverUstrukturertKravRequest arkiverUstrukturertKravRequest;
	private ArkiverUstrukturertKravResponse arkiverUstrukturertKravResponse;
	private no.nav.dokarkiv.core.domain.entities.Journalpost persistedJournalpost;

	@BeforeEach
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

	private void setUpJoark() throws Exception {
		setUpJoark(TEMAVALUE_FOR);
	}

	private void setUpJoark(String temaValue) throws Exception {
		arkiverUstrukturertKravRequest.setJournalpost(createJournalpost(temaValue));
		arkiverUstrukturertKravResponse = behandleJournalV3Provider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);

		persistedJournalpost = joarkRepository.findById(Long.parseLong(arkiverUstrukturertKravResponse
				.getJournalpostId())).get();
	}

	@Test
	public void shouldHaveArkivertAndReturnAJournalpost() throws Exception {
		setUpJoark();

		assertNotNull(persistedJournalpost);
	}

	@Test
	public void shouldVerifyPersistedJournalpostPropertiesAgaintInputValues() throws Exception {
		setUpJoark();

		assertJournalpostProperties(persistedJournalpost);
	}

	@Test
	public void shouldSaveAvsenderMottaker() throws Exception {
		Journalpost journalpost = createJournalpost(TEMAVALUE_PEN);
		journalpost.setEksternPart(createEksternPart());

		arkiverUstrukturertKravRequest.setJournalpost(journalpost);
		arkiverUstrukturertKravResponse = behandleJournalV3Provider
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest);

		persistedJournalpost = getPersistedJournalposterById(Long.parseLong(arkiverUstrukturertKravResponse
				.getJournalpostId()));

		assertThat(persistedJournalpost.getAvsenderMottaker(), is(NAVN));
		assertThat(persistedJournalpost.getAvsenderMottakerId(), is(ORGNUMMER));
	}

	@Test
	public void shouldFailInAvsenderMottakerValidation() throws Exception {
		Journalpost journalpost = createJournalpost(TEMAVALUE_PEN);
		EksternPart eksternPart = new EksternPart();
		eksternPart.setNavn(NAVN);
		journalpost.setEksternPart(eksternPart);

		arkiverUstrukturertKravRequest.setJournalpost(journalpost);

		assertThrows(ApplicationException.class,
				() -> arkiverUstrukturertKravResponse = behandleJournalV3Provider
						.arkiverUstrukturertKrav(arkiverUstrukturertKravRequest),
				"Journalpost.AvsenderMottakerId must be set when Journalpost.AvsenderMottaker is set");
	}

	@Test
	public void shouldVerifyThatPersistedDokumentMatchInputDokument() throws Exception {
		setUpJoark();

		assertDokumentSaved(persistedJournalpost);
	}

	@Test
	public void shouldVerifyThatPensjonAndForeldrepengerGetDifferentDokumentKategori() throws Exception {
		setUpJoark("PEN");

		DokumentInfo dokument = persistedJournalpost.findAllFilDetaljer().get(0).getDokumentInfo();
		assertNotNull(dokument);
		assertEquals(DokumentKategoriCode.IS, dokument.getKategori());

		setUpJoark("FOR");
		dokument = persistedJournalpost.findAllFilDetaljer().get(0).getDokumentInfo();
		assertNotNull(dokument);
		assertNull(dokument.getKategori());
	}

	@Test
	public void shouldReturnJournalpostIdAndDokumentId() throws Exception {
		setUpJoark();

		assertThat(arkiverUstrukturertKravResponse.getJournalpostId(), is(notNullValue()));
		assertThat(arkiverUstrukturertKravResponse.getDokumentId(), is(notNullValue()));
	}

	private void assertJournalpostProperties(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		assertThat(journalpost.getBrukere().iterator().next().getBrukerId(), is(FNR_BRUKER));
		assertThat(journalpost.getMottakskanal().name(), is(KANAL_ALTINN));
		assertThat(journalpost.getSignatur(), is(SIGNERT_TRUE));
		assertThat(journalpost.getFagomrade().name(), is(TEMAVALUE_FOR));
	}

	private void assertDokumentSaved(no.nav.dokarkiv.core.domain.entities.Journalpost journalpost) {
		String filUuid = journalpost.findAllFilDetaljer().get(0).getFilUuid();
		DokumentFil dokumentFil = dokumentFilRepository.findByFilUuid(filUuid);
		assertThat(dokumentFil.getFil(), is(DOKUMENT));
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
