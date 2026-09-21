package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.util.Digest.sha256;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for FilDetaljer.
 *
 */
public class FilDetaljerTest {

	public static final byte[] FILE_CONTENT = "Test".getBytes();

	@Test
	public void shouldThrowExceptionForMissingFiltype() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.variantFormat(VariantFormatCode.ARKIV)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(filDetaljer, "filtype");
	}

	@Test
	public void shouldThrowExceptionForMissingVariantFormat() {
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
	public void shouldCreateDokumentFilCorrectlyForNewFilDetaljer() {
		String kildeNavn = "Opprettet Kilde";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(FILE_CONTENT)
				.filUuid(FilDetaljer.generateUuid())
				.opprettetKildeNavn(kildeNavn)
				.build();

		DokumentFil dokumentFil = filDetaljer.createDokumentFil();

		assertDokumentFil(kildeNavn, filDetaljer, dokumentFil);
	}

	@Test
	public void shouldCreateDokumentFilCorrectlyForExistingFilDetaljer() {
		String kildeNavn = "Opprettet Kilde";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fildetaljerId(10L)
				.fileContent(FILE_CONTENT)
				.filUuid(FilDetaljer.generateUuid())
				.endretKildeNavn(kildeNavn)
				.build();

		DokumentFil dokumentFil = filDetaljer.createDokumentFil();

		assertDokumentFil(kildeNavn, filDetaljer, dokumentFil);
	}

	@Test
	public void shouldSetFilStorrelseWhenCreatingDokumentFil() {
		byte[] fileContent = FILE_CONTENT;
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(fileContent)
				.filUuid(FilDetaljer.generateUuid())
				.build();
		filDetaljer.createDokumentFil();
		assertThat(Integer.valueOf(filDetaljer.getFilstorrelse()), is(fileContent.length));
	}

	@Test
	public void shouldNotOverwriteFilStorrelseWhenCreatingDokumentFil() {
		String filStorrelse = "100";
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(FILE_CONTENT)
				.filstorrelse(filStorrelse)
				.filUuid(FilDetaljer.generateUuid())
				.build();
		filDetaljer.createDokumentFil();
		assertThat(filDetaljer.getFilstorrelse(), is(filStorrelse));
	}

	@Test
	public void shouldValidateThatFildetaljerIsAPdf() {
		FilDetaljer f = new FilDetaljer();
		f.setFiltype(FilTypeCode.PDF);
		assertTrue(f.isAPdf());
		f.setFiltype(FilTypeCode.PDFA);
		assertTrue(f.isAPdf());
	}

	@Test
	public void isNotPdf() {
		FilDetaljer f = new FilDetaljer();
		f.setFiltype(FilTypeCode.XML);
		assertFalse(f.isAPdf());
	}

	private void assertDokumentFil(String kildeNavn, FilDetaljer filDetaljer, DokumentFil dokumentFil) {
		assertThat(dokumentFil.getFil(), is(filDetaljer.getFileContent()));
		assertThat(dokumentFil.getFilUuid(), is(filDetaljer.getFilUuid()));
		assertThat(dokumentFil.getOpprettetKildeNavn(), is(kildeNavn));
		assertThat(filDetaljer.getSha256Sjekksum(), is(sha256(dokumentFil.getFil())));
	}

	@Test
	public void shouldReturnTrueWhenFileContentIsSet() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder()
				.fileContent(FILE_CONTENT)
				.build();
		assertThat(filDetaljer.hasFileContent(), is(true));
	}

	@Test
	public void shouldReturnFalseWhenFileContentIsNotSet() {
		FilDetaljer filDetaljer = getFilDetaljerBuilder().build();
		assertThat(filDetaljer.hasFileContent(), is(false));
	}

}
