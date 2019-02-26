package no.nav.dokarkiv.ferdigstilljournalpost.v1.config;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@ApiOperation("Ferdigstill journalpost")
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 400, message = "* Kan ikke ferdigstille"),
		@ApiResponse(code = 401, message = "* Bruker mangler tilgang for å vise dokumentet.\n* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerFerdigstillJournalpost {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
