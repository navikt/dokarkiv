package no.nav.dokarkiv.behandlejournal.v3;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;

import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.journalbehandling.JournalpostStructureVerifier;
import no.nav.dokarkiv.core.journalbehandling.MandatoryFieldsVerifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;

import java.util.Date;

/**
 * Base class for JournalpostValidator tests.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public abstract class AbstractBehandleJournalV3JournalpostValidatorTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();

	@Mock
	protected MandatoryFieldsVerifier mandatoryFieldsVerifierMock;

	@Mock
	protected JournalpostStructureVerifier journalpostStructureVerifierMock;

	protected Journalpost journalpost;

	@Before
	public void init() {
		journalpost = createJournalpost();
	}

	protected void validateAndAssertExceptionThrownWithMessage(BehandleJournalJournalpostValidator validator,
															   Class<? extends Exception> exception, String message) {
		expected.expect(exception);
		expected.expectMessage(message);
		validator.validate(journalpost);
	}

	protected Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build())
				.saksrelasjon(getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.BID).build())
				.signatur(true)
				.opprettetAvNavn("opprettetAvNavn")
				.innhold("innhold")
				.dokumentDato(new Date())
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.PSELV)
				.utsendingskanal(UtsendingsKanalCode.ALTINN)
				.fagomrade(FagomradeCode.AAP)
				.journalStatus(JournalStatusCode.FS)
				.journalForendeEnhetId("9999")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentInfoId(1L)
										.sensitivt(true)
										.dokumenttypeId("dokumenttypeId")
										.tittel("tittel")
										.kategori(DokumentKategoriCode.ES)
										.innskrenketPartsinnsyn(true)
										.organInternt(true)
										.brevkode("brevkode")
										.filDetaljerList(getFilDetaljerBuilder()
												.filtype(FilTypeCode.PDF)
												.fileContent("file".getBytes())
												.variantFormat(VariantFormatCode.ARKIV)
												.build())
										.build())
								.build())
				.build();
	}

	protected Journalpost createJournalpostWithoutSensitivtDokInfo() {
		return getJournalpostBuilder()
				.avsenderMottakerId("***gammelt_fnr***")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("***gammelt_fnr***").build())
				.saksrelasjon(getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.BID).build())
				.signatur(true)
				.opprettetAvNavn("opprettetAvNavn")
				.innhold("innhold")
				.dokumentDato(new Date())
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.PSELV)
				.utsendingskanal(UtsendingsKanalCode.ALTINN)
				.fagomrade(FagomradeCode.AAP)
				.journalStatus(JournalStatusCode.FS)
				.journalForendeEnhetId("9999")
				.dokumentInfoRelasjoner(
						getJournalpostDokumentInfoRelasjonBuilder()
								.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
								.dokumentInfo(getDokumentInfoBuilder()
										.dokumentInfoId(1L)
										.dokumenttypeId("dokumenttypeId")
										.tittel("tittel")
										.kategori(DokumentKategoriCode.ES)
										.innskrenketPartsinnsyn(true)
										.organInternt(true)
										.brevkode("brevkode")
										.filDetaljerList(getFilDetaljerBuilder()
												.filtype(FilTypeCode.PDF)
												.fileContent("file".getBytes())
												.variantFormat(VariantFormatCode.SLADDET)
												.build())
										.build())
								.build())
				.build();
	}

	protected void addDuplicatesOfVariantFormats(Journalpost journalpost) {
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
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

	protected void addNonUniqueRelasjon(Journalpost journalpost) {
		journalpost.addJournalpostDokumentInfoRelasjon(getJournalpostDokumentInfoRelasjonBuilder().dokumentInfo(
				getDokumentInfoBuilder()
						.dokumentInfoId(1L)
						.dokumenttypeId("dokumentTypeId")
						.sensitivt(true)
						.tittel("tittel")
						.kategori(DokumentKategoriCode.ES)
						.filDetaljerList(
								getFilDetaljerBuilder()
										.variantFormat(VariantFormatCode.ARKIV)
										.filtype(FilTypeCode.AFP)
										.fileContent("test".getBytes())
										.build())
						.build())
				.build());
	}

	protected void addHoveddokument(Journalpost journalpost) {
		journalpost.addJournalpostDokumentInfoRelasjon(
				getJournalpostDokumentInfoRelasjonBuilder()
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.dokumentInfo(
								getDokumentInfoBuilder()
										.dokumentInfoId(2L)
										.dokumenttypeId("dokumentTypeId")
										.sensitivt(true)
										.tittel("tittel")
										.kategori(DokumentKategoriCode.ES)
										.filDetaljerList(
												getFilDetaljerBuilder()
														.variantFormat(VariantFormatCode.ARKIV)
														.filtype(FilTypeCode.AFP)
														.fileContent("test".getBytes())
														.build())
										.build())
						.build());
	}

	protected void replaceArkivVariant(Journalpost journalpost) {
		journalpost.findAllFilDetaljer().iterator().next().setVariantFormat(VariantFormatCode.SLADDET);
	}

}
