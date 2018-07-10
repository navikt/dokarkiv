package no.nav.dokarkiv.behandlejournal.v2.datautil;

import no.nav.dokarkiv.behandlejournal.v2.KodeverdiHelper;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.JournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.arkiverustrukturertkrav.Journalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Arkivtemaer;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Dokumenttyper;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.Kommunikasjonskanaler;

/**
 * Util for creating Journalpost for the ArkiverUstrukturertKrav operation
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class ArkiverUstrukturertKravJournalpostDataUtil extends BehandleJournalCommonDataUtil {

	public static final String ARKIVTEMA = FagomradeCode.UFO.name();
	public static final String KANAL = MottaksKanalCode.ALTINN.name();
	public static final String DOKUMENT_TYPE_ID = "dokumentTypeId";
	public static final boolean BEGRENSET_PARTS_INNSYN = true;
	public static final String JOURNALFOERENDE_ENHET_REF = "2009";

	public static Journalpost createJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();
		journalpost.getForBruker().add(createPerson());
		journalpost.setArkivtema(KodeverdiHelper.kodeVerdi(ARKIVTEMA, Arkivtemaer.class));
		journalpost.setKanal(KodeverdiHelper.kodeVerdi(KANAL, Kommunikasjonskanaler.class));
		journalpost.setSignatur(createSignatur());
		journalpost.setDokumentDato(getToday());
		journalpost.setMottattDato(getToday());
		journalpost.setJournalfoerendeEnhetREF(JOURNALFOERENDE_ENHET_REF);
		journalpost.setJournalfoertDokument(createJournalfoertDokumentInfo());
		return journalpost;
	}

	private static JournalfoertDokumentInfo createJournalfoertDokumentInfo() {
		JournalfoertDokumentInfo dokInfo = new JournalfoertDokumentInfo();
		dokInfo.setBegrensetPartsInnsyn(BEGRENSET_PARTS_INNSYN);
		dokInfo.setDokumentType(KodeverdiHelper.kodeVerdi(DOKUMENT_TYPE_ID, Dokumenttyper.class));
		dokInfo.setTilleggsopplysninger(createTilleggsopplysninger());
		dokInfo.getBeskriverInnhold().add(createUstrukurertInnhold());
		return dokInfo;
	}
}
