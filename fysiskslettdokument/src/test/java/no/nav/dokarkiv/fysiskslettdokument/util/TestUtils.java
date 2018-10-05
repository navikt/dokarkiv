package no.nav.dokarkiv.fysiskslettdokument.util;

import static no.nav.dokarkiv.core.domain.builder.DokumentInfoBuilder.getDokumentInfoBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostBuilder.getJournalpostBuilder;
import static no.nav.dokarkiv.core.domain.builder.JournalpostDokumentInfoRelasjonBuilder.getJournalpostDokumentInfoRelasjonBuilder;

import no.nav.dokarkiv.core.domain.builder.FilDetaljerBuilder;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.TilknyttetJournalpostSomCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.Date;

public class TestUtils {

	private static final String OPPRETTET_KILDE_NAVN = "Opprettet kilde";
	private static final String OPPRETTET_AV_NAVN = "Opprettet navn";
	private static final String TILKNYTTET_AV_NAVN = "Tilknyttetnavn";
	private static final String ENDRET_AV_NAVN = "Endret av navn";
	private static final String AVSENDER_MOTTAKER_ID = "***gammelt_fnr***";
	private static final String DOKUMENT_TITTEL = "FysiskSlettDokument_Tittel";
	private static final String BREVGRUPPE = "Brevgruppe";
	private static final String BREVKODE = "Brevkode";
	private static final String FILNAVN = "filNavn";

	public static final Long JOURNALPOST_ID = 42L;
	public static final Long DOKUMENTINFO_ID = 1L;


//	public static LogiskSlettDokumentRequestTo createRequest(Long journalpostId, Long dokumentInfoId) {
//		return LogiskSlettDokumentRequestTo.builder()
//				.journalpostId(journalpostId)
//				.dokumentInfoId(dokumentInfoId)
//				.build();
//	}


	public static Journalpost createJournalpost(Boolean slettestatus) {
		return getJournalpostBuilder()
				.journalpostId(JOURNALPOST_ID)
				.journalStatus(JournalStatusCode.FL)
				.journalpostType(JournalpostTypeCode.U)
				.utsendingskanal(UtsendingsKanalCode.INGEN_DISTRIBUSJON)
				.dokumentDato(new Date())
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.fagomrade(FagomradeCode.RPO)
				.dokumentInfoRelasjoner(getJournalpostDokumentInfoRelasjonBuilder()
						.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
						.tilknyttetAvNavn(TILKNYTTET_AV_NAVN)
						.tilknyttetJournalpostSom(TilknyttetJournalpostSomCode.HOVEDDOKUMENT)
						.dokumentInfo(createDokumentInfo(slettestatus))
						.build())
				.build();
	}

	public static DokumentInfo createDokumentInfo(boolean sletteStatus) {
		return getDokumentInfoBuilder()
				.slettet(sletteStatus)
				.dokumentInfoId(DOKUMENTINFO_ID)
				.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
				.tittel(DOKUMENT_TITTEL)
				.endretAvNavn(ENDRET_AV_NAVN)
				.filDetaljerList(createFildetaljer())
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}

	private static FilDetaljer createFildetaljer() {
		return createFildetaljer(FilDetaljer.generateUuid());
	}

	private static FilDetaljer createFildetaljer(String filUuid) {
		return FilDetaljerBuilder.getFilDetaljerBuilder()
				.filUuid(filUuid)
				.filnavn(FILNAVN)
				.filtype(FilTypeCode.PDF)
				.variantFormat(VariantFormatCode.ARKIV)
				.opprettetKildeNavn(OPPRETTET_KILDE_NAVN)
				.build();
	}
}
