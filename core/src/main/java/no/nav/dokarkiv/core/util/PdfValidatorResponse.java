package no.nav.dokarkiv.core.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class PdfValidatorResponse {

	public boolean isValidPdf;

	public boolean isCompliant;

	public String pdfVersion;

	public Set<String> assertionResults;

	public String validPdfToString(){
		return isValidPdf? "gyldig":"ugyldig";
	}

	public String toString(String filename){

		return filename +" er en " + validPdfToString() + " PDF/A på format " + pdfVersion +". Den er " + compliantToString();
	}

	public String toString(String journalpostId, String fagomraade, String fileName){
		return "journalpostId=" + journalpostId + " fra fagomraade="+fagomraade + " har en " + validPdfToString() + " PDF/A: \"" + fileName + "\" på format " + pdfVersion +". Den er " + compliantToString();
	}


	private String compliantToString(){
		String nonCompliantReasons = "";
		if(!isCompliant){
			for(String s : assertionResults) {
				nonCompliantReasons += s + "\n";
			}
			return "ikke compliant med feil: \n" + nonCompliantReasons;
		}
		else
			return "compliant.";
	}

}
