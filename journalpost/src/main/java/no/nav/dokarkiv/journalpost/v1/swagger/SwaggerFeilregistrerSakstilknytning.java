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
@ApiOperation(value = "Feilregistrerer journalpostens knytning til sak",
        authorizations = {@Authorization(value = "Authorization"), @Authorization(value = "NavConsumerToken")})
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "* OK"),
        @ApiResponse(code = 400, message = "* Feil i aksjonslogg"),
        @ApiResponse(code = 401, message = "* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
        @ApiResponse(code = 404, message = "* Journalpost ikke funnet"),
        @ApiResponse(code = 500, message = "* Internal server error")})
public @interface SwaggerFeilregistrerSakstilknytning {
}
