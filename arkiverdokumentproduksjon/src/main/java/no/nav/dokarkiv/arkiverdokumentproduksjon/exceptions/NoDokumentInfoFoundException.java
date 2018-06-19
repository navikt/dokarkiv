package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Exception used when no DokumentInfo is found for a given dokumentInfoId.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class NoDokumentInfoFoundException extends FunctionalRecoverableException {

	/** Serialization UID */
	private static final long serialVersionUID = ***gammelt_fnr***36396074L;

	private Long dokumentInfoId;
	
	/**
	 * Constructs a new NoDokumentInfoFoundException.
	 *
	 * @param message The exception message.
	 * @param dokumentInfoId The dokumentInfoId.
	 */
	public NoDokumentInfoFoundException(String message, Long dokumentInfoId) {
		super(message);
		this.dokumentInfoId = dokumentInfoId;
	}

	/**
	 * Getter for the dokumentInfoId property.
	 *
	 * @return the dokumentInfoId
	 */
	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("dokumentInfoId", dokumentInfoId);
		return builder.toString();
	}
	
}
