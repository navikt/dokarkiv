package no.nav.dokarkiv.core.pdfValidation;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.NO_FLAVOUR;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;

@Slf4j
public class PDFAValidatorUtil {

	private static final List<PDFAFlavour> validPdfas = Arrays.asList(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U);
	public static final Set NOT_A_PDFA = new HashSet<>(Arrays.asList("Dokumentet er ikke en PDFA"));

	//Static init to initialize the FoundryProvider
	static {
		VeraGreenfieldFoundryProvider.initialise();
	}

	/*
	Note!
	Om vi etter hvert trenger å fikse på metadata for å få pdf/a'er gjennom har verapdf støtte for dette:
	<dependency>
		<groupId>org.verapdf</groupId>
		<artifactId>metadata-fixer</artifactId>
	</dependency>
	 */


	public static PDFAValidatorResponse validatePDFA(FilDetaljer filDetaljer) {
		if (filDetaljer == null || filDetaljer.getFileContent() == null) {
			throw new InvalidPdfException("Filen er null!");
		}

		try (PDFAParser parser = Foundries.defaultInstance().createParser(new ByteArrayInputStream(filDetaljer.getFileContent()))) {
			PDFAValidator validator = Foundries.defaultInstance().createFailFastValidator(parser.getFlavour(), 1);
			ValidationResult result = validator.validate(parser);
			if (result.isCompliant()) {
				if (isValidPdfVersion(result)) {
					return returnCompliantValidatorResponse(true, result, filDetaljer);
				}
				return returnCompliantValidatorResponse(false, result, filDetaljer);
			}
			return returnNonCompliantValidatorResponse(false, result, result.getTestAssertions(), filDetaljer);
		} catch (ModelParsingException e) {
			return returnNotAPdfValidatorResponse(filDetaljer);
		} catch (Exception e) {
			throw new InvalidPdfException("Feil under validering av PDF/A", e);
		}
	}

	private static boolean isValidPdfVersion(ValidationResult result) {
		return validPdfas.contains(result.getPDFAFlavour()) ? true : false;
	}

	private static PDFAValidatorResponse returnCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, FilDetaljer filDetaljer) {
		return new PDFAValidatorResponse(isValidPdf, true, result.getPDFAFlavour(), Collections.emptySet(), filDetaljer);
	}

	private static PDFAValidatorResponse returnNonCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, List<TestAssertion> assertions, FilDetaljer filDetaljer) {
		Set<String> reasonsForFailing = assertions.stream().map(assertion -> assertion.getMessage()).collect(Collectors.toSet());
		return new PDFAValidatorResponse(isValidPdf, false, result.getPDFAFlavour(), reasonsForFailing, filDetaljer);
	}

	private static PDFAValidatorResponse returnNotAPdfValidatorResponse(FilDetaljer filDetaljer) {
		return new PDFAValidatorResponse(false, false, NO_FLAVOUR, NOT_A_PDFA, filDetaljer);
	}

}
