package no.nav.dokarkiv.core.pdfValidation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import javax.persistence.Column;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PdfValidatorResponse {

	private boolean isValidPdf;

	private boolean isCompliant;

	private String pdfVersion;

	private Set<String> assertionResults;

	private String filUuid;


	public String validPdfToString(){
		return isValidPdf? "gyldig":"ugyldig";
	}

	public String toString(String filename){

		return "DokumentfilId="+filename +" er en " + validPdfToString() + " PDF/A på format " + pdfVersion +". Den er " + compliantToString();
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
