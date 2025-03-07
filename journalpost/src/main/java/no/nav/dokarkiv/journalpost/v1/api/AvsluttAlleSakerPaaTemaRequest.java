package no.nav.dokarkiv.journalpost.v1.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.NOT_REQUIRED;
import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record AvsluttAlleSakerPaaTemaRequest(
		@Schema(
				description = "Tre-bokstavers kode for tema/fagområde/arkivdel som skal avsluttes.",
				requiredMode = REQUIRED,
				example = "FAR")
		String tema,

		@Schema(
				description = "Jira-saksid (referanse til bestillingen)",
				requiredMode = REQUIRED,
				example = "MMA-9876"
		)
		String referanse,

		@Schema(
				description = "Tidspunkt når sakene ble avsluttet. Settes kun dersom alle sakene på tema skal merkes med samme avsluttet dato.",
				requiredMode = NOT_REQUIRED,
				pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS",
				example = "2025-03-07T10:58:53.470892300"
		)
		LocalDateTime avsluttetDato,

		@Schema(
				description = "Navn på enhet som er ansvarlig for sakene. Settes kun dersom alle sakene på tema skal merkes med samme administrative enhet.",
				requiredMode = NOT_REQUIRED,
				example = "5544"
		)
		String administrativEnhet
) {
}