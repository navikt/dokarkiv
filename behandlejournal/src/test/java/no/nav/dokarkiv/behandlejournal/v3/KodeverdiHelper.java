package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.informasjon.behandlejournal.Kodeverdi;

/**
 * Helper class for easing the use of Kodeverdi
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public class KodeverdiHelper {

	/**
	 * Returns an instance of a Kodeverdi subtype, with the value set.
	 * 
	 * @param value The code to be set in value
	 * @param clazz The class of the Kodeverdi subtype
	 * @return Instance of Kodeverdi subtype with value set
	 */
	public static <T extends Kodeverdi> T kodeVerdi(String value, Class<T> clazz) {
		T kodeverdi = null;
		try {
			kodeverdi = clazz.newInstance();
		} catch (IllegalAccessException e) {
			throw new ApplicationException("Creating subtype of Kodeverdi failed", e);
		} catch (InstantiationException e) {
			throw new ApplicationException("Creating subtype of Kodeverdi failed", e);
		}
		kodeverdi.setValue(value);
		return kodeverdi;
	}

}
