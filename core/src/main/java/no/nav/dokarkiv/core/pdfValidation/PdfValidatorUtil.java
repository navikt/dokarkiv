package no.nav.dokarkiv.core.pdfValidation;

import lombok.extern.slf4j.Slf4j;
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
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;

@Slf4j
public class PdfValidatorUtil {

	private static final List<PDFAFlavour> validPdfas = Arrays.asList(PDFA_1_A, PDFA_1_B, PDFA_2_A, PDFA_2_B, PDFA_2_U);
	private static final String NOT_PDF = "IKKE_PDF";
	private static final Set NOT_A_PDF = new HashSet<>(Arrays.asList("Dokumentet er ikke en PDF"));

	//Static init to initialize the FoundryProvider
	static{
		VeraGreenfieldFoundryProvider.initialise();
	}

	public static PdfValidatorResponse validatePdf(byte[] dokumentfil) {
		if (dokumentfil == null || dokumentfil.length == 0) {
			throw new InvalidPdfException("Filen er null");
		}
		return validatePdf(new ByteArrayInputStream(dokumentfil));
	}

	public static PdfValidatorResponse validatePdf(InputStream pdf) {
		if (pdf == null) {
			throw new InvalidPdfException("Filen er null");
		}
		try (PDFAParser parser = Foundries.defaultInstance().createParser(pdf)) {
			PDFAValidator validator = Foundries.defaultInstance().createValidator(parser.getFlavour(), false);
			ValidationResult result = validator.validate(parser);
			if (result.isCompliant()) {
				if (isValidPdfVersion(result)) {
					return returnCompliantValidatorResponse(true, result);
				}
				return returnCompliantValidatorResponse(false, result);
			}
			return returnNonCompliantValidatorResponse(false, result, result.getTestAssertions());
		} catch (ModelParsingException e) {
			return returnNotAPdfValidatorResponse();
		} catch (Exception e) {
			throw new InvalidPdfException("Feil under validering av PDF", e);
		}
	}

	//Disse verdiene må endres etter den nye oppgaven
	public static  void logJournalpost(Journalpost journalpost, String fildetaljer_uuid) {
		String journalpostID = journalpost.getJournalpostId() == null ? "INGEN_ID" : journalpost.getJournalpostId().toString();
		String tema = journalpost.getBehandlingstema() == null ? "TEMA_IKKE_SATT" : journalpost.getBehandlingstema();
		String fagomraade = journalpost.getFagomrade() == null ? "FAGOMRAADE_IKKE_SATT" : journalpost.getFagomrade().toString();
		String fildetaljerId = fildetaljer_uuid == null ? "INGEN_FILDETAJLER_:UUID" : fildetaljer_uuid;
		log.info("Starter behandling av filUuid={} tilhørende Journalpost={} med dokumentbehandlingstema:{} fra fagomraade={}, ", fildetaljerId, journalpostID, tema, fagomraade);
	}


	private static  boolean isValidPdfVersion(ValidationResult result) {
		return validPdfas.contains(result.getPDFAFlavour()) ? true : false;
	}

	private static  PdfValidatorResponse returnCompliantValidatorResponse(boolean isValidPdf, ValidationResult result) {
		return new PdfValidatorResponse(isValidPdf, true, result.getPDFAFlavour().toString(), null);
	}

	private static  PdfValidatorResponse returnNonCompliantValidatorResponse(boolean isValidPdf, ValidationResult result, List<TestAssertion> assertions) {
		Set<String> reasonsForFailing = assertions.stream().map(assertion -> assertion.getMessage()).collect(Collectors.toSet());
		return new PdfValidatorResponse(isValidPdf, false, result.getPDFAFlavour().toString(), reasonsForFailing);
	}

	private static  PdfValidatorResponse returnNotAPdfValidatorResponse() {
		return new PdfValidatorResponse(false, false, NOT_PDF, NOT_A_PDF);
	}
}
