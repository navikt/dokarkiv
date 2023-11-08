package no.nav.dokarkiv.core.exceptions;

import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@ControllerAdvice
public class DokarkivRestExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler({ConstraintViolationException.class})
	@ResponseStatus(BAD_REQUEST)
	public ResponseEntity<Object> handleConstraintViolationException(Exception err) {
		Map<String, Object> responseBody = new HashMap<>();
		logger.warn("Feilet med feilmelding=" + err.getMessage(), err);
		responseBody.put("message", err.getMessage());
		responseBody.put("status", BAD_REQUEST);
		return new ResponseEntity<>(responseBody, BAD_REQUEST);
	}

	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		if (ex.getCause() instanceof InvalidFormatException ife)
			return handleInvalidFormatException(ex, ife);

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
}
