package no.nav.dokarkiv.core.domain.codes;

import no.nav.dokarkiv.core.stelvio.CodesTablePeriodicItem;

/**
 * Used to associate an Enum with a Codestable Item class.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 * @param <K> The Enum code type
 * @param <V> The code's decode type
 */
public interface CtiRelationship<K extends Enum<K>, V> {

	/**
	 * Get the corresponding Codestable item class for an Enum.
	 * 
	 * @return The cti class
	 */
	Class<? extends CodesTablePeriodicItem<K, V>> getCtiClass();

}
