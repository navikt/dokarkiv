package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Enum for codes in T_K_DOKUMENT_S.
 *
 * @author Per Kristian Foss, Visma Sirius
 */
public enum DokumentStatusCode implements CtiRelationship<DokumentStatusCode, String> {

	/**
	 * Dokumentet er under redigering
	 */
	UNDER_REDIGERING,
	/**
	 * Dokumentet er ferdigstilt
	 */
	FERDIGSTILT,
	/**
	 * Dokumentet er avbrutt
	 */
	AVBRUTT;

	/** {@inheritDoc} */
	@Override
	public Class<? extends CodesTablePeriodicItem<DokumentStatusCode, String>> getCtiClass() {
		return DokumentStatusCti.class;
	}
}
