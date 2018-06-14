package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_TILKN_JP_SOM.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum TilknyttetJournalpostSomCode implements CtiRelationship<TilknyttetJournalpostSomCode, String> {

	/** Hoveddokument */
	HOVEDDOKUMENT,
	/** Vedlegg */
	VEDLEGG,
	/** Sammensatt dokument */
	SAMMENSATT_DOK;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<TilknyttetJournalpostSomCode, String>> getCtiClass() {
		return TilknyttetJournalpostSomCti.class;
	}

}
