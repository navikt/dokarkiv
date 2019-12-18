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
@ApiOperation(value = "Finner ubehandlede journalposts som er eldre enn 1(en) uke",
		authorizations = {@Authorization(value = "Authorization"), @Authorization(value = "NavConsumerToken")})
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 400, message = "Kall mangler enn eller flere påkrevde headere"),
		@ApiResponse(code = 401, message = "Konsument har ikke tilgang til å kalle tjenesten."),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerFinnMottatteJournalposter {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
