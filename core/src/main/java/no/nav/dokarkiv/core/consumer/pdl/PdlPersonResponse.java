package no.nav.dokarkiv.core.consumer.pdl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Data
public class PdlPersonResponse {

	private PdlHentPersoner data;
	private List<PdlError> errors;

	@Data
	public static class PdlHentPersoner {
		private PdlPerson hentPerson;
	}

	@Data
	public static class PdlPerson {
		private List<PdlNavn> navn;
	}

	@Data
	public static class PdlNavn {
		@ToString.Exclude
		private String fornavn;
		private String mellomnavn;
		private String etternavn;

		public String getFulltNavn() {
			return Stream.of(fornavn, mellomnavn, etternavn).filter(n -> !isBlank(n)).collect(joining(" "));
		}
	}

	@Data
	@JsonIgnoreProperties({"locations", "path"})
	public static class PdlError {
		private String message;
		private no.nav.dokarkiv.core.consumer.pdl.PdlResponse.PdlErrorExtension extensions;
	}

	@Data
	public static class PdlErrorExtension {
		private String code;
		private String classification;
		private PdlErrorDetails details;
	}

	@Data
	public static class PdlErrorDetails {
		private String type;
		private String cause;
		private String policy;
	}
}


