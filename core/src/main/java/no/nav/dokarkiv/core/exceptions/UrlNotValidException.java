package no.nav.dokarkiv.core.exceptions;

import no.nav.dokarkiv.core.domain.entities.DokumentUrlInfo;
import no.nav.dokarkiv.core.stelvio.FunctionalUnrecoverableException;

/**
 * Thrown in cases where an Url has exceeded its TTL.
 *
 * @author Magnus Skuland, Sirius IT
 * @author Eirik Bergande, Sirius IT
 */
public class UrlNotValidException extends FunctionalUnrecoverableException {

	/**
	 * ID used for serialization.
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Message string.
	 */
	private static final String MSG = "The time to live is exceeded for the DokumentUrlInfo with identifier: ";

	private DokumentUrlInfo dokumentUrlInfo;

	/**
	 * Constructs a new UrlNotValidException.
	 *
	 * @param dokumentUrlInfo The DokumentUrlInfo.
	 */
	public UrlNotValidException(DokumentUrlInfo dokumentUrlInfo) {
		super(MSG + dokumentUrlInfo.getDokumentUrlInfoId());
		this.dokumentUrlInfo = dokumentUrlInfo;
	}

	/**
	 * Getter for the dokumentUrlInfo property.
	 *
	 * @return the dokumentUrlInfo
	 */
	public DokumentUrlInfo getDokumentUrlInfo() {
		return dokumentUrlInfo;
	}

}

