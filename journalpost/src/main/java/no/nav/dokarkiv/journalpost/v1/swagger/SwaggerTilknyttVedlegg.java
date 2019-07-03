package no.nav.dokarkiv.journalpost.v1.swagger;

import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Documented;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiOperation(hidden = true, value = "Knytter ett eller flere eksisterende dokumenter til en utgående journalpost som vedlegg")
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 207, message = "Delvis ok (Multi-Status). Dokumentene som ikke lot seg knytte til journalpost som vedlegg returneres som en feiledeDokumenter-liste, med årsakskode."),
		@ApiResponse(code = 401, message = "Konsument har ikke tilgang til å kalle tjenesten."),
		@ApiResponse(code = 404, message = " Journalpost finnes ikke eller er utilgjengelig"),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerTilknyttVedlegg {
	@AliasFor(
			annotation = RequestMapping.class
	)
	String value() default "";
}
