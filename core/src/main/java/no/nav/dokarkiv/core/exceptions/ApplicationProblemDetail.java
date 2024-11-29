package no.nav.dokarkiv.core.exceptions;

import lombok.Getter;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Tilpasset RFC 9457 ProblemDetail subklasse
 * Inkluderer felt for klient-kompatibilitet med errors fra Spring Boot 2 versjoner av dokarkiv
 */
@Getter
public class ApplicationProblemDetail extends ProblemDetail {

	private ZonedDateTime timestamp;
	private String message;
	private String error;
	private URI path;

	/**
	 * Deserialisering
	 */
	protected ApplicationProblemDetail() {
	}

	public ApplicationProblemDetail(ProblemDetail other, URI instance) {
		super(other);
		this.timestamp = ZonedDateTime.now(ZoneId.of("Europe/Oslo"));
		this.error = getTitle();
		this.message = getDetail();
		setInstance(instance);
		if (getInstance() == null) {
			this.path = null;
		} else {
			this.path = getInstance();
		}
	}
}
