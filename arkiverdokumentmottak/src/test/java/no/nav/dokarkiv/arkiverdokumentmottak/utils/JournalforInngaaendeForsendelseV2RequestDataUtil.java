package no.nav.dokarkiv.arkiverdokumentmottak.utils;

import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.createSkannetInnhold;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakV2RequestDataUtil.populateFildetaljerBase;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.JournalpostDokumentInfoRelasjon;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;

/**
 * Util class for creating a {@link no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest} (TJOARK203)
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
public class JournalforInngaaendeForsendelseV2RequestDataUtil {

	private JournalforInngaaendeForsendelseV2RequestDataUtil() {
	}

	public static Journalpost createJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();

		ArkiverDokumentmottakV2RequestDataUtil.populateJournalpostBase(journalpost);
		journalpost.setTema(ArkiverDokumentmottakV2RequestDataUtil.FAGOMRADE.name());
		journalpost.setJournalforendeEnhet(ArkiverDokumentmottakV2RequestDataUtil.JOURNALFOERENDE_ENHET_REF);
		journalpost.setInnhold(ArkiverDokumentmottakV2RequestDataUtil.INNHOLD);
		journalpost.setAvsenderMottakerNavn(ArkiverDokumentmottakV2RequestDataUtil.EKSTERNPART_NAVN);
		journalpost.setKanalReferanseId(ArkiverDokumentmottakV2RequestDataUtil.KANALREFERANSE_ID);
		journalpost.setDatoDokument(ArkiverDokumentmottakV2RequestDataUtil.toXMLGregorianCalendar(ArkiverDokumentmottakV2RequestDataUtil.DATO_DOKUMENT));
		journalpost.setSaksrelasjon(ArkiverDokumentmottakV2RequestDataUtil.createSaksrelasjon());
		journalpost.setBruker(ArkiverDokumentmottakV2RequestDataUtil.createBruker());
		journalpost.withJournalpostDokumentInfoRelasjon(createJournalpostDokumentInfoRelasjon());

		return journalpost;
	}

	public static Fildetaljer addFildetaljer(VariantFormatCode variantFormatCode) {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, variantFormatCode);
		fildetaljer.setDokument(ArkiverDokumentmottakV2RequestDataUtil.BYTES);
		return fildetaljer;
	}

	private static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, VariantFormatCode.valueOf(ArkiverDokumentmottakV2RequestDataUtil.VARIANTFORMAT));
		fildetaljer.setDokument(ArkiverDokumentmottakV2RequestDataUtil.DOKUMENT);
		return fildetaljer;
	}

	public static JournalpostDokumentInfoRelasjon addVedlegg() {
		JournalpostDokumentInfoRelasjon jpdir = new JournalpostDokumentInfoRelasjon();
		jpdir.setDokumentInfo(createDokumentInfo());
		jpdir.setTilknyttetJournalpostSom(TilknyttetJournalpostEnum.VEDLEGG);
		return jpdir;
	}

	private static JournalpostDokumentInfoRelasjon createJournalpostDokumentInfoRelasjon() {
		JournalpostDokumentInfoRelasjon jpdir = new JournalpostDokumentInfoRelasjon();
		jpdir.setDokumentInfo(createDokumentInfo());
		jpdir.setTilknyttetJournalpostSom(ArkiverDokumentmottakV2RequestDataUtil.TILKNYTTET_JOURNALPOST_SOM_CODE);
		return jpdir;
	}

	private static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setTittel(ArkiverDokumentmottakV2RequestDataUtil.TITTEL);
		dokumentInfo.getFildetaljerListe().add(createFildetaljer());
		dokumentInfo.getSkannetInnholdListe().add(createSkannetInnhold());
		ArkiverDokumentmottakV2RequestDataUtil.populateDokumentInfoBase(dokumentInfo);
		return dokumentInfo;
	}
}
