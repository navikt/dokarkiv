package no.nav.dokarkiv.behandlejournal.v2.datautil;

import no.nav.dokarkiv.behandlejournal.v2.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.util.DateProvider;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoernotat.Journalpost;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

/**
 * Util for creating Journalpost for the JournalfoerNotatHenvendelse operation
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerNotatHenvendelseDataUtil extends BehandleJournalCommonDataUtil {

	public static final String ARKIVTEMA = FagomradeCode.UFO.name();
	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final boolean BEGRENSET_PARTS_INNSYN = true;
	public static final String JOURNALFOERENDE_ENHET_REF = "2009";
	public static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	public static final String OPPRETTET_AV_NAVN = "Max Mekker";
	public static final boolean SENSITIVITET = true;
	public static final String TITTEL = "The Sound of Music";
	public static final String KATEGORI = DokumentKategoriCode.B.name();
	public static final boolean ORGANINTERNT = true;
	public static final String INNHOLD = "Sanger fra verden";

	public static Journalpost createJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();
		journalpost.setArkivtema(KodeverdiHelper.kodeVerdi(ARKIVTEMA, Arkivtemaer.class));
		journalpost.setDokumentDato(getTodayJodaTime());
		journalpost.setSignatur(createSignatur());
		journalpost.setJournalfoerendeEnhetREF(JOURNALFOERENDE_ENHET_REF);
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setInnhold(INNHOLD);
		journalpost.setGjelderSak(createSak());
		journalpost.getForBruker().add(createPerson());
		journalpost.getKryssreferanseListe().add(createKryssreferanse());
		journalpost.getDokumentinfoRelasjon().add(createDokumentinfoRelasjon());
		return journalpost;
	}

	private static DokumentinfoRelasjon createDokumentinfoRelasjon() throws Exception {
		DokumentinfoRelasjon dokumentinfoRelasjon = new DokumentinfoRelasjon();
		dokumentinfoRelasjon.setTillknyttetJournalpostSomKode(HOVEDDOKUMENT);
		dokumentinfoRelasjon.setJournalfoertDokument(createJournalfoertDokumentInfo());
		return dokumentinfoRelasjon;
	}

	private static JournalfoertDokumentInfo createJournalfoertDokumentInfo() throws Exception {
		JournalfoertDokumentInfo dokInfo = new JournalfoertDokumentInfo();
		dokInfo.setDokumentType(KodeverdiHelper.kodeVerdi(DOKUMENT_TYPE_ID, Dokumenttyper.class));
		dokInfo.setFerdigDato(getTodayJodaTime());
		dokInfo.setSensitivitet(SENSITIVITET);
		dokInfo.setBegrensetPartsInnsyn(BEGRENSET_PARTS_INNSYN);
		dokInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		dokInfo.setTittel(TITTEL);
		dokInfo.setKategorikode(KATEGORI);
		dokInfo.setErOrganinternt(ORGANINTERNT);
		dokInfo.getBeskriverInnhold().add(createUstrukurertInnhold());
		dokInfo.setFerdigDato(getTodayJodaTime());
		return dokInfo;
	}

}
