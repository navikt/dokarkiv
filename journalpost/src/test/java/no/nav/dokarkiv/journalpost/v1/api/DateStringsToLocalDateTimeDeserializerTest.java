package no.nav.dokarkiv.journalpost.v1.api;

import tools.jackson.databind.DatabindException;
import tools.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateStringsToLocalDateTimeDeserializerTest {

	private static final JsonMapper JSON_MAPPER = new JsonMapper();
	private static final String EXPECTED_LOCAL_DATETIME_SECONDS = "2025-04-09T09:12:55";
	private static final String EXPECTED_LOCAL_DATETIME = "2025-04-09T09:12:55.271";
	private static final String EXPECTED_LOCALDATETIME_STARTOFDAY = "2025-04-09T00:00";

	@ParameterizedTest
	@MethodSource("deserializeToLocalDateTimeProvider")
	void shouldDeserializeToLocalDateTime(String dateVal, String expectedLocalDateTime) throws Exception {
		String json = """
				{
				  "legacyDate": $legacyDate$
				}
				""".replace("$legacyDate$", dateVal);

		ApiModell apiModell = JSON_MAPPER.readValue(json, ApiModell.class);
		assertThat(apiModell.getLegacyDate().toString()).isEqualTo(expectedLocalDateTime);
	}

	private static Stream<Arguments> deserializeToLocalDateTimeProvider() {
		return Stream.of(
				Arguments.of("1744182775271", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09\"", EXPECTED_LOCALDATETIME_STARTOFDAY),
				Arguments.of("\"2025-04-09T07:12:55+0000\"", EXPECTED_LOCAL_DATETIME_SECONDS),
				Arguments.of("\"2025-04-09T09:12:55.271\"", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09T07:12:55.271Z\"", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09T09:12:55.271+02:00\"", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09T07:12:55.271+00:00\"", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09T07:12:55.271+0000\"", EXPECTED_LOCAL_DATETIME),
				Arguments.of("\"2025-04-09T07:12:55.271000000Z\"", EXPECTED_LOCAL_DATETIME)
		);
	}

	@Data
	private static class ApiModell {
		@JsonDeserialize(using = DateStringsToLocalDateTimeDeserializer.class)
		LocalDateTime legacyDate;
	}

	@Test
	void shouldThrowJsonMappingException() {
		String json = """
				{
				  "legacyDate": "01.01.2025"
				}
				""";
		assertThatThrownBy(() -> JSON_MAPPER.readValue(json, ApiModell.class))
				.isInstanceOf(DatabindException.class)
				.hasMessageContaining("Klarte ikke parse tekst=01.01.2025 til LocalDateTime");
	}
}
