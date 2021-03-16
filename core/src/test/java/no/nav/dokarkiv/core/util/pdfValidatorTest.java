package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.pdfValidation.PdfValidatorUtil;
import no.nav.dokarkiv.core.pdfValidation.PdfValidatorResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class pdfValidatorTest {

	@Test
	public void validateTestPdfs() throws Exception {

		/*
		Disse testene må utbedres
		 */
		for (String fileName : getq2Filenames()) {
			InputStream pdf = classpathToInputStream("pdf/pdf/" + fileName);
			PdfValidatorResponse response = PdfValidatorUtil.validatePdf(pdf);
			System.out.println(response.toString(fileName));
		}
	}

	@Test
	public void validateArkivverketPdfs() throws Exception{
		/*
		Disse testene må utbedres
		her testes alle pdf/a'ene fra arkivverket. De skal egentlig (?) kanskje (?) validere men vi godtar denne feilen for nå.
		dvs at alle bortsett fra 2a/u skal validere mens 2a/u skal feilvalidere med en spesifikk feil
		 */
		for (String fileName : getArkiverketFilenames()) {
			InputStream pdf = classpathToInputStream("pdf/Arkivverket/" + fileName);
			PdfValidatorResponse response = PdfValidatorUtil.validatePdf(pdf);
			System.out.println(response.toString(fileName));
		}
	}

	private static List<String> getArkiverketFilenames(){
		ArrayList<String> fileNames = new ArrayList<>();
		String baseString = "2021_01_06_nasjonale_tiltak_16_9_PDF_A_";
		String PDF = ".pdf";
		fileNames.add("2021_01_06_nasjonale_tiltak_16_9.pdf");
		fileNames.add(baseString +"1a"+PDF);
		fileNames.add(baseString +"1b"+PDF);
		fileNames.add(baseString +"2a"+PDF);
		fileNames.add(baseString +"2b"+PDF);
		fileNames.add(baseString +"2u"+PDF);
		fileNames.add("2a"+PDF);
		fileNames.add("2u"+PDF);
		return fileNames;
	}

	private static List<String> getq2Filenames() {
		ArrayList<String> fileNames = new ArrayList<>();
		fileNames.add("Test.txt");
		fileNames.add("DummyXml.xml");
		fileNames.add("453643390_navno_pdfa.pdf");
		fileNames.add("453643409_navno_pdfa.pdf");
		fileNames.add("453643559_skan_im_pdf.pdf");
		fileNames.add("453643863_navno_pdf.pdf");
		fileNames.add("453644011_navno_pdfa.pdf");
		fileNames.add("453644029_altinn_pdfa.pdf");
		fileNames.add("453644120_eessi_pdfa.pdf");
		fileNames.add("453644161_eessi_pdf.pdf");
		fileNames.add("453644181_skan_im_pdfa.pdf");
		fileNames.add("453644357_altinn_pdf.pdf");
		fileNames.add("453644425_eessi_pdfa.pdf");
		fileNames.add("453644598_skan_im_pdfa.pdf");
		fileNames.add("453644612_navno_pdf.pdf");
		fileNames.add("453644811_altinn_pdfa.pdf");
		fileNames.add("453644979_eessi_pdf.pdf");

		return fileNames;
	}


	private static InputStream classpathToInputStream(String classpathResource) throws IOException {
		return new ClassPathResource(classpathResource).getInputStream();
	}

	//Gammel kode for å generere en pdf/a 1b
		/*
	* Testen krever noen ekstra filer
	@Test
	@Ignore
	public void shouldValidateValidPDFA2b() throws Exception {
		InputStream pdf = createValidPdf();

		PdfValidatorResponse response = PdfValidator.isValidPdf(pdf);
		assertThat(response.isValidPdf, is(true));
		assertThat(response.pdfVersion, is("1b"));
	}*/

	/*
	 * Denne testen oppretter en egen PDF/A-1b.
	 * Det er lettere / bedre å teste på dokumenter vi allerede vet er på PDF/A-xy
	public static InputStream createValidPdf() throws Exception {
		InputStream fontStream = classpathToInputStream("pdf/ArialMT.ttf");
		PDDocument doc = new PDDocument();
		PDFont font = PDTrueTypeFont.loadTTF(doc, fontStream);

		PDPage page = new PDPage();
		for (int i = 0; i < 75; i++)
			doc.addPage(page);


		// create a page with the message where needed
		PDPageContentStream contentStream = new PDPageContentStream(doc, page);
		contentStream.beginText();
		contentStream.setFont(font, 12);
		contentStream.moveTextPositionByAmount(100, 700);
		contentStream.drawString("Dette er en liten text");
		contentStream.endText();
		contentStream.saveGraphicsState();
		contentStream.close();

		PDDocumentCatalog cat = doc.getDocumentCatalog();
		PDMetadata metadata = new PDMetadata(doc);
		cat.setMetadata(metadata);

		// jempbox version
		XMPMetadata xmp = new XMPMetadata();
		XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
		xmp.addSchema(pdfaid);
		pdfaid.setConformance("B");
		pdfaid.setPart(1);
		pdfaid.setAbout("");
		metadata.importXMPMetadata(xmp.asByteArray());


		// retrieve icc
		// this file cannot be added in PDFBox, it must be downloaded
		// its localization is http://www.color.org/sRGB_IEC61966_2_1_black_scaled.icc
		// UNIX command to retrieve :
		// wget _O target/svart.icc http://www.color.org/sRGB_IEC61966_2_1_black_scaled.icc
		InputStream colorProfile = classpathToInputStream("pdf/svart.icc");
		// create output intent
		PDOutputIntent oi = new PDOutputIntent(doc, colorProfile);
		oi.setInfo("sRGB IEC61966_2.1");
		oi.setOutputCondition("sRGB IEC61966_2.1");
		oi.setOutputConditionIdentifier("sRGB IEC61966_2.1");
		oi.setRegistryName("http://www.color.org");
		cat.addOutputIntent(oi);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		doc.save(out);
		doc.close();
		InputStream in = new ByteArrayInputStream(out.toByteArray());

		return in;
	}*/

}
