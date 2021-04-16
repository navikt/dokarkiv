package no.nav.dokarkiv.core.pdfValidation;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.*;

@Slf4j
public class PdfValidatorUtil {

	private static final List<PDFAFlavour> validPdfas = Arrays.asList(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U);
	public static final String NOT_PDFA = "IKKE_PDFA";
	public static final Set NOT_A_PDFA = new HashSet<>(Arrays.asList("Dokumentet er ikke en PDFA"));

	//Static init to initialize the FoundryProvider
	static{
		VeraGreenfieldFoundryProvider.initialise();
	}

	public static PdfValidatorResponse validatePdf(byte[] dokumentfil) {
		if (dokumentfil == null || dokumentfil.length == 0) {
			throw new InvalidPdfException("Filen er null");
		}
		return validatePdf(new DokumentFil(null, dokumentfil));
	}

	public static PdfValidatorResponse validatePdf(DokumentFil dokumentfil) {
		String filUuid = null == dokumentfil.getFilUuid() ? "INGEN_FILUUID" : dokumentfil.getFilUuid();

		ByteArrayInputStream pdf = new ByteArrayInputStream(dokumentfil.getFil());
		if (pdf == null) {
			throw new InvalidPdfException("Filen er null");
		}
		try (PDFAParser parser = Foundries.defaultInstance().createParser(pdf)) {
			PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
			ValidationResult result = validator.validate(parser);
			if (result.isCompliant()) {
				if (isValidPdfVersion(result)) {
					return returnCompliantValidatorResponse(true, result, filUuid);
				}
				return returnCompliantValidatorResponse(false, result, filUuid);
			}
			return returnNonCompliantValidatorResponse(false, result, result.getTestAssertions(), filUuid);
		} catch (ModelParsingException e) {
			return returnNotAPdfValidatorResponse(filUuid);
		} catch (Exception e) {
			throw new InvalidPdfException("Feil under validering av PDF/A", e);
		}
	}

	private static  boolean isValidPdfVersion(ValidationResult result) {
		return validPdfas.contains(result.getPDFAFlavour()) ? true : false;
	}

	private static  PdfValidatorResponse returnCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, String filuuid) {
		return new PdfValidatorResponse(isValidPdf, true, result.getPDFAFlavour().toString(), null, filuuid);
	}

	private static  PdfValidatorResponse returnNonCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, List<TestAssertion> assertions, String filuuid) {
		Set<String> reasonsForFailing = assertions.stream().map(assertion -> assertion.getMessage()).collect(Collectors.toSet());
		return new PdfValidatorResponse(isValidPdf, false, result.getPDFAFlavour().toString(), reasonsForFailing, filuuid);
	}

	private static  PdfValidatorResponse returnNotAPdfValidatorResponse(String filuuid) {
		return new PdfValidatorResponse(false, false, NOT_PDFA, NOT_A_PDFA, filuuid);
	}
}
