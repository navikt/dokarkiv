package no.nav.dokarkiv.core.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.EMPTY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
public class ApplicationServletExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ConstraintViolationException.class})
	public ResponseEntity<Object> handleConstraintViolationException(Exception err, WebRequest webRequest) {
		ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST, "Klient har sendt inn data som databasen avviser. " + err.getMessage());
		problemDetail.setTitle("Database Constraint violation");
		return createResponseEntity(problemDetail, EMPTY, BAD_REQUEST, webRequest);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		if (ex.getCause() instanceof InvalidFormatException ife) {
			return handleInvalidFormatException(ex, ife);
		}
		return handleExceptionInternal(ex, ex.getMessage(), headers, BAD_REQUEST, request);
	}

	private static ResponseEntity<Object> handleInvalidFormatException(HttpMessageNotReadableException e, InvalidFormatException invalidFormatException) {
		String feilmelding;
		var verdi = invalidFormatException.getValue();
		var feltType = invalidFormatException.getTargetType();
		var feltNavn = invalidFormatException.getPath().stream()
				.map(Reference::getFieldName)
				.collect(Collectors.joining("."));

		if (feltType.isEnum()) {
			feilmelding = format("Feltet %s=%s må være en av %s", feltNavn, verdi, Arrays.toString(feltType.getEnumConstants()));
		} else {
			feilmelding = format("'%s' er ikke en gyldig verdi for feltet %s", verdi, feltNavn);
		}
		return ResponseEntity.badRequest()
				.contentType(APPLICATION_JSON)
				.body(format("\"%s\"", feilmelding));
	}

	@Override
	protected ResponseEntity<Object> createResponseEntity(Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
		if (body instanceof ProblemDetail problemDetail) {
			if (request instanceof ServletWebRequest servletWebRequest) {
				return new ResponseEntity<>(new ApplicationProblemDetail(problemDetail, URI.create(servletWebRequest.getRequest().getRequestURI())), headers, statusCode);
			}
			return new ResponseEntity<>(new ApplicationProblemDetail(problemDetail, null), headers, statusCode);
		}
		return super.createResponseEntity(body, headers, statusCode, request);
	}
}
