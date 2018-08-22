package no.nav.dokarkiv.behandlejournal.v3.datautil;

import no.nav.dokarkiv.behandlejournal.v3.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Kommunikasjonskanaler;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerutgaaendehenvendelse.DokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerutgaaendehenvendelse.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.journalfoerutgaaendehenvendelse.Journalpost;

/**
 * Util for creating Journalpost for the JournalfoerUtgaaendeHendelse operation
 *
 * @author Stig Strøm
 */
public class JournalfoerUtgaaendeHenvendelseDataUtil extends BehandleJournalCommonDataUtil {

	public static final FagomradeCode ARKIVTEMA = FagomradeCode.UFO;
	public static final UtsendingsKanalCode KANAL = UtsendingsKanalCode.ALTINN;
	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final boolean BEGRENSET_PARTS_INNSYN = true;
	public static final String JOURNALFOERENDE_ENHET_REF = "2009";
	public static final String HOVEDDOKUMENT = TilknyttetJournalpostSomCode.HOVEDDOKUMENT.name();
	public static final String OPPRETTET_AV_FORNAVN = "Bjarne";
	public static final String OPPRETTET_AV_ETTERNAVN = "Betjent";
	public static final String OPPRETTET_AV_NAVN = OPPRETTET_AV_FORNAVN + " " + OPPRETTET_AV_ETTERNAVN;
	public static final boolean SENSITIVITET = true;
	public static final String TITTEL = "The Sound of Music";
	public static final DokumentKategoriCode KATEGORI = DokumentKategoriCode.B;
	public static final String BREVKODE = "TSoM";
	public static final String INNHOLD = "Sanger fra verden";

	public static Journalpost creatJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();
		journalpost.setArkivtema(KodeverdiHelper.kodeVerdi(ARKIVTEMA.name(), Arkivtemaer.class));
		journalpost.setDokumentDato(getToday());
		journalpost.setSignatur(createSignatur());
		journalpost.setJournalfoerendeEnhetREF(JOURNALFOERENDE_ENHET_REF);
		journalpost.setOpprettetAvNavn(OPPRETTET_AV_NAVN);
		journalpost.setInnhold(INNHOLD);
		journalpost.setDistribusjonAvJournal(createJournaldistribusjon());
		journalpost.setGjelderSak(createSak());
		journalpost.getKryssreferanseListe().add(createKryssreferanse());
		journalpost.getDokumentinfoRelasjon().add(createDokumentinfoRelasjon());
		journalpost.getForBruker().add(createPerson());
		journalpost.setEksternPart(createEksternPart());
		journalpost.setKanal(KodeverdiHelper.kodeVerdi(KANAL.name(), Kommunikasjonskanaler.class));
		journalpost.setDatoEkspedert(getToday());
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
