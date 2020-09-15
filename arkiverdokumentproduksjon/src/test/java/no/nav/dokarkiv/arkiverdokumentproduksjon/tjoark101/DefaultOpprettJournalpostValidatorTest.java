package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

/**
 * Validator test for {@link DefaultOpprettJournalpostValidator}
 *
 * @author Stig Strøm
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes =  {DefaultMandatoryFieldsVerifier.class,
		DefaultOpprettJournalpostValidator.class,
		DefaultJournalpostStructureVerifier.class,
		OpprettJournalpostPostUpdateVerifier.class})
public class DefaultOpprettJournalpostValidatorTest {
	private static final boolean SENSITIVT_REQUEST = true;
	private static final String OPPRETTET_AV_NAVN = "Saksbehandler";
	private static final String OPPRETTET_KILDE_NAVN = "dokumentproduksjon";

	@Rule
	public ExpectedException expected = ExpectedException.none();

	private Journalpost journalpost;

	@Inject
	private OpprettJournalpostValidator opprettJournalpostValidator;

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
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoSaksIDOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("sakId must be set");
		journalpost.getSaksrelasjon().setSakId(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfSaksRelasjonFagsystemOnJournalpostIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagsystem must be set");
		journalpost.getSaksrelasjon().setFagsystem(null);
		opprettJournalpostValidator.validate(journalpost);
	}


	//Journalpost
	@Test
	public void shouldThrowExceptionIfFagomradeNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("fagomrade must be set");
		journalpost.setFagomrade(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfOpprettetAvNavnNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("opprettetAvNavn must be set");
		journalpost.setOpprettetAvNavn(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalforendeEnhetIdNotSetOnJournalpost() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Field journalfoerendeEnhetId must be set");
		journalpost.setJournalForendeEnhetId(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfInnholdNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("innhold must be set");
		journalpost.setInnhold(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfAvsenderMottakerNotSetOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("avsenderMottaker must be set");
		journalpost.setAvsenderMottaker(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	//Ekstra test for JP
	@Test
	public void shouldThrowExceptionIfJournalstatusIsNotSet() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("journalstatus must be set");
		journalpost.setJournalstatus(null);

		opprettJournalpostValidator.validate(journalpost);
	}

	//Brukere
	@Test
	public void shouldThrowExceptionIfNoBrukereOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("Journalpost must have at least one Bruker");
		journalpost.clearBrukere();
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerIdIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerId must be set");
		journalpost.getBrukere().iterator().next().setBrukerId(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfBrukerTypeIsNull() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("brukerType must be set");
		journalpost.getBrukere().iterator().next().setBrukerType(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	//DokumentInfo
	@Test
	public void shouldThrowExceptionIfNoDokumentInfoObjectOnJournalpost() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("dokumentInfo must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().setDokumentInfo(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoKategoriOnDocumentInfo() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("kategori must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setKategori(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoTittelOnDocumentInfo() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("tittel must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setTittel(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoBrevkodeOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Brevkode must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setBrevkode(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfNoDokumenttypeIdOnDocumentInfo() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("DokumenttypeId must be set");
		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setDokumenttypeId(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldNotThrowExceptionIfNoSensitivtOnDocumentInfo() {

		journalpost.getJournalpostDokumentInfoRelasjoner().iterator().next().getDokumentInfo().setSensitivt(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	//FilDetaljer

	@Test
	public void shouldThrowExceptionIfNoFildetaljer() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Fildetaljer must be set");
		journalpost.findHoveddokumentDokumentInfoRelasjon().getDokumentInfo().clearFildetaljerListe();
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfVariantFormatArkivIsMissing() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("variantFormat must be set");
		journalpost.findAllFilDetaljer().get(0).setVariantFormat(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExcpetionIfNoFilTypeOnFildetaljerOnJournalpost() {
		expected.expect(InvalidArgumentException.class);
		expected.expectMessage("filtype must be set");
		journalpost.findAllFilDetaljer().get(0).setFiltype(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExcpetionIfMetaforceInstanceIdIsMissing() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("MetaforceInstanceId must be set");
		journalpost.findAllFilDetaljer().get(0).setMetaforceInstanceId(null);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExcpetionIfMetaforceInstanceIdIsZero() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("MetaforceInstanceId must be set");
		journalpost.findAllFilDetaljer().get(0).setMetaforceInstanceId(0L);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldPassIfJournalStatusD() {
		journalpost.setJournalstatus(JournalStatusCode.D);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalStatusNotD() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Expected journalstatus D, got A");
		journalpost.setJournalstatus(JournalStatusCode.A);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldPassIfJournalpostTypeU() {
		journalpost.setJournalposttype(JournalpostTypeCode.U);
		opprettJournalpostValidator.validate(journalpost);
	}

	@Test
	public void shouldThrowExceptionIfJournalpostTypeNotU() {
		expected.expect(ApplicationException.class);
		expected.expectMessage("Expected journalpostType U, got I");
		journalpost.setJournalposttype(JournalpostTypeCode.I);
		opprettJournalpostValidator.validate(journalpost);
	}

	private Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.saksrelasjon(
						getSaksrelasjonBuilder()
								.sakId("1")
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
