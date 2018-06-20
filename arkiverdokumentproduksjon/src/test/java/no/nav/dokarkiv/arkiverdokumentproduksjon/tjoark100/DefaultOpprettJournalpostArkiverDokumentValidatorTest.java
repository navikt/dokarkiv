package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

import no.nav.dokarkiv.arkiverdokumentproduksjon.config.ValidatorTestConfig;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ApplicationException;
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
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.exceptions.InvalidJournalpostStructureException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.Date;

/**
 * Validator test for {@link DefaultOpprettJournalpostArkiverDokumentValidator}
 *
 * @author Stig Strøm
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ValidatorTestConfig.class})
public class DefaultOpprettJournalpostArkiverDokumentValidatorTest {
	private static final Long DOKUMENTINFO_ID = 1L;
	private static final boolean SENSITIVT_REQUEST = true;
	private static final boolean FERDIGSTILL_JOURNALPOST = true;
	private static final boolean IKKE_FERDIGSTILL_JOURNALPOST = false;

	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private Journalpost journalpost;

	@Inject
	private OpprettJournalpostArkiverDokumentValidator validator;

	@Before
	public void setUp() {
		journalpost = createJournalpost();
	}

	//Saksrelasjon
	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("saksrelasjon must be set");
		journalpost.setSaksrelasjon(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("sakId must be set");
		journalpost.getSaksrelasjon().setSakId(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagsystem must be set");
		journalpost.getSaksrelasjon().setFagsystem(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}


	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagomrade must be set");
		journalpost.setFagomrade(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("opprettetAvNavn must be set");
		journalpost.setOpprettetAvNavn(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Field journalfoerendeEnhetId must be set");
		journalpost.setJournalForendeEnhetId(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("innhold must be set");
		journalpost.setInnhold(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("avsenderMottaker must be set");
		journalpost.setAvsenderMottaker(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfUtsendingsKanalNotSetOnJournalpost() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Utsendingskanal must be set");

		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldNotThrowExceptionIfUtsendingsKanalNotSetOnJournalpost() {
		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost, IKKE_FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfDokumentDatoIsMissing() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("DatoDokument must be set");

		journalpost.setDokumentDato(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("journalstatus must be set");
		journalpost.setJournalstatus(null);

		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("Journalpost must have at least one Bruker");
		journalpost.clearBrukere();
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerId must be set");
		journalpost.getBrukere().iterator().next().setBrukerId(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerType must be set");
		journalpost.getBrukere().iterator().next().setBrukerType(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("dokumentInfo must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Kategori must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Tittel must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Brevkode must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setBrevkode(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("DokumenttypeId must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfNoSensitivtOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Sensitivt must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSensitivt(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	//FilDetaljer
	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() {
		expected.expect(InvalidJournalpostStructureException.class);
		expected.expectMessage("DokumentInfos must contain an arkiv variant");
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExcpetionIfNoFilTypeOnFildetaljerOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("filtype must be set");
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExcpetionIfNoFileContentOnFildetaljerOnFilDetaljer() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("FileContent must be set");
		journalpost.findAllFilDetaljer().get(0).setFileContent(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfDokumentDuplicatesOnJournalpost() {
		expected.expect(InvalidJournalpostStructureException.class);
		expected.expectMessage("DokumentInfo cannot contain dokumentvariant duplicates");
		addDuplicatesOfVariantFormats(journalpost);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionIfGotArkivVariantButOneVarianIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("variantFormat must be set");
		validator.validate(journalPostWithOneVariantFormatSetToNull(), FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldNotThrowExceptionIfNullJournalpostType() {
		journalpost.setJournalposttype(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal() {
		journalpost.setJournalposttype(null);
		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldNotThrowExceptionNoUtsendingskanal2() {
		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost, IKKE_FERDIGSTILL_JOURNALPOST);
	}

	@Test
	public void shouldThrowExceptionNoUtsendingskanal() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Utsendingskanal must be set");

		journalpost.setUtsendingskanal(null);
		validator.validate(journalpost, FERDIGSTILL_JOURNALPOST);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").brukerType(BrukerTypeCode.PERSON).build())
				.journalStatus(JournalStatusCode.FS)
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.BID).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.ALTINN)
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
												.kategori(DokumentKategoriCode.E_BLANKETT)
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
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").brukerType(BrukerTypeCode.PERSON).build())
				.journalStatus(JournalStatusCode.FS)
				.saksrelasjon(
						getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.BID).build())
				.innhold("innhold")
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.ALTINN)
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
												.kategori(DokumentKategoriCode.E_BLANKETT)
												.dokumenttypeId("dokumenttypeid")
												.tittel("Brev")
												.sensitivt(SENSITIVT_REQUEST)
												.brevkode("BREV")
												.filDetaljerList(
														getFilDetaljerBuilder()
																.variantFormat(VariantFormatCode.ARKIV)
																.filtype(FilTypeCode.AFP)
																.fileContent("test".getBytes())
																.build(),
														getFilDetaljerBuilder()
																.variantFormat(null)
																.filtype(FilTypeCode.DOC)
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
														.filtype(FilTypeCode.AFP)
														.fileContent("test".getBytes())
														.build(),
												getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.DOC)
														.fileContent("test".getBytes())
														.build())
										.build())
						.build());
	}
}
