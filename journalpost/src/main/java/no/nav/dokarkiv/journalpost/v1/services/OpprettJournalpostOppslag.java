package no.nav.dokarkiv.journalpost.v1.services;

/// Resultat av eksterne oppslag (PDL/Ereg) som gjøres før transaksjonen for opprettelse av journalpost starter.
///
/// @param aktoerIdForSak           aktørId brukt for å identifisere eller opprette arkivsak
/// @param avsenderMottakerNavn     navn på avsender/mottaker
/// @param brukerFolkeregisterIdent folkeregisterident for bruker oppgitt med aktørId
/// @param brukerIkkeFunnet         true dersom bruker oppgitt med aktørId ikke ble funnet i PDL
public record OpprettJournalpostOppslag(
		String aktoerIdForSak,
		String avsenderMottakerNavn,
		String brukerFolkeregisterIdent,
		boolean brukerIkkeFunnet
) {
}
