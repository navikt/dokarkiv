package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorResponse;
import no.nav.dokarkiv.core.pdfValidation.PDFAValidatorUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.verapdf.pdfa.flavours.PDFAFlavour;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_1_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_A;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_B;
import static org.verapdf.pdfa.flavours.PDFAFlavour.PDFA_2_U;

@ExtendWith(MockitoExtension.class)
public class PDFAValidatorUtilTest {

	private final String baseString = "2021_01_06_nasjonale_tiltak_16_9_PDF_A_";
	private final String PDF = ".pdf";
	private final String faultString = "All properties specified in XMP form shall use either the predefined schemas defined in the XMP Specification,\n\t\t\tISO 19005-1 or this part of ISO 19005, or any extension schemas that comply with 6.6.2.3.2.";

	@Test
	public void validatePdfs() throws Exception {
		validatePDFA(PDFA_1_A);
		validatePDFA(PDFA_1_B);
		validatePDFA(PDFA_2_B);

		//Disse to har noen problemer. Arkivverket mener de er gyldige men veraPDF er ikke enig.
		//Om disse to slutter å funke prøv å endre de til validatePDFA.
		validateBadPDFA(PDFA_2_A);
		validateBadPDFA(PDFA_2_U);
	}

	@Test
	public void shouldValidateBadPDFA() throws IOException {
		InputStream pdfFile = classpathToInputStream("pdf/pdf/453644598_skan_im_pdfa.pdf");
		PDFAValidatorResponse response = PDFAValidatorUtil.validatePDFA(createFilDetaljer(pdfFile.readAllBytes()));

		assertThat(response.getPdfVersion(), is(PDFA_1_B));
		assertThat(response.validPdfToString(), is("ugyldig"));
		assertThat(response.getAssertionResults().size(), is(6));
	}

	@Test
	public void shouldThrowExceptionWhenNoFile() {
		Assertions.assertThrows(InvalidPdfException.class, () -> PDFAValidatorUtil.validatePDFA(createFilDetaljer(null)));
	}

	private void validatePDFA(PDFAFlavour flavour) throws IOException {
		InputStream pdfFile = classpathToInputStream("pdf/Arkivverket/" + baseString + flavour.getId() + PDF);
		PDFAValidatorResponse response = PDFAValidatorUtil.validatePDFA(createFilDetaljer(pdfFile.readAllBytes()));
		assertThat(response.getAssertionResults(), is(Collections.emptySet()));
		assertThat(response.getPdfVersion(), is(flavour));
		assertThat(response.validPdfToString(), is("gyldig"));
	}

	private void validateBadPDFA(PDFAFlavour flavour) throws IOException {
		InputStream pdfFile = classpathToInputStream("pdf/Arkivverket/" + baseString + flavour.getId() + PDF);
		PDFAValidatorResponse response = PDFAValidatorUtil.validatePDFA(createFilDetaljer(pdfFile.readAllBytes()));

		assertThat(response.getPdfVersion(), is(flavour));
		assertThat(response.validPdfToString(), is("ugyldig"));
		assertThat(response.getAssertionResults().size(), is(1));
		List<String> resultList = new ArrayList<>(response.getAssertionResults());
		assertThat(resultList.get(0), is(faultString));
	}

	private FilDetaljer createFilDetaljer(byte[] fil) {
		FilDetaljer detaljer = new FilDetaljer();
		detaljer.setFileContent(fil);
		return detaljer;
	}

	private static InputStream classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream();
	}

}
