package no.nav.dokarkiv.core.stelvio;

/**
 * Internal helper class to be used only by Stelvio.
 *
 * @author Stig Kleppe-Jørgensen, Accenture
 */
@Deprecated
public class Internal {

	/**
	 * Creates a new instance of Internal.
	 */
	protected Internal() {

	}

	/**
	 * Internal helper method for doing unchecked casts. Before using this, try to rewrite so the cast is not needed anymore. If
	 * that is not possible, make sure that the cast will always be sound.
	 *
	 * @param <T>
	 *            a type variable
	 * @param objectToCast
	 *            the object that will be cast.
	 * @return an unchecked cast of the specified object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T cast(Object objectToCast) {
		return (T) objectToCast;
	}
}
