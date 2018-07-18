package no.nav.dokarkiv.core.stelvio;

/**
 * Interface to be implemented by Enums that represent Codes in CodesTables that have
 * values that can be represented directly by an Enum.
 *
 * <p>
 * An example of an Enum that can't directly represent a codestable code is the enum
 * for postal code (zip code). The enum instance Postnr.0116 is illegal. By implementing
 * this interface and returning the code in getIllegalCode, the actual name of the
 * Enum instance can be anything. (IE: Postnr.P0116)
 * </p>
 *
 * @author Morten Andersen-Gott (Accenture)
 * @deprecated brukes ikke
 *
 */
@Deprecated
public interface IllegalCodeEnum {

	/**
	 * Returns the code of an Enum that is on a form that makes it illegal to use as an Enum
	 * value. (IE: Starting with special characters)

	 * @return the illegal code
	 */
	String getIllegalCode();

}
