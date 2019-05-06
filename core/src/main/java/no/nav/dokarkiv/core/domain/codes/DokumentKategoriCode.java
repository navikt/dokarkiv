package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_KATEGORI_T.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public enum DokumentKategoriCode implements CtiRelationship<DokumentKategoriCode, String> {

	/**
	 * Brev
	 */
	B,
	/**
	 * Vedtaksbrev
	 */
	VB,
	/**
	 * Infobrev
	 */
	IB,
	/**
	 * Elektronisk skjema
	 */
	ES,
	/**
	 * Tolkbart skjema
	 */
	TS,
	/**
	 * Ikke tolkbart skjema
	 */
	IS,
	/**
	 * Konverterte data fra system
	 */
	KS,
	/**
	 * Konvertert fra elektronisk arkiv
	 */
	KD,
	/**
	 * Strukturert elektronisk dokument
	 */
	SED,
	/**
	 * Publikumsblankett EØS
	 */
	PUBL_BLANKETT_EOS,
	/**
	 * Elektronisk dialog (brukerdialog)
	 */
	ELEKTRONISK_DIALOG,
	/**
	 * Referat (brukerdialog)
	 */
	REFERAT,

	/**
	 * Referat fra samtale med bruker
	 */
	FORVALTNINGSNOTAT,

	/**
	 * Søknad
	 */
	SOK,

	/**
	 * Klage eller anke
	 */
	KA;

	@Override
	public Class<? extends CodesTablePeriodicItem<DokumentKategoriCode, String>> getCtiClass() {
		return DokumentKategoriCti.class;
	}

}
