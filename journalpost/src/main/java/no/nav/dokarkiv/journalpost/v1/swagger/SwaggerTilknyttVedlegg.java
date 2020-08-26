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
@ApiOperation(value = "Knytter ett eller flere eksisterende dokumenter til en utgående journalpost som vedlegg",
		authorizations = {@Authorization(value = "Authorization"), @Authorization(value = "NavConsumerToken")})
@ApiResponses(value = {
		@ApiResponse(code = 200, message = "OK"),
		@ApiResponse(code = 207, message = "Delvis ok (Multi-Status). Dokumentene som ikke lot seg knytte til journalpost som vedlegg returneres som en feiledeDokumenter-liste, med årsakskode."),
		@ApiResponse(code = 401, message = "Konsument har ikke tilgang til å kalle tjenesten."),
		@ApiResponse(code = 403, message = "Konsument har ikke tilgang til å kalle tjenesten"),
		@ApiResponse(code = 404, message = " Journalpost finnes ikke eller er utilgjengelig"),
		@ApiResponse(code = 500, message = "Internal server error")})
public @interface SwaggerTilknyttVedlegg {
}
