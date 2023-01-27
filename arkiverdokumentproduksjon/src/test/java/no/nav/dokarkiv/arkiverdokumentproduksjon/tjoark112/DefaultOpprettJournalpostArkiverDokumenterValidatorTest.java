package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DefaultMandatoryFieldsVerifier.class,
		DefaultOpprettJournalpostArkiverDokumenterValidator.class,
		DefaultJournalpostStructureVerifier.class})
public class DefaultOpprettJournalpostArkiverDokumenterValidatorTest {
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final Long DOKUMENTINFO_ID_VEDLEGG = 2L;
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	private Journalpost journalpost;

	@Autowired
	private OpprettJournalpostArkiverDokumenterValidator validator;

	@BeforeEach
	public void setUp() {
		journalpost = createJournalpost();
	}

	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		journalpost.setSaksrelasjon(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"saksrelasjon must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setSakId(null);
		journalpost.getSaksrelasjon().setSaknrfk(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"sakId must be set");
	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setFagsystem(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"fagsystem must be set");
	}


	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		journalpost.setOpprettetAvNavn(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"opprettetAvNavn must be set");
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Field journalfoerendeEnhetId must be set");
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		journalpost.setInnhold(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"innhold must be set");
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		journalpost.setAvsenderMottaker(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"avsenderMottaker must be set");
	}

	@Test
	public void shouldNotThrowExceptionIfUtsendingsKanalNotSetOnJournalpost() {
		journalpost.setUtsendingskanal(null);

		validator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfDokumentDatoIsMissing() {
		journalpost.setDokumentDato(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"DatoDokument must be set");
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		journalpost.setJournalstatus(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"journalstatus must be set");
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"Journalpost must have at least one Bruker");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"brukerId must be set");
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"brukerType must be set");
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"dokumentInfo must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Kategori must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Tittel must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setBrevkode(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Brevkode must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"DokumenttypeId must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoSensitivtOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSensitivt(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"Sensitivt must be set");
	}

	//FilDetaljer
	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);

		assertThrows(InvalidJournalpostStructureException.class,
				() -> validator.validate(journalpost),
				"DokumentInfos must contain an arkiv variant");
	}

	@Test
	public void shouldThrowExcpetionIfNoFilTypeOnFildetaljerOnJournalpost() {
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);

		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalpost),
				"filtype must be set");
	}

	@Test
	public void shouldThrowExcpetionIfNoFileContentOnFildetaljerOnFilDetaljer() {
		journalpost.findAllFilDetaljer().get(0).setFileContent(null);

		assertThrows(ApplicationException.class,
				() -> validator.validate(journalpost),
				"FileContent must be set");
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() {
		addDuplicatesOfVariantFormats(journalpost);

		assertThrows(InvalidJournalpostStructureException.class,
				() -> validator.validate(journalpost),
				"DokumentInfo cannot contain dokumentvariant duplicates");
	}

	@Test
	public void shouldThrowExceptionIfGotArkivVariantButOneVarianIsNull() {
		assertThrows(InvalidArgumentException.class,
				() -> validator.validate(journalPostWithOneVariantFormatSetToNull()),
				"variantFormat must be set");
	}

	@Test
	public void shouldNotThrowExceptionIfNullJournalpostType() {
		journalpost.setJournalposttype(null);

		validator.validate(journalpost);
	}

	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal() {
		journalpost.setJournalposttype(null);
		journalpost.setUtsendingskanal(null);

		validator.validate(journalpost);
	}

	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal2() {
		journalpost.setUtsendingskanal(null);

		validator.validate(journalpost);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").brukerType(BrukerTypeCode.PERSON).build())
				.journalStatus(JournalStatusCode.FS)
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId(1L).saknrfk("1").fagsystem(FagsystemCode.FS22).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.dokumentDato(new Date())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.journalForendeEnhetId("309480dfk")
				.land("Norge")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.kategori(DokumentKategoriCode.SED)
												.dokumenttypeId("dokumenttypeid")
												.tittel("Brev")
												.sensitivt(SENSITIVT_REQUEST)
												.brevkode("BREV")
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.ARKIV).build()).build())
								.build(),
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID_VEDLEGG)
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.kategori(DokumentKategoriCode.SED)
												.dokumenttypeId("dokumenttypeid")
												.tittel("Brev")
												.sensitivt(SENSITIVT_REQUEST)
												.brevkode("BREV")
												.filDetaljerList(
														getFilDetaljerBuilder().filtype(FilTypeCode.PDF)
																.fileContent("file".getBytes())
																.variantFormat(VariantFormatCode.ARKIV).build()).build())
								.build()).build();
	}


	private Journalpost journalPostWithOneVariantFormatSetToNull() {
		return getJournalpostBuilder()
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").brukerType(BrukerTypeCode.PERSON).build())
				.journalStatus(JournalStatusCode.FS)
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId(1L).saknrfk("1").fagsystem(FagsystemCode.FS22).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
				.fagomrade(FagomradeCode.AAP)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.dokumentDato(new Date())
				.journalForendeEnhetId("309480dfk")
				.land("Norge")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentInfoId(DOKUMENTINFO_ID)
												.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
												.kategori(DokumentKategoriCode.SED)
												.dokumenttypeId("dokumenttypeid")
												.tittel("Brev")
												.sensitivt(SENSITIVT_REQUEST)
												.brevkode("BREV")
												.filDetaljerList(
														getFilDetaljerBuilder()
																.variantFormat(VariantFormatCode.ARKIV)
																.filtype(FilTypeCode.PDF)
																.fileContent("test".getBytes())
																.build(),
														getFilDetaljerBuilder()
																.variantFormat(null)
																.filtype(FilTypeCode.RTF)
																.fileContent("test".getBytes())
																.build()).build())
								.build()).build();
	}


	protected void addDuplicatesOfVariantFormats(Journalpost journalpost) {
		journalpost.addJournalpostDokumentInfoRelasjon(
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.tilknyttetAvNavn("Tester")
						.dokumentInfo(
								getDokumentInfoBuilder()
										.dokumenttypeId("dokumentTypeId")
										.sensitivt(true)
										.tittel("tittel")
										.innskrenketPartsinnsyn(true)
										.brevkode("brevkode")
										.organInternt(false)
										.kategori(DokumentKategoriCode.ES)
										.filDetaljerList(
												getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.PDF)
														.fileContent("test".getBytes())
														.build(),
												getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.RTF)
														.fileContent("test".getBytes())
														.build())
										.build())
						.build());
	}
}
