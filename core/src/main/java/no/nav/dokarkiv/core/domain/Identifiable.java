package no.nav.dokarkiv.core.domain;

/**
 * Used in order to mark and treat an entity as identifiable.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public interface Identifiable {

	/**
	 * Returns an id for an item/entity.
	 *
	 * @return The unique identification (amongst enitites of same type).
	 */
	Long getId();

}
