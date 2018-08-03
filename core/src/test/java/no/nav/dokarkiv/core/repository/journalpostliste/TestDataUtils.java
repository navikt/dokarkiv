package no.nav.dokarkiv.core.repository.journalpostliste;

import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.ChangeStampBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * Class for generating test data for Joark repository tests
 *
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
public class TestDataUtils {

    private static final FagsystemCode fagsystem = FagsystemCode.PEN;
    private static final FagomradeCode fagomrade = FagomradeCode.PEN;
    private static final DateTime journalDato = new DateTime(2016, 5, 1, 0, 0);
    private static final BrukerTypeCode brukerType = BrukerTypeCode.PERSON;
    private static final JournalStatusCode journalStatus = JournalStatusCode.J;
    private static final String journalfEnhet = "test";
    private static Boolean isFeilregistrert = null;
    private static final JournalpostTypeCode journalpostType = JournalpostTypeCode.U;

    public static JournalpostBuilder createJournalpostWithSaksrelasjon(String saksnr, boolean isFeilregistrert, FagomradeCode fagomrade,
                                                                       FagsystemCode fagsystem, JournalpostTypeCode journalpostType) {
        return JournalpostBuilder.getJournalpostBuilder()
                .fagomrade(fagomrade == null ? TestDataUtils.fagomrade : fagomrade)
                .journalStatus(journalStatus)
                .journalpostType(journalpostType)
                .journalDato(journalDato.toDate())
                .saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
                        .opprettetKildeNavn("test")
                        .sakId(saksnr)
                        .fagsystem(fagsystem == null ? TestDataUtils.fagsystem : fagsystem)
                        .feilregistrert(isFeilregistrert)
                        .build())
                .brukere(BrukerBuilder.getBrukerBuilder()
                        .brukerId("1")
                        .brukerType(brukerType)
                        .opprettetKildeNavn("test")
                        .build())
                .dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                        .opprettetKildeNavn("test")
                        .tilknyttetAvNavn("test")
                        .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                                .dokumentstatus(DokumentStatusCode.FERDIGSTILT)
                                .opprettetKildeNavn("test")
                                .build())
                        .build())
                .opprettetKildeNavn("test")
                .journalForendeEnhetId(journalfEnhet);
    }

    public static JournalpostBuilder createJournalpost(String saksNr, Date journalDato, JournalStatusCode journalStatusCode, FagomradeCode fagomrade) {
        return JournalpostBuilder.getJournalpostBuilder()
                .saksrelasjon(SaksrelasjonBuilder.getSaksrelasjonBuilder()
                        .opprettetKildeNavn("test")
                        .sakId(saksNr)
                        .fagsystem(fagsystem)
                        .feilregistrert(isFeilregistrert)
                        .build())
                .brukere(BrukerBuilder.getBrukerBuilder()
                        .brukerId("1")
                        .brukerType(brukerType)
                        .opprettetKildeNavn("test")
                        .build())
                .dokumentInfoRelasjoner(JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder()
                        .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                        .opprettetKildeNavn("test")
                        .tilknyttetAvNavn("test")
                        .dokumentInfo(DokumentInfoBuilder.getDokumentInfoBuilder()
                                .dokumentstatus(DokumentStatusCode.FERDIGSTILT)
                                .opprettetKildeNavn("test")
                                .filDetaljerList(FilDetaljerBuilder.getFilDetaljerBuilder()
                                        .filtype(FilTypeCode.PDF)
                                        .filUuid("uuid")
                                        .variantFormat(VariantFormatCode.ARKIV)
                                        .opprettetKildeNavn("test")
                                        .build()
                                )
                                .build())
                        .build())
                .journalDato(journalDato)
                .changeStamp(ChangeStampBuilder.aChangeStamp().withCreatedDate(journalDato).withCreatedBy("test").build())
                .fagomrade(fagomrade == null ? TestDataUtils.fagomrade : fagomrade)
                .journalStatus(journalStatusCode == null ? TestDataUtils.journalStatus : journalStatusCode)
                .journalpostType(journalpostType)
                .opprettetKildeNavn("test")
                .journalForendeEnhetId(journalfEnhet);
    }

}
