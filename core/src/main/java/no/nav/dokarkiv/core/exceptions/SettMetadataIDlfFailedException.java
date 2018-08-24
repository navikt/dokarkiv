package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when an internal call to settMetadataIDLF fails.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class SettMetadataIDlfFailedException extends DokarkivFunctionalException {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = ***gammelt_fnr***45624652L;

	/**
	 * Constructs a new SettMetadataIDlfFailedException.
	 *
	 * @param cause The cause of exception
	 */
	public SettMetadataIDlfFailedException(Throwable cause) {
		super("Could not update DLF with metadata: " + cause.getMessage(), cause);
	}
}
