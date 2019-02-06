package no.nav.dokarkiv.core.util;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.builder.BrukerBuilder;
import no.nav.dokarkiv.core.domain.builder.ChangeStampBuilder;
import no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder;
import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostBuilder;
import no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder;
import no.nav.dokarkiv.core.domain.builder.SaksrelasjonBuilder;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.domain.entities.Begrensning;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.joda.time.DateTime;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for generating test data for Joark repository tests
 *
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
public class TestDataUtils {

	public static final FagsystemCode fagsystem = FagsystemCode.PEN;
	private static final FagomradeCode fagomrade = FagomradeCode.PEN;
	private static final DateTime journalDato = new DateTime(2016, 5, 1, 0, 0);
	private static final BrukerTypeCode brukerType = BrukerTypeCode.PERSON;
	private static final JournalStatusCode journalStatus = JournalStatusCode.J;
	private static final String journalfEnhet = "test";
	private static Boolean isFeilregistrert = null;
	private static final JournalpostTypeCode journalpostType = JournalpostTypeCode.U;

	public static final String KANAL_REFERANSE_ID = "kanal";
	public static final String TILLEGGSOPPLYSNINGER_KEY = "keey";
	public static final String TILLEGGSOPPLYSNINGER_VALUE = "value";

	public static final String AKSJON_APPLIKASJON = "Dokarkiv";
	public static final String AKSJON_HJEMMEL = "POL";
	public static final String AKSJON_UTFOERT_AV = "Z142455";
	public static final String AKSJON_BRUKER = "144411133";
	public static final String AKSJON_MELDING = "Test";
	public static final String USER_ID = "Z12323213";
	public static final String APPLICATION = "srvtestbruker";
	public static final String AKSJON_FRA_VERDI = "Test1";
	public static final String AKSJON_TIL_VERDI = "Test2";
	public static final String AKSJON_ARKIVELEMENT= "Journalpost.journalpostId";

	public static AksjonsLoggTO createAksjonsLoggTO(Long journalpostId, Long dokumentInfoId) {
		return AksjonsLoggTO.builder()
				.aksjon(AksjonsTypeCode.ARKIVERING)
				.bruker(AKSJON_BRUKER)
				.melding(AKSJON_MELDING)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.hjemmel(AKSJON_HJEMMEL)
				.utfoertAv(AKSJON_UTFOERT_AV)
				.build();
	}

	public static AksjonsLoggTO createAksjonsLoggTOHeader() {
		return AksjonsLoggTO.builder()
				.bruker(AKSJON_BRUKER)
				.melding(AKSJON_MELDING)
				.hjemmel(AKSJON_HJEMMEL)
				.utfoertAv(AKSJON_UTFOERT_AV)
				.build();
	}

	public static List<ArkivElementEndringTO> createArkivElementEndringToList() {
		return Arrays.asList(
				ArkivElementEndringTO.builder()
						.arkivElement(AKSJON_ARKIVELEMENT)
						.fraVerdi(AKSJON_FRA_VERDI)
						.tilVerdi(AKSJON_TIL_VERDI)
						.build()
		);
	}

    public static Begrensning createBegrensning(Long journalpostId, Long dokumentInfoId, SkjermingTypeCode skjermingTypeCode) {

        Begrensning begrensning = Begrensning.builder()
                .begrensningType(skjermingTypeCode)
                .journalpostId(journalpostId)
                .dokumentInfoId(dokumentInfoId).build();

		begrensning.setOpprettetKildeNavn("test navn");

		return begrensning;
	}

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
		Map<String, String> tilleggsopplysninger = new HashMap<>();
		tilleggsopplysninger.put(TILLEGGSOPPLYSNINGER_KEY, TILLEGGSOPPLYSNINGER_VALUE);

		return JournalpostBuilder.getJournalpostBuilder()
				.addOriginalJournalpost(true)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.mottakskanal(MottaksKanalCode.NAV_NO)
				.tilleggsopplysninger(tilleggsopplysninger)
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
								.tilleggsopplysninger(tilleggsopplysninger)
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

	public static Journalpost createJournalpost() {
		return createJournalpost("123", DateTime.now().toDate(), JournalStatusCode.J, FagomradeCode.PEN).build();
	}

}
