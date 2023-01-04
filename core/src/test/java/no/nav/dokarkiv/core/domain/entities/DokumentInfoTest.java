package no.nav.dokarkiv.core.domain.entities;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.jupiter.api.Test;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;
import static no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode.POL;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.PRODUKSJON;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.SLADDET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for DokumentInfo.
 *
 */
public class DokumentInfoTest {


	@Test
	public void getFildetaljerListeShouldFilterSkjermetOnlyReturnSkjermetArkivVariant() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(ARKIV)
								.skjermingType(POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.skjermingType(POL)
								.build())
				.build();

		assertThat(dokumentInfo.getFildetaljerListe().size(), is(1));
		assertThat(dokumentInfo.getFildetaljerListe().iterator().next().getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldReturnFildetaljerWhenNotSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(ARKIV)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(3L)
								.filUuid("test3")
								.variantFormat(PRODUKSJON)
								.build())
				.build();

		assertThat(dokumentInfo.getFildetaljerListe().size(), is(3));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(3));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.PRODUKSJON).getVariantFormat(), is(PRODUKSJON));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET).getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test2").getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test3").getVariantFormat(), is(PRODUKSJON));

	}

	@Test
	public void findFilDetaljerByVariantFormatShouldReturnSladdetVariantWhenArkivVariantIsSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.skjermingType(POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.ARKIV).getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormatAdmin(VariantFormatCode.ARKIV).getVariantFormat(), is(ARKIV));

	}

	@Test
	public void findFilDetaljerByVariantFormatShouldReturnArkivFildetaljerWhenKassert() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(ARKIV)
								.skjermingType(POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(SLADDET)
								.skjermingType(POL)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV), notNullValue());
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(ARKIV).getSkjermingType(), is(POL));
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
		assertThat(dokumentInfo.findFilDetaljerByVariantFormat(VariantFormatCode.SLADDET).getVariantFormat(), is(SLADDET));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void findFilDetaljerByFilUuidShouldReturnSladdetVariantWhenFilUuidBelongsArkivVariantAndArkivVariantIsSkjermet() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.skjermingType(POL)
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
	public void findFilDetaljerByFilUuidShouldReturnArkivVariantWhenFilUuidBelongsArkivVariantAndArkivVariantIsSkjermetAndSladdetNotExists() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(VariantFormatCode.ARKIV)
								.skjermingType(POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(PRODUKSJON)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getFilUuid(), is("test"));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void findFilDetaljerByFilUuidShouldReturnArkivVariantWhenFilUuidBelongsArkivVariantAndArkivVariantIsNotSkjermet() {
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
	public void findFilDetaljerByFilUuidShouldReturnArkivFildetaljerWhenKassert() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(
						FilDetaljer.builder()
								.fildetaljerId(1L)
								.filUuid("test")
								.variantFormat(ARKIV)
								.skjermingType(POL)
								.build(),
						FilDetaljer.builder()
								.fildetaljerId(2L)
								.filUuid("test2")
								.variantFormat(PRODUKSJON)
								.skjermingType(POL)
								.build())
				.build();

		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test2"), nullValue());
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test"), notNullValue());
		assertThat(dokumentInfo.findFilDetaljerByFilUuid("test").getVariantFormat(), is(ARKIV));
		assertThat(dokumentInfo.getFildetaljerListeAdmin().size(), is(2));
	}

	@Test
	public void shouldThrowExceptionForMissingEndretAvNavn() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.dokumentInfoId(19L)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, null, "endretAvNavn");
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentstatusAndJournalposttypeU() {
		Journalpost journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.U)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "dokumentstatus");
	}

	@Test
	public void shouldThrowExceptionForMissingDokumentstatusAndJournalposttypeN() {
		Journalpost journalpost = getJournalpostBuilder()
				.journalpostType(JournalpostTypeCode.N)
				.build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "dokumentstatus");
	}

	@Test
	public void shouldThrowExceptionForMissingKategori() {
		Journalpost journalpost = getJournalpostBuilder().build();
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.tittel("Tittel")
				.sensitivt(false)
				.build();

		assertExceptionThrownWhenVerifyingMandatoryFields(dokumentInfo, journalpost, "kategori");
	}

	@Test
	public void shouldThrowExceptionForMissingTittel() {
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
	public void shouldFindSkannetInnholdById() {
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
	public void shouldFindSkannetInnholdByIdWithNewAndExistingSkannetInnholdsInList() {
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
	public void shouldFindFilDetaljerById() {
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
	public void shouldFindFilDetaljerByFilUuid() {
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
	public void shouldThrowExceptionForDuplicateDokumentVarianter() {
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
	public void shouldReturnTrueIfThereIsADocumentWithArkivVariant() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.ARKIV)
						.build())
				.build();

		assertThat(dokumentInfo.hasArkivFormat(), is(true));
	}

	@Test
	public void shouldReturnFalseIfThereIsNotADocumentWithArkivVariant() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder()
				.filDetaljerList(getFilDetaljerBuilder()
						.variantFormat(VariantFormatCode.PRODUKSJON)
						.build())
				.build();

		assertThat(dokumentInfo.hasArkivFormat(), is(false));
	}

	@Test
	public void shouldThrowExceptionForMissingFilDetaljerWhenEndeligJournalforing() {
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
	public void shouldFindFilDetaljerByVariantFormat() {
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
	public void shouldFindJournalpostDokumentInfoRelasjonByJournalpostId() {
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
	public void shouldRemoveFildetaljerFromSet() {
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
	public void shouldHaveMultipleJournalpostRelations() {
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
	public void shouldNotHaveMultipleJournalpostRelations() {
		DokumentInfo dokumentInfo = getDokumentInfoBuilder().build();

		getJournalpostDokumentInfoRelasjonBuilder()
				.dokumentInfo(dokumentInfo)
				.build();

		assertThat(dokumentInfo.isRelatedToMultipleJournalposts(), is(false));
	}

	@Test
	public void shouldNotHaveMultipleJournalpostRelationsWhenZero() {
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
