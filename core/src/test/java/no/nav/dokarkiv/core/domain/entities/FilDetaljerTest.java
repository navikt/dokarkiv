package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Test;

/**
 * Unit tests for FilDetaljer.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class FilDetaljerTest {

	@Test
	public void shouldThrowExceptionForMissingFiltype() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.variantFormat(VariantFormatCode.ARKIV)
									.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(filDetaljer, "filtype");
	}
	
	@Test
	public void shouldThrowExceptionForMissingVariantFormat() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.filtype(FilTypeCode.PDF)
									.build();
		
		assertExceptionThrownWhenVerifyingMandatoryFields(filDetaljer, "variantFormat");
	}
	
	private void assertExceptionThrownWhenVerifyingMandatoryFields(FilDetaljer filDetaljer, String fieldName) {
		try {
			filDetaljer.verifyMandatoryFields();
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}
	
	@Test
	public void shouldCreateDokumentFilCorrectlyForNewFilDetaljer() throws Exception {
		String kildeNavn = "Opprettet Kilde";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.fileContent("Test".getBytes())
									.filUuid(FilDetaljer.generateUuid())
									.opprettetKildeNavn(kildeNavn)
									.build();
		
		DokumentFil dokumentFil = filDetaljer.createDokumentFil();
		
		assertDokumentFil(kildeNavn, filDetaljer, dokumentFil);
	}
	
	@Test
	public void shouldCreateDokumentFilCorrectlyForExistingFilDetaljer() throws Exception {
		String kildeNavn = "Opprettet Kilde";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.fildetaljerId(10L)
									.fileContent("Test".getBytes())
									.filUuid(FilDetaljer.generateUuid())
									.endretKildeNavn(kildeNavn)
									.build();
		
		DokumentFil dokumentFil = filDetaljer.createDokumentFil();
		
		assertDokumentFil(kildeNavn, filDetaljer, dokumentFil);
	}
	
	@Test
	public void shouldSetFilStorrelseWhenCreatingDokumentFil() throws Exception {
		byte[] fileContent = "Test".getBytes();
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.fileContent(fileContent)
									.filUuid(FilDetaljer.generateUuid())
									.build();
		filDetaljer.createDokumentFil();
		assertThat(Integer.valueOf(filDetaljer.getFilstorrelse()), is(fileContent.length));
	}
	
	@Test
	public void shouldNotOverwriteFilStorrelseWhenCreatingDokumentFil() throws Exception {
		byte[] fileContent = "Test".getBytes();
		String filStorrelse = "100";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.fileContent(fileContent)
									.filstorrelse(filStorrelse)
									.filUuid(FilDetaljer.generateUuid())
									.build();
		filDetaljer.createDokumentFil();
		assertThat(filDetaljer.getFilstorrelse(), is(filStorrelse));
	}
	
	@Test
	public void shouldValidateThatFildetaljerIsAPdf() throws Exception {
		FilDetaljer f = new FilDetaljer();
		f.setFiltype(FilTypeCode.PDF);
		assertTrue(f.isAPdf());
		f.setFiltype(FilTypeCode.PDFA);
		assertTrue(f.isAPdf());
	}	
	
	@Test
	public void isNotPdf() throws Exception {
		FilDetaljer f = new FilDetaljer();
		f.setFiltype(FilTypeCode.XML);
		assertFalse(f.isAPdf());
	}

	private void assertDokumentFil(String kildeNavn, FilDetaljer filDetaljer, DokumentFil dokumentFil) {
		assertThat(dokumentFil.getFil(), is(filDetaljer.getFileContent()));
		assertThat(dokumentFil.getFilUuid(), is(filDetaljer.getFilUuid()));
		assertThat(dokumentFil.getOpprettetKildeNavn(), is(kildeNavn));
	}
	
	@Test
	public void shouldReturnTrueWhenFileContentIsSet() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
									.fileContent("Test".getBytes())
									.build();
		assertThat(filDetaljer.hasFileContent(), is(true));
	}
	
	@Test
	public void shouldReturnFalseWhenFileContentIsNotSet() throws Exception {
		FilDetaljer filDetaljer = getFilDetaljerBuilder().build();
		assertThat(filDetaljer.hasFileContent(), is(false));
	}
	
}
