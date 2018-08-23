package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when either a FilDetaljer or DokumentFil cannot be found by filUuid.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class InvalidFilUuidException extends DokarkivFunctionalException {
	
	/** Serialization UID */
	private static final long serialVersionUID = ***gammelt_fnr***32278339L;

	private final String filUuid;
	
	/**
	 * Constructs a new InvalidFilUuidException.
	 *
	 * @param message The exception message.
	 * @param filUuid The invalid filUuid.
	 */
	public InvalidFilUuidException(String message, String filUuid) {
		super(message);
		this.filUuid = filUuid;
	}

	/**
	 * Getter for the filUuid property.
	 *
	 * @return the filUuid
	 */
	public String getFilUuid() {
		return filUuid;
	}
}
