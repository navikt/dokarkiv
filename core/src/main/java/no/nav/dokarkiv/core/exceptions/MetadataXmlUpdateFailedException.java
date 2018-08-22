package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Thrown when updating metadata xml in SettMetadataIDLF fails.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class MetadataXmlUpdateFailedException extends FunctionalUnrecoverableException {

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
