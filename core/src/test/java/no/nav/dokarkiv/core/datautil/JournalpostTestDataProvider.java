package no.nav.dokarkiv.core.datautil;

import no.nav.dokarkiv.core.domain.ChangeStamp;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID_SLADDET;
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

/**
 * Provides helpers for building {@link JournalpostBuilder}-instances
 */
public final class JournalpostTestDataProvider {

	public static final String JP_AVSENDER_MOTTAKER_ID = "avsenderMottakerId";
	public static final String JP_AVSENDER_MOTTAKER = "test";
	public static final FagomradeCode JP_FAGOMRADE = FagomradeCode.PEN;
	public static final String JP_INNHOLD = "innhold";
	public static final JournalpostTypeCode JP_TYPE = U;
	public static final String FNR = "12341234123";
	public static final Date JANUARY_1_2020 = Date.from(LocalDate.of(2020, Month.JANUARY, 1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant());
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
		return createJournalpost(createDokumentInfo(dokumentTittel, filuid, FIL_UUID_SLADDET));
	}

	public static JournalpostBuilder createJournalpost(String dokumentTittel, String filuid, Date hoveddokumentFerdigDato) {
		return createJournalpost(createDokumentInfo(dokumentTittel, filuid, FIL_UUID_SLADDET).dokumentFerdigDato(hoveddokumentFerdigDato));
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
				.addOriginalJournalpost(true)
				.avsenderMottakerId("1")
				.kanalReferanseId("kanalreferanseId-" + UUID.randomUUID())
				.mottattDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
				.mottakskanal(NAV_NO)
				.fagomrade(FagomradeCode.PEN)
				.journalStatus(journalStatus)
				.journalpostType(journalpostType)
				.saksrelasjon(SaksrelasjonTestDataProvider.createPENSaksrelasjon())
				.brukere(BrukerTestDataProvider.createBruker("11111111111", BrukerTypeCode.PERSON), BrukerTestDataProvider.createBruker("999999999", BrukerTypeCode.ORGANISASJON))
				.innhold(INNHOLD)
				.journalForendeEnhetId("SesamStasjon")
				.avsenderMottaker("Bjarne Betjent")
				.opprettetAvNavn("Leonora Dorothea Dahl")
				.opprettetKildeNavn("itest")
				.dokumentDato(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()))
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
