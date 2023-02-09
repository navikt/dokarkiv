package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.ApplicationException;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import no.nav.dokarkiv.core.journalbehandling.DefaultJournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.DefaultMandatoryFieldsVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DefaultMandatoryFieldsVerifier.class,
		DefaultOpprettJournalpostValidator.class,
		DefaultJournalpostStructureVerifier.class,
		OpprettJournalpostPostUpdateVerifier.class})
public class DefaultOpprettJournalpostValidatorTest {
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";
	private static final String OPPRETTET_KILDE_NAVN = "dokumentproduksjon";

	private Journalpost journalpost;

	@Autowired
	private OpprettJournalpostValidator opprettJournalpostValidator;

	@BeforeEach
	public void setUp() {
		journalpost = createJournalpost();
	}

	//Saksrelasjon
	@Test
	public void shouldThrowExceptionIfNoSaksrelasjonOnJournalpost() {
		journalpost.setSaksrelasjon(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"saksrelasjon must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setSakId(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"sakId must be set");

	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		journalpost.getSaksrelasjon().setFagsystem(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"fagsystem must be set");
	}


	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		journalpost.setFagomrade(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"fagomrade must be set");
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		journalpost.setOpprettetAvNavn(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"opprettetAvNavn must be set");
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		journalpost.setJournalForendeEnhetId(null);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Field journalfoerendeEnhetId must be set");
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		journalpost.setInnhold(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"innhold must be set");
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		journalpost.setAvsenderMottaker(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"avsenderMottaker must be set");
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		journalpost.setJournalstatus(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"journalstatus must be set");
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		journalpost.clearBrukere();

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Journalpost must have at least one Bruker");
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerId(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"brukerId must be set");
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		journalpost.getBrukere().iterator().next().setBrukerType(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"brukerType must be set");
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"dokumentInfo must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"kategori must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"tittel must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setBrevkode(null);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Brevkode must be set");
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"DokumenttypeId must be set");
	}

	@Test
	public void shouldNotThrowExceptionIfNoSensitivtOnDocumentInfo() {
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSensitivt(null);

		opprettJournalpostValidator.validate(journalpost);
	}

	//FilDetaljer

	@Test
	public void shouldThrowExceptionIfNoFildetaljer() {
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().clearFildetaljerListe();

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Fildetaljer must be set");
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"variantFormat must be set");
	}

	@Test
	public void shouldThrowExcpetionIfNoFilTypeOnFildetaljerOnJournalpost() {
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);

		assertThrows(InvalidArgumentException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"filtype must be set");
	}

	@Test
	public void shouldThrowExcpetionIfMetaforceInstanceIdIsMissing() {
		journalpost.findAllFilDetaljer().get(0).setMetaforceInstanceId(null);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"MetaforceInstanceId must be set");
	}

	@Test
	public void shouldThrowExcpetionIfMetaforceInstanceIdIsZero() {
		journalpost.findAllFilDetaljer().get(0).setMetaforceInstanceId(0L);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"MetaforceInstanceId must be set");
	}

	@Test
	public void shouldPassIfJournalStatusD() {
		journalpost.setJournalstatus(JournalStatusCode.D);

		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalStatusNotD() {
		journalpost.setJournalstatus(JournalStatusCode.A);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Expected journalstatus D, got A");
	}

	@Test
	public void shouldPassIfJournalpostTypeU() {
		journalpost.setJournalposttype(JournalpostTypeCode.U);

		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostTypeNotU() {
		journalpost.setJournalposttype(JournalpostTypeCode.I);

		assertThrows(ApplicationException.class,
				() -> opprettJournalpostValidator.validate(journalpost),
				"Expected journalpostType U, got I");
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId(1L)
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.fagsystem(FagsystemCode.FS22).build())
				.journalStatus(JournalStatusCode.D)
				.journalpostType(JournalpostTypeCode.U)
				.fagomrade(FagomradeCode.UKJ)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.journalForendeEnhetId("309480dfk")
				.innhold("innhold")
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.land("Norge")
				.brukere(
						getBrukerBuilder()
								.brukerId("01054512313")
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.brukerType(BrukerTypeCode.PERSON).build())
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
								.tilknyttetAvNavn("Tester")
								.dokumentInfo(
										getDokumentInfoBuilder()
												.dokumentstatus(DokumentStatusCode.UNDER_REDIGERING)
												.kategori(DokumentKategoriCode.SED)
												.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
												.tittel("Brev")
												.dokumenttypeId("dokumenttypeId")
												.sensitivt(SENSITIVT_REQUEST)
												.brevkode("brevkode")
												.filDetaljerList(
														getFilDetaljerBuilder()
																.filtype(FilTypeCode.AXML)
																.metaforceInstanceId(123L)
																.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
																.variantFormat(
																		VariantFormatCode.ARKIV)
																.build())
												.build()).build()).build();

	}
}
