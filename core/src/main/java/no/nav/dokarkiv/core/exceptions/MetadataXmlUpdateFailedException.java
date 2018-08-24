package no.nav.dokarkiv.core.exceptions;

/**
 * Thrown when updating metadata xml in SettMetadataIDLF fails.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class MetadataXmlUpdateFailedException extends DokarkivFunctionalException {

	/**
	 * Serialization UID
	 */
	private static final long serialVersionUID = -***gammelt_fnr***98920183L;

	/**
	 * Constructs a new MetadataXmlUpdateFailedException.
	 *
	 * @param cause The exception cause.
	 */
	public MetadataXmlUpdateFailedException(Throwable cause) {
		super("Update of metadata xml failed", cause);
	}

}
