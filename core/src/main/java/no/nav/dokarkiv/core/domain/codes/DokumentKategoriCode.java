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
	 * E-post
	 */
	EP,
	/**
	 * Faktura
	 */
	F,
	/**
	 * Konverterte data fra system
	 */
	KS,
	/**
	 * Konvertert fra elektronisk arkiv
	 */
	KD,
	/**
	 * Konvertert fra papirarkiv (skannet)
	 */
	KM,
	/**
	 * Strukturert elektronisk dokument
	 */
	SED,
	/**
	 * SystemSED
	 */
	SYS_SED,
	/**
	 * Publikumsblankett EØS
	 */
	PUBL_BLANKETT_EOS,
	/**
	 * E-blankett
	 */
	E_BLANKETT,
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
