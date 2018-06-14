package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_JOURNAL_S.
 * 
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public enum JournalStatusCode implements CtiRelationship<JournalStatusCode, String> {
	/**
	 * journalført
	 */
	J,
	/**
	 * midl journalført
	 */
	M,
	/**
	 * Utgår før tilknytn til sak
	 */
	U,
	/**
	 * Dokument under produksjon
	 */
	D,
	/**
	 * Reservert dokument
	 */
	R,
	/**
	 * Ferdig og sentral print
	 */
	FS,
	/**
	 * Ferdig og lokal print
	 */
	FL,
	/**
	 * Ekspedert
	 */
	E,
	/**
	 * Avbrutt
	 */
	A,
	/**
	 * Mottatt   
	 */
	MO,
	/**
	 * Ukjent bruker 
	 */
	UB,
	/** 
	 * Opplasting dokument 
	 */
	OD;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<JournalStatusCode, String>> getCtiClass() {
		return JournalStatusCti.class;
	}

}
