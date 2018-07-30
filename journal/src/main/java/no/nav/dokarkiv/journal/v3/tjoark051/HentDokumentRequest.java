package no.nav.dokarkiv.journal.v3.tjoark051;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.modig.core.exception.ApplicationException;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Request object for the MOD operation HentDokument.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
public class HentDokumentRequest {
	
	private Long dokumentId;
	private VariantFormatCode variantFormat;

	
	/** Default constructor needed for mapping. */
	@SuppressWarnings("unused")
	private HentDokumentRequest() {
	}
	
	/**
	 * Constructor using fields of class.
	 * @param dokumentId the id of DokumentInfo object related to document.
	 * @param variantFormat the VarianFormatCode of the document.
	 */
	public HentDokumentRequest(Long dokumentId, VariantFormatCode variantFormat) {
		this.dokumentId = dokumentId;
		this.variantFormat = variantFormat;
	}
	
	public void validate() {
		if (dokumentId == null) {
			throw new ApplicationException("Missing parameter in request: dokumentId");
		}
		if (variantFormat == null) {
			throw new ApplicationException("Missing parameter in request: variantFormat");
		}
	}
	
	/**
	 * @return the dokumentId
	 */
	public Long getDokumentId() {
		return dokumentId;
	}
	
	/**
	 * @return the variantFormat
	 */
	public VariantFormatCode getVariantFormat() {
		return variantFormat;
	}
	
	/** {@inheritDoc} */
	@Override
	public String toString() {
		return new ToStringBuilder(this).append("dokumentId", dokumentId).append("varianFormat", variantFormat.toString())
				.toString();
	}
		
	
}
