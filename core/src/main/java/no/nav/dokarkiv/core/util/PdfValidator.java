package no.nav.dokarkiv.core.util;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.verapdf.core.ModelParsingException;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.*;

public class PdfValidator {

	private static final List<PDFAFlavour> validPdfas = Arrays.asList(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U);
	private static final String NOT_PDF = "IKKE_PDF";
	private static final Set NOT_A_PDF = new HashSet<>(Arrays.asList("Dokumentet er ikke en PDF"));

	public static PdfValidatorResponse isValidPdf(byte[] dokumentfil) {
		if(dokumentfil == null || dokumentfil.length == 0){
			throw new InvalidPdfException("Filen er null");
		}
		return isValidPdf(new ByteArrayInputStream(dokumentfil));
	}

	public static PdfValidatorResponse isValidPdf(InputStream pdf) {
		VeraGreenfieldFoundryProvider.initialise();
		try (PDFAParser parser = Foundries.defaultInstance().createParser(pdf)) {
			PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
			ValidationResult result = validator.validate(parser);
			if(result.isCompliant()){
				if(isValidPdfVersion(result)) {
					return returnCompliantValidatorResponse(true, result);
				}
				return returnCompliantValidatorResponse(false, result);
			}
			return returnNonCompliantValidatorResponse(false, result, result.getTestAssertions());
		}
		catch(ModelParsingException e){
			return returnNotAPdfValidatorResponse();
		}
		catch (Exception e) {
			throw new InvalidPdfException("Feil under validering av PDF", e);
		}
	}

	private static boolean isValidPdfVersion(ValidationResult result){
		return validPdfas.contains(result.getPDFAFlavour()) ? true : false;
	}

	private static PdfValidatorResponse returnCompliantValidatorResponse(boolean isValidPdf,ValidationResult result){
		return new PdfValidatorResponse(isValidPdf, true, result.getPDFAFlavour().toString(), null);
	}

	private static PdfValidatorResponse returnNonCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, List<TestAssertion> assertions){
		Set<String> reasonsForFailing = assertions.stream().map(assertion -> assertion.getMessage()).collect(Collectors.toSet());
		return new PdfValidatorResponse(isValidPdf, false, result.getPDFAFlavour().toString(), reasonsForFailing);
	}

	private static PdfValidatorResponse returnNotAPdfValidatorResponse(){
		return new PdfValidatorResponse(false, false, NOT_PDF, NOT_A_PDF);
	}

	private static PdfValidatorResponse returnFileIsNull(){
		return new PdfValidatorResponse(false, false, NOT_PDF, NOT_A_PDF);
	}


}
