package no.nav.dokarkiv.journalpost.v1.api;

import java.time.LocalDateTime;

public record AvsluttAlleSakerPaaTemaRequest(
		String tema,
		String referanse,
		LocalDateTime avsluttetDato,
		String administrativEnhet
) {
}