package no.nav.dokarkiv.hentdokument.exceptions;

import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Exception that is thrown when no DokumentUrlInfo could be found for a given doc
 * token value. This exception is checked.
 * 
 * @author Magnus Skuland, Sirius IT
 */
public class DokumentUrlNotFoundException extends FunctionalUnrecoverableException {

	/** Serialization ID. */
	private static final long serialVersionUID = 1L;

	private String docToken;
	/** Error message. */
	private static final String MSG = "No DokumentUrl entry could be found for docToken: ";

	/**
	 * Constructs a {@link DokumentUrlNotFoundException} using the given
	 * docToken.
	 * 
	 * @param docToken
	 *            The doc token.
	 */
	public DokumentUrlNotFoundException(String docToken) {
		super(MSG + docToken);
		this.docToken = docToken;
	}

	/**
	 * Getter for the docToken property.
	 * 
	 * @return the docToken
	 */
	public String getDocToken() {
		return docToken;
	}

}
