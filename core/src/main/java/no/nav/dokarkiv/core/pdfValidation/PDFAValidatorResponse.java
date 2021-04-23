package no.nav.dokarkiv.core.pdfValidation;

import lombok.Getter;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.Set;

@Getter
public class PDFAValidatorResponse {

	private boolean isValidPdf;
	private boolean isCompliant;
	private PDFAFlavour pdfVersion;
	private Set<String> assertionResults;
	private String filUuid;
	private String arkivvariant;
	private String dokumenttype;
	private long dokumentinfoId;
	private String filtype;

	public PDFAValidatorResponse(boolean isValidPdf, boolean isCompliant, PDFAFlavour pdfVersion, Set<String> assertionResults, FilDetaljer filDetaljer) {
		this.isValidPdf = isValidPdf;
		this.isCompliant = isCompliant;
		this.pdfVersion = pdfVersion;
		this.assertionResults = assertionResults;
		this.filUuid = filDetaljer.getFilUuid();
		this.arkivvariant = filDetaljer.getVariantFormat() == null ? "Ukjent format" : filDetaljer.getVariantFormat().toString();
		this.dokumenttype = filDetaljer.getFiltype() == null ? "UKJENT" : filDetaljer.getFiltype().toString();
		this.dokumentinfoId = safeDeterminedokumentinfoId(filDetaljer.getDokumentInfo());
		this.filtype = filDetaljer.getFiltype() == null ? "UKJENT" : filDetaljer.getFiltype().toString();
	}

	private long safeDeterminedokumentinfoId(DokumentInfo dokumentInfo) {
		return dokumentInfo == null ? 0 : dokumentInfo.getDokumentInfoId() == null ? 0 : dokumentInfo.getDokumentInfoId();
	}


	public String validPdfToString() {
		return isValidPdf ? "gyldig" : "ugyldig";
	}

}
