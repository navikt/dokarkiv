package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiOperation(value = "Fullfører journalføringen og låser journalposten for senere endringer",
        authorizations = {@Authorization(value = "Authorization"), @Authorization(value = "NavConsumerToken")})
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK - Journalposten fikk status ekspedert"),
        @ApiResponse(code = 400, message = "Kan bare sette status ekspedert, når:\n" +
                "*Journalpost er UTGÅENDE\n" +
                "*Journalpost har status FS eller FL\n" +
                "*Journalpost har en saksrelasjon som ikke er feilregistrert"),
        @ApiResponse(code = 401, message = "Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
        @ApiResponse(code = 403, message = "Konsument har ikke tilgang til å ekspedere journalpost"),
        @ApiResponse(code = 404, message = "Journalpost ikke funnet"),
        @ApiResponse(code = 500, message = "Uventet teknisk feil")})
public @interface SwaggerOppdaterDistribusjonsinfo {
}
