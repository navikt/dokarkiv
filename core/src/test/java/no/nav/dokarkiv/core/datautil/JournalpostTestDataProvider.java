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

import java.time.LocalDateTime;
import java.time.Month;
import java.util.UUID;

import static no.nav.dokarkiv.core.datautil.DokumentFilTestDataProvider.FIL_UUID_SLADDET;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createDokumentInfo;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createHovedDokumentInfoFP;
import static no.nav.dokarkiv.core.datautil.DokumentInfoTestDataProvider.createVedleggDokumentInfo;
import static no.nav.dokarkiv.core.datautil.JournalpostDokumentInfoRelasjonTestDataProvider.createHoveddokumentRelasjon;
import static no.nav.dokarkiv.core.datautil.SaksrelasjonTestDataProvider.PEN_SAK_ID;
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
	public static final String FNR = "12341234123";
	public static final LocalDateTime JANUARY_1_2020 = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0, 0);
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

	public static JournalpostBuilder createJournalpost(String dokumentTittel, String filuid, LocalDateTime hoveddokumentFerdigDato) {
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
				.changeStamp(new ChangeStamp("test"))
				.opprettetAvNavn("testuser")
				.opprettetKildeNavn("test")
				.mottakskanal(NAV_NO)
				.mottattDato(JANUARY_1_2020)
				.journalDato(JANUARY_1_2020)
				.innhold(JP_INNHOLD)
				.saksrelasjon(createSaksrelasjon().build());
	}

	public static JournalpostBuilder buildJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus) {
		return buildJournalpost(journalpostType, journalStatus, PEN_SAK_ID);
	}

	public static JournalpostBuilder buildJournalpost(JournalpostTypeCode journalpostType, JournalStatusCode journalStatus, Long sakId) {
		return getJournalpostBuilder()
				.addOriginalJournalpost(true)
				.avsenderMottakerId("1")
				.kanalReferanseId("kanalreferanseId-" + UUID.randomUUID())
				.mottattDato(JANUARY_1_2020)
				.mottakskanal(NAV_NO)
				.fagomrade(FagomradeCode.PEN)
				.journalStatus(journalStatus)
				.journalpostType(journalpostType)
				.saksrelasjon(SaksrelasjonTestDataProvider.createPENSaksrelasjonWithSak(sakId))
				.brukere(BrukerTestDataProvider.createBruker("11111111111", BrukerTypeCode.PERSON), BrukerTestDataProvider.createBruker("999999999", BrukerTypeCode.ORGANISASJON))
				.innhold(INNHOLD)
				.journalForendeEnhetId("SesamStasjon")
				.avsenderMottaker("Bjarne Betjent")
				.opprettetAvNavn("Leonora Dorothea Dahl")
				.opprettetKildeNavn("itest")
				.dokumentDato(LocalDateTime.now())
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
