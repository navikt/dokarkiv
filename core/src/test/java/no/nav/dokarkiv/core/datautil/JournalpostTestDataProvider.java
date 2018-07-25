package no.nav.dokarkiv.core.datautil;

import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createDokumentInfo;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createHovedDokumentInfoFP;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.createSaksrelasjon;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import org.joda.time.LocalDateTime;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

/**
 * Provides helpers for building {@link JournalpostBuilder}-instances
 *
 * @author Roar Bjurstrom, Visma Consulting.
 * @author Thomas Kåsene, Visma Consulting AS
 * @author Torgeir Cook, Visma Consulting AS
 */
public final class JournalpostTestDataProvider {

	public static final String JP_AVSENDER_MOTTAKER_ID = "avsenderMottakerId";
	public static final String JP_AVSENDER_MOTTAKER = "test";
	public static final FagomradeCode JP_FAGOMRADE = FagomradeCode.PEN;
	public static final String JP_INNHOLD = "innhold";
	public static final JournalpostTypeCode JP_TYPE = U;
	public static final String FNR = "***gammelt_fnr***";
	public static final Date JANUARY_1_2020 = Date.from(LocalDate.of(2020, Month.JANUARY, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
	public static final String INNHOLD = "Foreldrepenger";

	private JournalpostTestDataProvider() {
	}

	public static JournalpostBuilder createJournalpost(String filuid) {
		return createJournalpost(DokumentInfoTestDataProvider.DOKUMENT_TITTEL, filuid);
	}
	
	public static JournalpostBuilder createJournalpost(FagomradeCode fagomradeCode) {
		return createJournalpostWithoutHoveddokument(fagomradeCode)
				.dokumentInfoRelasjoner(createHoveddokumentRelasjon(createDokumentInfo().build()).build());	
		}

	public static JournalpostBuilder createJournalpost(String dokumentTittel, String filuid) {
		return createJournalpost(createDokumentInfo(dokumentTittel, filuid));
	}

	public static JournalpostBuilder createJournalpost(String dokumentTittel, String filuid, Date hoveddokumentFerdigDato) {
		return createJournalpost(createDokumentInfo(dokumentTittel, filuid).dokumentFerdigDato(hoveddokumentFerdigDato));
	}

	public static JournalpostBuilder createJournalpost(DokumentKategoriCode hoveddokumentKategori) {
		return createJournalpost(createDokumentInfo(hoveddokumentKategori));
	}

	public static JournalpostBuilder createJournalpost(DokumentInfoBuilder dokumentInfoBuilder) {
		return createJournalpostWithoutHoveddokument()
				.dokumentInfoRelasjoner(createHoveddokumentRelasjon(dokumentInfoBuilder.build()).build());
	}
	
	public static JournalpostBuilder createJournalpostWithoutHoveddokument() {
		return createJournalpostWithoutHoveddokument(JP_FAGOMRADE);
	}
	
	public static JournalpostBuilder createJournalpostWithoutHoveddokument(FagomradeCode fagomradeCode) {
		return getJournalpostBuilder()
				.journalStatus(FS)
				.journalpostType(U)
				.fagomrade(fagomradeCode)
				.avsenderMottaker(JP_AVSENDER_MOTTAKER)
				.avsenderMottakerId(JP_AVSENDER_MOTTAKER_ID)
				.changeStamp(new ChangeStamp(null, JANUARY_1_2020, null, null))
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.mottakskanal(NAV_NO)
				.mottattDato(JANUARY_1_2020)
				.journalDato(JANUARY_1_2020)
				.innhold(JP_INNHOLD)
				.saksrelasjon(createSaksrelasjon().build());
	}

	public static JournalpostBuilder buildJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
        return getJournalpostBuilder()
                .avsenderMottakerId("1")
                .mottattDato(LocalDateTime.now().toDate())
                .mottakskanal(NAV_NO)
                .fagomrade(FagomradeCode.PEN)
                .journalStatus(journalStatus)
                .journalpostType(journalpostType)
                .saksrelasjon(SaksrelasjonTestDataProvider.createPENSaksrelasjon())
                .brukere(BrukerTestDataProvider.createBruker("***gammelt_fnr***", BrukerTypeCode.PERSON), BrukerTestDataProvider.createBruker("999999999", BrukerTypeCode.ORGANISASJON))
                .innhold(INNHOLD)
				.journalForendeEnhetId("SesamStasjon")
                .avsenderMottaker("Bjarne Betjent")
                .opprettetKildeNavn("itest")
                .dokumentDato(LocalDateTime.now().toDate())
                .dokumentInfoRelasjoner(
                        getJournalpostDokumentInfoRelasjonBuilder()
                                .opprettetKildeNavn("itest")
                                .tilknyttetAvNavn("itest")
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
                                .dokumentInfo(createHovedDokumentInfoFP().build())
                                .build(),
                        getJournalpostDokumentInfoRelasjonBuilder()
                                .opprettetKildeNavn("itest")
                                .tilknyttetAvNavn("itest")
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.VEDLEGG)
                                .dokumentInfo(createVedleggDokumentInfo().build())
                                .build());
    }
}
