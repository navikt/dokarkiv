package no.nav.dokarkiv.internal.dokvaktmester;

public record EndreFerdigstiltJournalpostRequest(
		String brukerId,
		EndreSak sak,
		String tema,
		String begrunnelseNokkel
) {

}
