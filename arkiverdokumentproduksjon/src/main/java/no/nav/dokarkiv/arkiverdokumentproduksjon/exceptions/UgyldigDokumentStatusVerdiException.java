package no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.stelvio.FunctionalRecoverableException;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Exception that is thrown when there is an illegal DokumentStatus.
 * 
 * @author Thao Thanh Nguyen, Visma Consulting
 */
public class UgyldigDokumentStatusVerdiException extends FunctionalRecoverableException {
	
	/** Unique serial version id. */
	private static final long serialVersionUID = ***gammelt_fnr***0054658L;

	private final DokumentStatusCode dokumentStatus;
	
	/**
	 * Constructs a new UgyldigDokumentStatusVerdiException.
	 * 
	 * @param message
	 *            The exception message.
	 * @param dokumentStatus
	 *            The invalid status.
	 */
	public UgyldigDokumentStatusVerdiException(String message, DokumentStatusCode dokumentStatus) {
		super(message);
		this.dokumentStatus = dokumentStatus;
	}

	/**
	 * getter for the dokumentStatus property.
	 *
	 * @return the dokumentStatus
	 */
	public DokumentStatusCode getDokumentStatus() {
		return dokumentStatus;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.appendSuper(super.toString());
		builder.append("dokumentStatus", dokumentStatus);
		return builder.toString();
	}
	
}
