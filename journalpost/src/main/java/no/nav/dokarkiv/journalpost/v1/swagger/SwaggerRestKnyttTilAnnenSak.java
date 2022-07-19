package no.nav.dokarkiv.journalpost.v1.swagger;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK - alle dokumenter ble knyttet til ny journalpost."),
        @ApiResponse(responseCode = "400", description = "Feil - kopierjournalpostintern eller oppdaterjournalpost feilet.", content = @Content),
        @ApiResponse(responseCode = "403", description = "Feil - bruker har ikke tilgang til journalpost.", content = @Content),
        @ApiResponse(responseCode = "404", description = "Feil - journalpost finnes ikke eller er utilgjengelig.", content = @Content),
        @ApiResponse(responseCode = "500", description = "Teknisk feil.", content = @Content)
}
)
public @interface SwaggerRestKnyttTilAnnenSak {
}
