package no.nav.dokarkiv.core.util;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDOutputIntent;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class pdfValidatorTest {

	@Autowired
	private ApplicationContext ctx;


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

	@Test
	//@Ignore
	public void validateTestPdfs() throws Exception {

		for (String fileName : getFilenames()) {
			InputStream pdf = classpathToInputStream("pdf/pdf/" + fileName);
			PdfValidatorResponse response = PdfValidator.isValidPdf(pdf);
			//System.out.println(response.toString(fileName) + "\n");
			System.out.println(response.toString(""+29283849, "PEN", fileName));
		}


	}

	private static List<InputStream> loadPdfs() throws Exception {
		ArrayList<InputStream> pdfs = new ArrayList<>();

		pdfs.add(classpathToInputStream("pdf/pdf/453643390_navno_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453643409_navno_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453643559_skan_im_pdf.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453643863_navno_pdf.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644011_navno_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644029_altinn_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644120_eessi_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644161_eessi_pdf.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644181_skan_im_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644357_altinn_pdf.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644425_eessi_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644598_skan_im_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644612_navno_pdf.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644811_altinn_pdfa.pdf"));
		pdfs.add(classpathToInputStream("pdf/pdf/453644979_eessi_pdf.pdf"));

		return pdfs;
	}

	private static List<String> getFilenames() {
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

	/*
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
