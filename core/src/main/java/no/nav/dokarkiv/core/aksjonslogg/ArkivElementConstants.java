package no.nav.dokarkiv.core.aksjonslogg;

import lombok.experimental.UtilityClass;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@UtilityClass
public class ArkivElementConstants {

	public static final String DOKUMENT_INFO_KASSERT_DATO = "DokumentInfo.kassertDato";
	public static final String DOKUMENT_INFO_KASSERT_AV = "DokumentInfo.kassertAv";

	public static final String DOKUMENT_INFO_DOKUMENT_INFO_ID = "DokumentInfo.dokumentInfoId";
	public static final String JOURNALPOST_JOURNALPOST_ID= "Journalpost.journalpostId";
	public static final String RELASJON_DOKUMENT_INFO_ID= "JournalpostDokumentInfoRelasjon.dokumentInfoId";

	public static final String JOURNALPOST_SKJERMING_TYPE= "Journalpost.skjermingType";
	public static final String RELASJON_SKJERMING_TYPE= "JournalpostDokumentInfoRelasjon.skjermingType";

	public static final String FILDETALJER_VARIANTFORMAT = "FilDetaljer.variantFormat";
}
