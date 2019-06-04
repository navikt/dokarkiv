package no.nav.dokarkiv.core.journalbehandling;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.KryssreferanseBuilder.getKryssreferanseBuilder;
import static no.nav.dokarkiv.core.domain.builder.ReturInfoBuilder.getReturInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SkannetInnholdBuilder.getSkannetInnholdBuilder;

import no.nav.dokarkiv.core.domain.codes.ArsakReturCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.ReferanseTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

/**
 * Unit tests for DefaultMandatoryFieldsVerifier. Validation failures is tested
 * in the domain object tests, so we only test happy cases here.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
public class DefaultMandatoryFieldsVerifierTest {

	private DefaultMandatoryFieldsVerifier mandatoryFieldsVerifier;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() {
		mandatoryFieldsVerifier = new DefaultMandatoryFieldsVerifier();
	}

	@Test
	public void shouldVerifyCompleteJournalpostUpdate() throws Exception {
		DokumentInfo dokumentInfo = createDokumentInfo();
		Journalpost journalpost = createJournalpost(dokumentInfo);

		mandatoryFieldsVerifier.verifyFields(journalpost);
	}

	@Test
	public void shouldVerifyJournalForendeEnhetIdWhenVerifyJournalForendeEnhetIdIsTrue() {
		expectedException.expect(InvalidArgumentException.class);
		DokumentInfo dokumentInfo = createDokumentInfo();
		Journalpost journalpost = createJournalpost(dokumentInfo);
		journalpost.setJournalForendeEnhetId(null);
		journalpost.setJournalstatus(JournalStatusCode.J);
		mandatoryFieldsVerifier.verifyFields(journalpost);
	}

	@Test
	public void shouldNotVerifyDokumentInfoFieldsWhenExistingDokumentInfoAttachedToJournalpost() throws Exception {
		Journalpost journalpost =
				getJournalpostBuilder()
						.journalStatus(JournalStatusCode.MO)
						.journalpostType(JournalpostTypeCode.U)
						.opprettetAvNavn("test")
						.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.tilknyttetAvNavn("Tilknyttet av")
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentInfoId(100L)
										.build())
								.build())
						.build();

		mandatoryFieldsVerifier.verifyFields(journalpost);
	}

	private Journalpost createJournalpost(DokumentInfo dokumentInfo) {
		return getJournalpostBuilder()
				.journalpostId(100L)
				.journalStatus(JournalStatusCode.J)
				.journalpostType(JournalpostTypeCode.I)
				.fagomrade(FagomradeCode.ERS)
				.innhold("Innhold")
				.avsenderMottaker("Navn")
				.journalForendeEnhetId("Enhet")
				.endretAvNavn("Endret av")
				.saksrelasjon(getSaksrelasjonBuilder()
						.saksrelasjonId(10L)
						.sakId("123")
						.fagsystem(FagsystemCode.FS22)
						.endretAvNavn("Endret av")
						.build())
				.brukere(getBrukerBuilder()
						.brukerId("***gammelt_fnr***")
						.brukerType(BrukerTypeCode.PERSON)
						.build())
				.returInfos(getReturInfoBuilder()
						.returInfoId(123L)
						.returDato(new Date())
						.arsakRetur(ArsakReturCode.FLYTTET_ADR_UKJ)
						.build())
				.kryssReferanser(getKryssreferanseBuilder()
						.referanseId("200000123")
						.referanseType(ReferanseTypeCode.SPOERSMAAL)
						.build())
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.journalpostDokumentInfoRelasjonId(89L)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
						.tilknyttetAvNavn("Tilknyttet av")
						.dokumentInfo(dokumentInfo)
						.build())
				.build();
	}

	private DokumentInfo createDokumentInfo() {
		return getDokumentInfoBuilder()
				.dokumentInfoId(45L)
				.kategori(DokumentKategoriCode.B)
				.tittel("tittel")
				.sensitivt(false)
				.endretAvNavn("Endret av")
				.skannetInnhold(getSkannetInnholdBuilder()
						.vedleggInnhold("Vedlegg")
						.build())
				.filDetaljerList(getFilDetaljerBuilder()
						.filtype(FilTypeCode.PDF)
						.variantFormat(VariantFormatCode.PRODUKSJON)
						.build())
				.build();
	}

}