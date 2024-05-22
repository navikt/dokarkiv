package no.nav.dokarkiv.core.aksjonslogg;

import lombok.experimental.UtilityClass;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

@UtilityClass
public class ArkivElementConstants {

	public static final String DOKUMENT_INFO_KASSERT_DATO = "DokumentInfo.kassertDato";
	public static final String DOKUMENT_INFO_KASSERT = "DokumentInfo.kassert";
	public static final String DOKUMENT_INFO_KASSERT_AV = "DokumentInfo.kassertAv";
	public static final String DOKUMENT_INFO_DOKUMENT_INFO_ID = "DokumentInfo.dokumentInfoId";
	public static final String DOKUMENT_INFO_TITTEL = "DokumentInfo.tittel";
	public static final String DOKUMENT_INFO_BREVKODE = "DokumentInfo.brevkode";
	public static final String DOKUMENT_INFO_SENSITIVT = "DokumentInfo.sensitivt";

	public static final String JOURNALPOST_JOURNALPOST_ID = "Journalpost.journalpostId";
	public static final String JOURNALPOST_SKJERMING_TYPE = "Journalpost.skjermingType";
	public static final String JOURNALPOST_JOURNALSTATUS = "Journalpost.journalstatus";
	public static final String JOURNALPOST_FAGOMRADE = "Journalpost.fagomrade";
	public static final String JOURNALPOST_INNHOLD = "Journalpost.innhold";
	public static final String JOURNALPOST_BRUKER = "Journalpost.bruker";
	public static final String JOURNALPOST_AVSENDER_MOTTAKER = "Journalpost.avsend_mottaker";
	public static final String JOURNALPOST_AVSENDER_MOTTAKER_ID = "Journalpost.avsend_mottak_id";
	public static final String JOURNALPOST_JOURNALFORENDE_ENHET = "Journalpost.journalf_enhet";
	public static final String JOURNALPOST_OVERSTYR_INNSYN = "Journalpost.k_innsyn";

	public static final String RELASJON_DOKUMENT_INFO_ID = "JournalpostDokumentInfoRelasjon.dokumentInfoId";

	public static final String RELASJON_TILKNYTTET_SOM = "JournalpostDokumentInfoRelasjon.tilknyttetJournalpostSom";

	public static final String SAKSRELASJON_FAGSYSTEM = "Saksrelasjon.fagsystem";
	public static final String SAKSRELASJON_SAKID = "Saksrelasjon.sakId";

	public static final String SAK_FAGSAKNR = "Sak.fagsaknr";
	public static final String SAK_APPLIKASJON = "Sak.applikasjon";

	public static final String RELASJON_SKJERMING_TYPE = "JournalpostDokumentInfoRelasjon.skjermingType";

	public static final String FILDETALJER_VARIANTFORMAT = "FilDetaljer.variantFormat";
	public static final String FILDETALJER_FILUUID = "Fildetaljer.filUuid";

	public static final String DOKUMENT_FIL_FIL_UUID = "DokumentFil.filUuid";

	public static String fildetaljerSkjermingTypeVariant(VariantFormatCode variantFormatCode) {
		return String.format("Fildetaljer.variantFormat[%s].skjermingType", variantFormatCode);
	}

}
