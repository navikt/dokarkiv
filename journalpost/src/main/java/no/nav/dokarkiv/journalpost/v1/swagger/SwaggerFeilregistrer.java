package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiOperation(value = "Feilregistrer journalpost", authorizations = {@Authorization(value = "apiKey")})
@ApiResponses(value = {
        @ApiResponse(code = 200, message = "* OK"),
        @ApiResponse(code = 400, message = "* Ugyldig avvikstype\n" +
                "* Feil i aksjonslogg\n" +
                "* Saksrelasjonen er allerede feilregistrert\n" +
                "* Feilregistrering er ikke mulig fordi journalposten ikke er knyttet til sak\n" +
                "* Feilregistreringen er allerede opphevet\n" +
                "* Journalpost kan ikke settes til UB (ukjent bruker)\n" +
                "* Journalposten er allerede avbrutt\n" +
                "* Journalposten kan ikke avbrytes da den er ferdigstilt"),
        @ApiResponse(code = 401, message = "* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
        @ApiResponse(code = 404, message = "* Journalpost ikke funnet"),
        @ApiResponse(code = 500, message = "* Internal server error")})
public @interface SwaggerFeilregistrer{
    @AliasFor(
            annotation = RequestMapping.class
    )
    String value() default "";
}
