package no.nav.dokarkiv.behandlejournal.v2.datautil;

import no.nav.dokarkiv.behandlejournal.v2.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Kommunikasjonskanaler;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.Journalpost;

/**
 * Util for creating Journalpost for the JournalfoerInngaaendeHenvendelse operation
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class JournalfoerInngaaendeHenvendelseDataUtil extends BehandleJournalCommonDataUtil {

	public static final FagomradeCode ARKIVTEMA = FagomradeCode.UFO;
	public static final MottaksKanalCode KANAL = MottaksKanalCode.ALTINN;
	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final boolean BEGRENSET_PARTS_INNSYN = true;
	public static final String JOURNALFOERENDE_ENHET_REF = "2009";
	public static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	public static final String OPPRETTET_AV_NAVN = "Donald Duck";
	public static final boolean SENSITIVITET = true;
	public static final String TITTEL = "The Sound of Music";
	public static final DokumentKategoriCode KATEGORI = DokumentKategoriCode.B;
	public static final String INNHOLD = "Sanger fra verden";

	public static Journalpost createJournalpost() throws Exception {
		Kommunikasjonskanaler kanal = new Kommunikasjonskanaler();
		kanal.setValue(KANAL.name());
		Journalpost journalpost = new Journalpost();
		journalpost.setArkivtema(KodeverdiHelper.kodeVerdi(ARKIVTEMA.name(), Arkivtemaer.class));
		journalpost.setMottattDato(getToday());
		journalpost.setDokumentDato(getToday());
		journalpost.setKanal(kanal);
		journalpost.setSignatur(createSignatur());
		journalpost.setJournalfoerendeEnhetREF(JOURNALFOERENDE_ENHET_REF);
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setInnhold(INNHOLD);
		journalpost.setGjelderSak(createSak());
		journalpost.getForBruker().add(createPerson());
		journalpost.setEksternPart(createEksternPart());
		journalpost.getKryssreferanseListe().add(createKryssreferanse());
		journalpost.getDokumentinfoRelasjon().add(createDokumentinfoRelasjon());
		return journalpost;
	}

	private static DokumentinfoRelasjon createDokumentinfoRelasjon() {
		DokumentinfoRelasjon dokumentinfoRelasjon = new DokumentinfoRelasjon();
		dokumentinfoRelasjon.setTillknyttetJournalpostSomKode(HOVEDDOKUMENT);
		dokumentinfoRelasjon.setJournalfoertDokument(createJournalfoertDokumentInfo());
		return dokumentinfoRelasjon;
	}

	private static JournalfoertDokumentInfo createJournalfoertDokumentInfo() {
		JournalfoertDokumentInfo dokInfo = new JournalfoertDokumentInfo();
		dokInfo.setDokumentType(KodeverdiHelper.kodeVerdi(DOKUMENT_TYPE_ID, Dokumenttyper.class));
		dokInfo.setSensitivitet(SENSITIVITET);
		dokInfo.setBegrensetPartsInnsyn(BEGRENSET_PARTS_INNSYN);
		dokInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		dokInfo.setTittel(TITTEL);
		dokInfo.setKategorikode(KATEGORI.name());
		dokInfo.getBeskriverInnhold().add(createUstrukurertInnhold());
		return dokInfo;
	}

}
