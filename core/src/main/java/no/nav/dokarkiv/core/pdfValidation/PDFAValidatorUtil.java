package no.nav.dokarkiv.core.pdfValidation;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import org.verapdf.core.ModelParsingException;
import org.verapdf.core.ValidationException;
import org.verapdf.gf.foundry.VeraGreenfieldFoundryProvider;
import org.verapdf.pdfa.Foundries;
import org.verapdf.pdfa.PDFAParser;
import org.verapdf.pdfa.PDFAValidator;
import org.verapdf.pdfa.VeraPDFFoundry;
import org.verapdf.pdfa.flavours.PDFAFlavour;
import org.verapdf.pdfa.results.TestAssertion;
import org.verapdf.pdfa.results.ValidationResult;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.NO_FLAVOUR;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_3_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_3_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_3_U;

@Slf4j
public class PDFAValidatorUtil {

	// Lovlige formater
	// https://lovdata.no/dokument/SF/forskrift/2017-12-19-2286/KAPITTEL_5-4#%C2%A75-18
	private static final EnumSet<PDFAFlavour> VALID_PDFA_FLAVOURS = EnumSet.of(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U, PDFA_3_A, PDFA_3_B, PDFA_3_U);
	public static final Set<String> NOT_A_PDFA = Set.of("Dokumentet er ikke en PDFA");
	public static final Set<String> NON_VALID_PDFA_VERSION = Set.of("Dokumentet er ikke på et av de registrerte lovlige formatene " + VALID_PDFA_FLAVOURS);

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
		if (filDetaljer == null || filDetaljer.getFileContent() == null || filDetaljer.getFileContent().length == 0) {
			throw new InvalidPdfException("Filen er null eller tom");
		}

		try (VeraPDFFoundry foundry = Foundries.defaultInstance()) {
			PDFAParser parser = foundry.createParser(new ByteArrayInputStream(filDetaljer.getFileContent()));

			// Hvis PDF ikke er på et av de lovlige formatene hopp over valideringen
			if (!VALID_PDFA_FLAVOURS.contains(parser.getFlavour())) {
				return returnIncorrectFlavourReponse(filDetaljer, parser.getFlavour());
			}
			return doValidatePDFA(filDetaljer, foundry, parser);

		} catch (ModelParsingException e) {
			return returnNotAPdfValidatorResponse(filDetaljer);
		} catch (Exception e) {
			throw new InvalidPdfException("Feil under validering av PDF/A", e);
		}
	}

	private static PDFAValidatorResponse doValidatePDFA(FilDetaljer filDetaljer, VeraPDFFoundry foundry, PDFAParser parser) throws ValidationException {
		PDFAValidator validator = foundry.createValidator(parser.getFlavour(), false);
		ValidationResult result = validator.validate(parser);

		if (result.isCompliant()) {
			return returnCompliantValidatorResponse(result, filDetaljer);
		}
		return returnNonCompliantValidatorResponse(result, result.getTestAssertions(), filDetaljer);
	}

	private static PDFAValidatorResponse returnCompliantValidatorResponse(ValidationResult result, FilDetaljer filDetaljer) {
		return new PDFAValidatorResponse(true, true, result.getPDFAFlavour(), Collections.emptySet(), filDetaljer);
	}

	private static PDFAValidatorResponse returnNonCompliantValidatorResponse(ValidationResult result, List<TestAssertion> assertions, FilDetaljer filDetaljer) {
		Set<String> reasonsForFailing = assertions.stream().map(TestAssertion::getMessage).collect(Collectors.toSet());
		return new PDFAValidatorResponse(false, false, result.getPDFAFlavour(), reasonsForFailing, filDetaljer);
	}

	private static PDFAValidatorResponse returnNotAPdfValidatorResponse(FilDetaljer filDetaljer) {
		return new PDFAValidatorResponse(false, false, NO_FLAVOUR, NOT_A_PDFA, filDetaljer);
	}

	private static PDFAValidatorResponse returnIncorrectFlavourReponse(FilDetaljer filDetaljer, PDFAFlavour flavour) {
		return new PDFAValidatorResponse(false, false, flavour, NON_VALID_PDFA_VERSION, filDetaljer);
	}

}
