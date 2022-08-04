package no.nav.dokarkiv.behandlejournal.v2;

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
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import java.util.Date;

import static no.nav.dokarkiv.core.domain.builder.BrukerBuilder.getBrukerBuilder;
import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder.getFilDetaljerBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder.getSaksrelasjonBuilder;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Base class for JournalpostValidator tests.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
public abstract class AbstractBehandleJournalJournalpostValidatorTest {

	@Mock
	protected MandatoryFieldsVerifier mandatoryFieldsVerifierMock;

	@Mock
	protected JournalpostStructureVerifier journalpostStructureVerifierMock;

	protected Journalpost journalpost;

	@BeforeEach
	public void init() {
		journalpost = createJournalpost();
	}

	protected void validateAndAssertExceptionThrownWithMessage(BehandleJournalJournalpostValidator validator,
															   Class<? extends Exception> exception, String message) {
		assertThrows(exception,
				() -> validator.validate(journalpost),
				message);
	}

	protected Journalpost createJournalpost() {
		return getJournalpostBuilder()
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").build())
				.saksrelasjon(getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
				.signatur(true)
				.opprettetAvNavn("opprettetAvNavn")
				.innhold("innhold")
				.dokumentDato(new Date())
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
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
				.avsenderMottakerId("01054512313")
				.avsenderMottaker("avsender")
				.brukere(getBrukerBuilder().brukerId("01054512313").build())
				.saksrelasjon(getSaksrelasjonBuilder().sakId("1").fagsystem(FagsystemCode.FS22).build())
				.signatur(true)
				.opprettetAvNavn("opprettetAvNavn")
				.innhold("innhold")
				.dokumentDato(new Date())
				.mottattDato(new Date())
				.journalpostType(JournalpostTypeCode.I)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.utsendingskanal(UtsendingsKanalCode.EESSI)
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
