package no.nav.dokarkiv.core.domain.entities;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Test;

/**
 * Unit tests for DokumentInfo.
 *
 * @author Thomas Eugen Bjørge, Sirius IT
 */
public class DokumentInfoTest {

	@Test
	public void shouldReturnEmptyFildetaljerListWhenKassert() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.skjermingType(SkjermingTypeCode.POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.skjermingType(SkjermingTypeCode.POL)
								.build())
				.build();

		assertThat(dokumentInfo.getFildetaljerListe().isEmpty(), is(true));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnFildetaljerWhenNotKassert() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.build())
				.build();

		assertThat(dokumentInfo.getFildetaljerListe().size(), is(2));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnSladdetVariantWhenArkivVariantIsSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.skjermingType(SkjermingTypeCode.POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnArkivVariantWhenArkivVariantIsNotSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnSladdetVariantWhenFilUuidBelongsArkivVariantAndArkivVariantIsSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.skjermingType(SkjermingTypeCode.POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getFilUuid(), is("test2"));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnArkivtVariantWhenFilUuidBelongsArkivVariantAndArkivVariantIsNotSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getFilUuid(), is("test"));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}


	@Test
	public void shouldThrowExceptionForMissingEndretAvNavn() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.dokumentInfoId(19L)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, null, "endretAvNavn");
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentstatusAndJournalposttypeU() throws Exception {
		Journalpost journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "dokumentstatus");
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentstatusAndJournalposttypeN() throws Exception {
		Journalpost journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.N)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "dokumentstatus");
	}

	@Test
	public void shouldThrowExceptionForMissingKategori() throws Exception {
		Journalpost journalpost = getJournalpostBuilder().build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.tittel("Tittel")
				.sensitivt(false)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "kategori");
	}

	@Test
	public void shouldThrowExceptionForMissingTittel() throws Exception {
		Journalpost journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.D)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.kategori(DokumentKategoriCode.B)
				.sensitivt(false)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "tittel");
	}

	@Test
	public void shouldFindSkannetInnholdById() throws Exception {
		long skannetInnholdId = 200L;
		String innhold = "innhold";
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.skannetInnhold(getSkannetInnholdBuilder()
								.skannetInnholdId(100L)
								.vedleggInnhold("test")
								.build(),
						getSkannetInnholdBuilder()
								.skannetInnholdId(skannetInnholdId)
								.vedleggInnhold(innhold)
								.build())
				.build();
		SkannetInnhold skannetInnhold = dokumentInfo.findSkannetInnholdById(skannetInnholdId);
		assertThat(skannetInnhold.getVedleggInnhold(), is(innhold));
	}

	@Test
	public void shouldFindSkannetInnholdByIdWithNewAndExistingSkannetInnholdsInList() throws Exception {
		long skannetInnholdId = 200L;
		String innhold = "innhold";
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.skannetInnhold(getSkannetInnholdBuilder()
								.vedleggInnhold("test")
								.build(),
						getSkannetInnholdBuilder()
								.skannetInnholdId(skannetInnholdId)
								.vedleggInnhold(innhold)
								.build())
				.build();
		SkannetInnhold skannetInnhold = dokumentInfo.findSkannetInnholdById(skannetInnholdId);
		assertThat(skannetInnhold.getVedleggInnhold(), is(innhold));
	}

	@Test
	public void shouldFindFilDetaljerById() throws Exception {
		long filDetaljerId = 200L;
		String uuid = "uuid";
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						getFilDetaljerBuilder()
								.fildetaljerId(100L)
								.filUuid("test")
								.build(),
						getFilDetaljerBuilder()
								.fildetaljerId(filDetaljerId)
								.filUuid(uuid)
								.build())
				.build();
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerById(filDetaljerId);
		assertThat(filDetaljer.getFilUuid(), is(uuid));
	}

	@Test
	public void shouldFindFilDetaljerByFilUuid() throws Exception {
		long filDetaljerId = 200L;
		String filUuid = FilDetaljer.generateUuid();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						getFilDetaljerBuilder()
								.filUuid("test")
								.build(),
						getFilDetaljerBuilder()
								.fildetaljerId(filDetaljerId)
								.filUuid(filUuid)
								.build())
				.build();
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByFilUuid(filUuid);
		assertThat(filDetaljer.getId(), is(filDetaljerId));
	}

	@Test
	public void shouldThrowExceptionForDuplicateDokumentVarianter() throws Exception {
		VariantFormatCode arkivVariant = VariantFormatCode.ARKIV;
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
								.variantFormat(arkivVariant)
								.build(),
						getFilDetaljerBuilder()
								.variantFormat(VariantFormatCode.PRODUKSJON)
								.build(),
						getFilDetaljerBuilder()
								.variantFormat(arkivVariant)
								.build())
				.build();

		try {
			dokumentInfo.verifyNoVariantDuplicates();
		} catch (InvalidJournalpostStructureException e) {
			assertThat(e.getMessage(), containsString("2"));
			assertThat(e.getMessage(), containsString(arkivVariant.name()));
		}
	}

	@Test
	public void shouldReturnTrueIfThereIsADocumentWithArkivVariant() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.ARKIV)
						.build())
				.build();

		assertThat(dokumentInfo.hasArkivFormat(), is(true));
	}

	@Test
	public void shouldReturnFalseIfThereIsNotADocumentWithArkivVariant() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.PRODUKSJON)
						.build())
				.build();

		assertThat(dokumentInfo.hasArkivFormat(), is(false));
	}

	@Test
	public void shouldThrowExceptionForMissingFilDetaljerWhenEndeligJournalforing() throws Exception {
		Journalpost journalpost = getJournalpostBuilder()
				.journalStatus(JournalStatusCode.J)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.tittel("Tittel")
				.kategori(DokumentKategoriCode.B)
				.sensitivt(false)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "FilDetaljer");
	}

	@Test
	public void shouldFindFilDetaljerByVariantFormat() throws Exception {
		VariantFormatCode arkiv = VariantFormatCode.ARKIV;
		String uuid = "uuid";
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						getFilDetaljerBuilder()
								.variantFormat(VariantFormatCode.PRODUKSJON)
								.filUuid("test")
								.build(),
						getFilDetaljerBuilder()
								.variantFormat(arkiv)
								.filUuid(uuid)
								.build())
				.build();
		FilDetaljer filDetaljer = dokumentInfo.findFilDetaljerByVariantFormat(arkiv);
		assertThat(filDetaljer.getFilUuid(), is(uuid));

	}

	@Test
	public void shouldFindJournalpostDokumentInfoRelasjonByJournalpostId() throws Exception {
		Long journalpostId = 200L;
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		JournalpostDokumentInfoRelasjon journalpostRelasjon = getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();
		getJournalpostBuilder()
				.journalpostId(journalpostId)
				.dokumentInfoRelasjoner(journalpostRelasjon)
				.build();

		assertThat(dokumentInfo.findJournalpostRelasjonByJournalpostId(journalpostId), is(journalpostRelasjon));
	}

	@Test
	public void shouldRemoveFildetaljerFromSet() throws Exception {
		FilDetaljer arkivFildetaljer = getFilDetaljerBuilder()
				.variantFormat(VariantFormatCode.ARKIV)
				.filUuid("uuid")
				.build();

		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						getFilDetaljerBuilder()
								.variantFormat(VariantFormatCode.PRODUKSJON)
								.filUuid("uuid2")
								.build(),
						arkivFildetaljer)
				.build();

		dokumentInfo.removeFilDetaljer(arkivFildetaljer);
		assertThat(dokumentInfo.getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfo.getFildetaljerListe().iterator().next().getVariantFormat(), is(VariantFormatCode.PRODUKSJON));

	}

	@Test
	public void shouldHaveMultipleJournalpostRelations() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();

		getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();
		assertThat(dokumentInfo.isRelatedToMultipleJournalposts(), is(true));
	}

	@Test
	public void shouldNotHaveMultipleJournalpostRelations() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();

		assertThat(dokumentInfo.isRelatedToMultipleJournalposts(), is(false));
	}

	@Test
	public void shouldNotHaveMultipleJournalpostRelationsWhenZero() throws Exception {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		assertThat(dokumentInfo.isRelatedToMultipleJournalposts(), is(false));
	}

	private void assertExceptionThrownWhenVerifyingMandatoryFields(DokumentInfo dokumentInfo, Journalpost journalpost,
																   String fieldName) {
		try {
			dokumentInfo.verifyMandatoryFields(journalpost);
			fail();
		} catch (InvalidArgumentException e) {
			assertThat(e.getMessage(), containsString(fieldName));
		}
	}
}
