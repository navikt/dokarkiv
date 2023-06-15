package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonInclude(NON_NULL)
public record Journalpost(
		long journalpostId,
		FagomradeCode fagomraade,
		String fagomraadenavn,
		JournalStatusCode status,
		JournalpostTypeCode type,
		String kanalreferanseId,
		MottaksKanalCode mottakskanal,
		UtsendingsKanalCode utsendingskanal,
		AvsenderMottaker avsenderMottaker,
		InnsynCode innsyn,
		SkjermingTypeCode skjerming,
		RelevanteDatoer relevanteDatoer,
		List<Tilleggsopplysning> tilleggsopplysninger,

		Bruker bruker,
		Saksrelasjon saksrelasjon,
		List<Dokumentinfo> dokumenter,

		// Ikke brukt av safselvbetjening
		String behandlingstemakode,
		String behandlingstemanavn,
		String innhold,
		String journalfoerendeEnhet,
		String journalfoertAvNavn,
		String opprettetAvNavn,
		String antallRetur,
		Long nextJournalpostId,
		Long totaltAntall
) {
}

@JsonInclude(NON_NULL)
record RelevanteDatoer(
		Date forsendelseMottatt, // mottattDato
		Date opprettet, // datoOpprettet
		Date hoveddokument, // dokumentDato
		Date journalfoert, // journalDato
		Date sendtPrint, // sendtPrintDato
		Date ekspedert, // ekspedertDato
		Date retur // avsReturDato
) {
}

record Tilleggsopplysning(
		String noekkel,
		String verdi
) {
}

@JsonInclude(NON_NULL)
record AvsenderMottaker(
		String id,
		AvsenderMottakerIdTypeCode type,

		// Brukt av saf
		String navn,
		String land
) {
}

@JsonInclude(NON_NULL)
record Saksrelasjon(
		String sakId,
		FagsystemCode fagsystem,
		Boolean feilregistrert,
		Sak sak
) {
}

record Sak(
		String aktoerId,
		String tema,
		String fagsakNr,
		String applikasjon,
		String orgNr,
		String opprettetAv,
		LocalDateTime opprettetTidspunkt
) {
}

record Bruker(
		String id,
		String type
) {
}

@JsonInclude(NON_NULL)
record Dokumentinfo(
		Long dokumentinfoId,
		DokumentStatusCode status,
		Date datoFerdigstilt,
		String brevkode,
		String dokumenttypeId,
		String tittel,
		SkjermingTypeCode skjerming,
		Long originalJournalpostId,
		Boolean kassert,
		DokumentKategoriCode kategori,
		List<Fildetaljer> fildetaljer,
		List<LogiskVedlegg> logiskeVedlegg
) {
}

@JsonInclude(NON_NULL)
record Fildetaljer(
		VariantFormatCode format,
		SkjermingTypeCode skjerming,
		String navn,
		String uuid,
		String type,
		String stoerrelse
) {
}

record LogiskVedlegg(
		String vedleggId,
		String tittel
) {
}