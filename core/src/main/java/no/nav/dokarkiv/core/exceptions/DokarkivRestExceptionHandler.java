package no.nav.dokarkiv.core.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

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
}
