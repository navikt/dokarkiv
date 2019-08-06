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
@ApiOperation(
		value = "Oppretter en journalpost i fagarkivet, med eller uten dokumenter",
		authorizations = {@Authorization(value = "Authorization")})
@ApiResponses(value = {
		@ApiResponse(code = 201, message = "Created"),
		@ApiResponse(code = 400, message = "* Kan ikke opprette journalpost"),
		@ApiResponse(code = 401, message = "* Mangler tilgang til å opprette ny journalpost.\n* Ugyldig OIDC token. Denne feilen gis dersom tokenet ikke har riktig format eller er utgått."),
		@ApiResponse(code = 403, message = "Bruker mangler tilgang til å opprette journalpost på tema"),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerOpprettJournalpost {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
