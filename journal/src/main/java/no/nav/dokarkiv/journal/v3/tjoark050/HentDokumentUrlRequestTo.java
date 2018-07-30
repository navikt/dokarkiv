package no.nav.dokarkiv.journal.v3.tjoark050;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;

/**
 * Request object for HentDokumentUrl.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public class HentDokumentUrlRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private VariantFormatCode variantFormat;
 
    /**
	 * Default constructor only used for mapping.
	 */
	@SuppressWarnings("unused")
	private HentDokumentUrlRequestTo() {
	}
    
	/**
	 * Constructs a new HentDokumentUrlRequestTo.
	 *
	 * @param journalpostId journalpostId
	 * @param dokumentInfoId dokumentInfoId
	 * @param variantFormat document variant
	 */
	public HentDokumentUrlRequestTo(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		this.journalpostId = journalpostId;
		this.dokumentInfoId = dokumentInfoId;
		this.variantFormat = variantFormat;
	}
	
	public void validate() {
		if (journalpostId == null) {
			throw new InvalidArgumentException("Missing parameter journalpostId");
		}
		if (dokumentInfoId == null) {
			throw new InvalidArgumentException("Missing parameter dokumentInfoId");
		}
		if (variantFormat == null) {
			throw new InvalidArgumentException("Missing parameter variantFormat");
		}
	}

	public Long getJournalpostId() {
		return journalpostId;
	}

	public Long getDokumentInfoId() {
		return dokumentInfoId;
	}

	public VariantFormatCode getVariantFormat() {
		return variantFormat;
	}
	
}
