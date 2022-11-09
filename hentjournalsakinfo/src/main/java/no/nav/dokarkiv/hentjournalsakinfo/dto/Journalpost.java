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
		FagomradeCode fagomraade, // fagomrade
		JournalStatusCode status, // journalstatus
		JournalpostTypeCode type, // journalposttype
		RelevanteDatoer relevanteDatoer,
		MottaksKanalCode mottakskanal,
		UtsendingsKanalCode utsendingskanal,
		String kanalreferanseId, // kanalReferanseId
		InnsynCode innsyn, // innsyn i sak eller journalpost?
		SkjermingTypeCode skjerming, // skjerming på sak eller journalpost?
		AvsenderMottaker avsenderMottaker,
		List<Tilleggsopplysning> tilleggsopplysninger,

		// Brukt av saf
		String behandlingstemakode, // behandlingstema
		String behandlingstemanavn,
		Long nextJournalpostId,
		Long totaltAntall,
		String innhold, // kva er innhaldet?
		String journalfoerendeEnhet, // journalforendeEnhet
		String journalfoertAvNavn, // journalfortAvNavn
		String opprettetAvNavn,
		String antallRetur,

		Sak sak,
		Saksrelasjon saksrelasjon,
		Bruker bruker,
		List<Dokumentinfo> dokumenter
) {
}

@JsonInclude(NON_NULL)
record Sak(
		String id, // sakId
		String aktoerId,
		FagsystemCode fagsystem,
		String tema,
		String fagsakNr,
		String applikasjon,
		Boolean feilregistrert,
		String orgNr, //orgnr
		String opprettetAv,
		LocalDateTime opprettetTidspunkt
) {
}

@JsonInclude(NON_NULL)
record Saksrelasjon(
		FagsystemCode fagsystem,
		Boolean feilregistrert,
		LocalDateTime opprettetTidspunkt
) {
}

@JsonInclude(NON_NULL)
record RelevanteDatoer(
		Date opprettet, // datoOpprettet
		Date dokument, // dokumentDato
		Date journalfoert, // journalDato
		Date sendtPrint, // sendtPrintDato
		Date ekspedert, // ekspedertDato
		Date retur, // avsReturDato
		Date mottatt // mottattDato
) {
}

record Tilleggsopplysning(
		String noekkel, // nokkel
		String verdi
) {
}

@JsonInclude(NON_NULL)
record AvsenderMottaker(
		String id, // avsenderMottakerId
		AvsenderMottakerIdTypeCode type, // avsenderMottakerIdType

		// Brukt av saf
		String navn, // avsenderMottakerNavn
		String land // avsenderMottakerLand
) {
}

record Bruker(
		String id, // brukerId
		String type // brukerIdType
) {
}

@JsonInclude(NON_NULL)
record Dokumentinfo(
		Long id, // dokumentInfoId
		DokumentStatusCode status, // dokumentstatus
		Date datoFerdigstilt,
		String brevkode,
		String dokumenttypeId,
		String tittel,
		SkjermingTypeCode skjerming,
		Long originalJournalpostId, // origJournalpostId
		Boolean kassert,
		DokumentKategoriCode kategori,
		List<Variant> varianter,
		List<LogiskVedlegg> logiskeVedlegg, // logiske

		// Brukt av safselvbetjening - skal bli faset ut
		Boolean organinternt, // organInternt
		Boolean innskrenketPartsinnsyn, // innskrPartsinnsyn
		Boolean innskrenketTredjepart // innskrTredjepart
) {
}

@JsonInclude(NON_NULL)
record Variant(
		VariantFormatCode format, // variantf
		SkjermingTypeCode skjerming, // skjerming her også??
		String filnavn,
		String filuuid,
		String filtype,
		String filstoerrelse // filstorrelse
) {
}

record LogiskVedlegg(
		String vedleggId,
		String tittel
) {
}