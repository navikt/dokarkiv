package no.nav.dokarkiv.arkiverdokumentmottak.utils;

import static no.nav.dokarkiv.arkiverdokumentmottak.ServiceConstants.FORSENDELSE_MOTTAK_ID_KEY;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.createTilleggsOpplysning;
import static no.nav.dokarkiv.arkiverdokumentmottak.utils.ArkiverDokumentmottakRequestDataUtil.populateFildetaljerBase;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.arkiverdokumentmottak.TilknyttetJournalpostEnum;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.DokumentInfo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Fildetaljer;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.Journalpost;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.informasjon.journalforinngaaendeforsendelse.JournalpostDokumentInfoRelasjon;

/**
 * Util class for creating a {@link no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest}
 *
 * @author Leo-Andreas Ervik, Visma Consulting. 21.02.2017
 */
public final class JournalforInngaaendeForsendelseRequestDataUtil {
	public static final String FORSENDELSE_MOTTA_VALUE = "forsendelse_motta_value";

	private JournalforInngaaendeForsendelseRequestDataUtil() {
	}

	public static Journalpost createJournalpost() throws Exception {
		Journalpost journalpost = new Journalpost();

		ArkiverDokumentmottakRequestDataUtil.populateJournalpostBase(journalpost);
		journalpost.setTema(ArkiverDokumentmottakRequestDataUtil.FAGOMRADE.name());
		journalpost.setJournalforendeEnhet(ArkiverDokumentmottakRequestDataUtil.JOURNALFOERENDE_ENHET_REF);
		journalpost.setInnhold(ArkiverDokumentmottakRequestDataUtil.INNHOLD);
		journalpost.setAvsenderMottakerNavn(ArkiverDokumentmottakRequestDataUtil.EKSTERNPART_NAVN);
		journalpost.withJournalpostDokumentInfoRelasjon(createJournalpostDokumentInfoRelasjon());
		journalpost.setSaksrelasjon(ArkiverDokumentmottakRequestDataUtil.createSaksrelasjon());
		journalpost.setBruker(ArkiverDokumentmottakRequestDataUtil.createBruker());
		journalpost.getJournalpostTilleggsopplysninger()
				.add(createTilleggsOpplysning(FORSENDELSE_MOTTAK_ID_KEY, FORSENDELSE_MOTTA_VALUE));
		journalpost.setDatoDokument(ArkiverDokumentmottakRequestDataUtil.toXMLGregorianCalendar(ArkiverDokumentmottakRequestDataUtil.DATO_DOKUMENT));

		return journalpost;
	}

	public static Fildetaljer addFildetaljer(VariantFormatCode variantFormatCode) {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, variantFormatCode);
		fildetaljer.setDokument(ArkiverDokumentmottakRequestDataUtil.BYTES);
		return fildetaljer;
	}

	private static Fildetaljer createFildetaljer() {
		Fildetaljer fildetaljer = new Fildetaljer();
		populateFildetaljerBase(fildetaljer, VariantFormatCode.valueOf(ArkiverDokumentmottakRequestDataUtil.VARIANTFORMAT));
		fildetaljer.setDokument(ArkiverDokumentmottakRequestDataUtil.DOKUMENT);
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
		jpdir.setTilknyttetJournalpostSom(ArkiverDokumentmottakRequestDataUtil.TILKNYTTET_JOURNALPOST_SOM_CODE);
		return jpdir;
	}

	private static DokumentInfo createDokumentInfo() {
		DokumentInfo dokumentInfo = new DokumentInfo();
		dokumentInfo.setTittel(ArkiverDokumentmottakRequestDataUtil.TITTEL);
		dokumentInfo.getFildetaljerListe().add(createFildetaljer());
		ArkiverDokumentmottakRequestDataUtil.populateDokumentInfoBase(dokumentInfo);
		return dokumentInfo;
	}

}