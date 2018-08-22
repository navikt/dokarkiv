package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.DokumentInformasjonTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostRequestTo;
import no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark066.to.OppdaterJournalpostTo;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.UgyldigInputException;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.ArkivSak;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Avsender;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentinformasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Dokumentkategori;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.InngaaendeJournalpost;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Organisasjon;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Person;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leo-Andreas Ervik, Visma Consulting. 01.06.2017.
 */
public class OppdaterJournalpostRequestMapperTest {
	
	private static final String JOURNALPOST_ID = "1";
	private static final String INNHOLD = "INNHOLD";
	private static final String AVSENDER_MOTTAKERID = "***gammelt_fnr***";
	private static final String AVSENDER_MOTTAKER_NAVN = "Batman";
	private static final String ARKIV_SAK_FAGSYSTEM = FagsystemCode.PEN.name();
	private static final String ARKIV_SAKID = "1";
	private static final String HOVEDDOKUMENT_KATEGORI_KODE = DokumentKategoriCode.SOK.name();
	private static final String HOVEDDOKUMENT_DOKUMENT_INFO_ID = "1";
	private static final String HOVEDDOKUMENT_TITTEL = "HOVEDDOKUMENTTITTEL";
	private static final String VEDLEGG_DOKUMENT_INFO_ID = "9";
	private static final String VEDLEGG_TITTEL = "VEDLEGGTITTEL";
	private static final String FNR = "***gammelt_fnr***";
	private static final String ORGNR = "999999999";
	private static final Tema TEMA = new Tema();
	
	
	private OppdaterJournalpostRequestMapper mapper;
	private OppdaterJournalpostRequest request;
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		TEMA.setValue(FagomradeCode.PEN.name());
		mapper = new OppdaterJournalpostRequestMapper();
		request = new OppdaterJournalpostRequest();
		createRequest();
	}
	
	@Test
	public void shouldMap() throws Exception {
		OppdaterJournalpostRequestTo requestTo = mapper.map(request);
		OppdaterJournalpostTo oppdaterTo = requestTo.getOppdaterJournalpostTo();
		
		assertThat(oppdaterTo.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(oppdaterTo.getAvsenderTo().getAvsenderId(), is(AVSENDER_MOTTAKERID));
		assertThat(oppdaterTo.getAvsenderTo().getAvsenderNavn(), is(AVSENDER_MOTTAKER_NAVN));
		assertThat(oppdaterTo.getInnhold(), is(INNHOLD));
		assertThat(oppdaterTo.getArkivSak().getArkivSakId(), is(ARKIV_SAKID));
		assertThat(oppdaterTo.getArkivSak().getArkivSakSystem(), is(FagsystemCode.valueOf(ARKIV_SAK_FAGSYSTEM)));
		assertThat(oppdaterTo.getTema(), is(FagomradeCode.PEN));
		assertThat(oppdaterTo.getAktoerTo().getAktoerId(), is(FNR));
		assertThat(oppdaterTo.getAktoerTo().getBrukerTypeCode(), is(BrukerTypeCode.PERSON));
		
		assertThat(oppdaterTo.getHoveddokument().getDokumentId(), is(Long.valueOf(HOVEDDOKUMENT_DOKUMENT_INFO_ID)));
		assertThat(oppdaterTo.getHoveddokument().getTittel(), is(HOVEDDOKUMENT_TITTEL));
		assertThat(oppdaterTo.getHoveddokument().getDokumentkategori(), is(DokumentKategoriCode.valueOf(HOVEDDOKUMENT_KATEGORI_KODE)));
		
		assertThat(oppdaterTo.getVedlegg(), hasSize(5));
		
		for (int i = 0; i < oppdaterTo.getVedlegg().size(); i++) {
			DokumentInformasjonTo to = oppdaterTo.getVedlegg().get(i);
			assertThat(to.getDokumentId(), is(Long.valueOf(VEDLEGG_DOKUMENT_INFO_ID + i)));
			assertThat(to.getTittel(), is(VEDLEGG_TITTEL + i));
			assertThat(to.getDokumentkategori(), is(DokumentKategoriCode.values()[i]));
		}
	}
	
	@Test
	public void shouldMapOrganisasjon() throws Exception {
		Organisasjon organisasjon = new Organisasjon();
		organisasjon.setOrganisasjonsnummer(ORGNR);
		
		request.getInngaaendeJournalpost().setBruker(organisasjon);
		OppdaterJournalpostRequestTo requestTo = mapper.map(request);
		
		assertThat(requestTo.getOppdaterJournalpostTo().getAktoerTo().getAktoerId(), is(ORGNR));
		assertThat(requestTo.getOppdaterJournalpostTo().getAktoerTo().getBrukerTypeCode(), is(BrukerTypeCode.ORGANISASJON));
	}
	
	@Test
	public void shouldFailOnMissingInngaaendeJournal() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("OppdaterJournalpostRequest.InngaaendeJournal kan ikke være null");
		
		request.setInngaaendeJournalpost(null);
		mapper.map(request);
	}
	
	@Test
	public void shouldNotFailOnNullObjects() throws Exception {
		request.getInngaaendeJournalpost().setBruker(null);
		request.getInngaaendeJournalpost().setHoveddokument(null);
		request.getInngaaendeJournalpost().setArkivSak(null);
		request.getInngaaendeJournalpost().setAvsender(null);
		request.getInngaaendeJournalpost().setTema(null);
		
		mapper.map(request);
	}
	
	@Test
	public void shouldFailOnMissingArkivSakIdObject() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler id på ArkivSak i request for å oppdatere journalpost. journalpostId=");
		
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakSystem("U");
		request.getInngaaendeJournalpost().setArkivSak(arkivSak);
		
		mapper.map(request);
	}
	
	@Test
	public void shouldFailOnMissingArkivSakSystemObject() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler fagsystem på ArkivSak i request for å oppdatere journalpost. journalpostId=");
		
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakId("123");
		request.getInngaaendeJournalpost().setArkivSak(arkivSak);
		
		mapper.map(request);
	}
	
	@Test
	public void shouldFailOnMissingDokumentIdObject() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler DokumentId på Dokument i request for å oppdatere journalpost. journalpostId=");
		
		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setKodeverksRef("SOK");
		dokumentinformasjon.setDokumentkategori(dokumentkategori);
		dokumentinformasjon.setTittel("TITLE");
		
		request.getInngaaendeJournalpost().setHoveddokument(dokumentinformasjon);
		
		mapper.map(request);
	}
	
	@Test
	public void shouldFailOnMissingAktoerIdPerson() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler Ident på Aktoer i request for å oppdatere journalpost. journalpostId=");
		
		request.getInngaaendeJournalpost().setBruker(new Person());
		
		mapper.map(request);
	}
	
	@Test
	public void shouldFailOnMissingAktoerIdOrganisasjon() throws Exception {
		expected.expect(UgyldigInputException.class);
		expected.expectMessage("Mangler Organisasjonsnummer på Aktoer i request for å oppdatere journalpost. journalpostId=");
		
		request.getInngaaendeJournalpost().setBruker(new Organisasjon());
		
		mapper.map(request);
	}
	
	private void createRequest() {
		InngaaendeJournalpost inngaaendeJournalpost = new InngaaendeJournalpost();
		
		inngaaendeJournalpost.setJournalpostId(JOURNALPOST_ID);
		inngaaendeJournalpost.setInnhold(INNHOLD);
		inngaaendeJournalpost.setTema(TEMA);
		
		Avsender avsender = new Avsender();
		avsender.setAvsenderId(AVSENDER_MOTTAKERID);
		avsender.setAvsenderNavn(AVSENDER_MOTTAKER_NAVN);
		inngaaendeJournalpost.setAvsender(avsender);
		
		ArkivSak arkivSak = new ArkivSak();
		arkivSak.setArkivSakId(ARKIV_SAKID);
		arkivSak.setArkivSakSystem(ARKIV_SAK_FAGSYSTEM);
		inngaaendeJournalpost.setArkivSak(arkivSak);
		
		Person person = new Person();
		person.setIdent(FNR);
		inngaaendeJournalpost.setBruker(person);
		
		Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
		Dokumentkategori dokumentkategori = new Dokumentkategori();
		dokumentkategori.setValue(HOVEDDOKUMENT_KATEGORI_KODE);
		dokumentinformasjon.setDokumentkategori(dokumentkategori);
		dokumentinformasjon.setDokumentId(HOVEDDOKUMENT_DOKUMENT_INFO_ID);
		dokumentinformasjon.setTittel(HOVEDDOKUMENT_TITTEL);
		inngaaendeJournalpost.setHoveddokument(dokumentinformasjon);
		
		for (Dokumentinformasjon vedleggInfo : createDokumentInformasjon()) {
			inngaaendeJournalpost.getVedleggListe().add(vedleggInfo);
		}
		
		request.setInngaaendeJournalpost(inngaaendeJournalpost);
	}
	
	private List<Dokumentinformasjon> createDokumentInformasjon() {
		ArrayList<Dokumentinformasjon> dokInfoList = new ArrayList<>();
		
		for(int i = 0; i < 5; i++) {
			Dokumentinformasjon dokumentinformasjon = new Dokumentinformasjon();
			Dokumentkategori dokumentkategori = new Dokumentkategori();
			dokumentkategori.setValue(DokumentKategoriCode.values()[i].name());
			dokumentinformasjon.setDokumentkategori(dokumentkategori);
			dokumentinformasjon.setDokumentId(VEDLEGG_DOKUMENT_INFO_ID + i);
			dokumentinformasjon.setTittel(VEDLEGG_TITTEL + i);
			dokInfoList.add(dokumentinformasjon);
		}
		
		return dokInfoList;
	}
}