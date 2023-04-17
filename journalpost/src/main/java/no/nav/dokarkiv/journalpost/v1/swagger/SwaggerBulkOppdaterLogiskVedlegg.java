package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import no.nav.dokarkiv.core.domain.entities.SkannetInnhold;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
		summary = "Bulk oppdaterer logisk vedlegg for dokumentInfoId. Oppdaterer titler til input. Tom liste fjerner alle logiske vedlegg."
)
@ApiResponses(value = {
		@ApiResponse(responseCode = "204", description = "No Content - Ressursen oppdateres. Ingen respons i body."),
		@ApiResponse(responseCode = "400", description = "Bad Request - Input feilet validering. Eksempler: dokumentInfoId er ikke et tall eller tittel er lenger enn " + SkannetInnhold.VEDLEGG_INNHOLD_LENGTH + " tegn.", content = @Content),
		@ApiResponse(responseCode = "401", description = "Unauthorized - Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått.", content = @Content),
		@ApiResponse(responseCode = "404", description = "Not Found - DokumentInfo for dokumentInfoId ikke funnet.", content = @Content),
		@ApiResponse(responseCode = "500", description = "Internal Server Error - Intern uhåndtert teknisk feil.", content = @Content)
})
public @interface SwaggerBulkOppdaterLogiskVedlegg {
}
