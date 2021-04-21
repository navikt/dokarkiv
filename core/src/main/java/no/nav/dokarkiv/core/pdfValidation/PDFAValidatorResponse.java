package no.nav.dokarkiv.core.pdfValidation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PDFAValidatorResponse {

	private boolean isValidPdf;
	private boolean isCompliant;
	private PDFAFlavour pdfVersion;
	private Set<String> assertionResults;
	private String filUuid;


	public String validPdfToString(){
		return isValidPdf? "gyldig":"ugyldig";
	}

}
