package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static com.fasterxml.jackson.core.JsonToken.VALUE_NUMBER_INT;
import static com.fasterxml.jackson.core.JsonToken.VALUE_STRING;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;

/**
 * Deserialiserer string og heltall varianter av {@link java.util.Date} som er brukt i produksjon med {@link com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer}
 *
 * Introdusert pga deserialisering-klassen for LocalDateTime ikke støtter blant annet kun ISO_DATE eller unix time. Dette ifbm overgangen fra Date typer i Rest-API til java.time typer.
 * Deserialiserer til norsk tidssone Europe/Oslo
 */
public class DateStringsToLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

	@SuppressWarnings("unused")
	public DateStringsToLocalDateTimeDeserializer() {
		this(OffsetDateTime.class);
	}

	protected DateStringsToLocalDateTimeDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
		if (parser.hasToken(VALUE_STRING)) {
			return parseString(parser);
		} else if (parser.hasToken(VALUE_NUMBER_INT)) {
			long epochTimeMillis = _parseLongPrimitive(context, parser.getText());
			return Instant.ofEpochMilli(epochTimeMillis).atZone(ZONEID_NORGE).toLocalDateTime();
		}
		String message = "Klarte ikke parse token=%s, tokenId=%d til LocalDateTime".formatted(parser.getText(), parser.currentTokenId());
		throw JsonMappingException.from(parser, message);
	}

	private static LocalDateTime parseString(JsonParser jsonParser) throws IOException {
		String text = jsonParser.getText().trim();
		if (text.isEmpty()) {
			return null;
		}
		return parseDateString(jsonParser, text);
	}

	private static LocalDateTime parseDateString(JsonParser jsonParser, String dateString) throws JsonMappingException {
		try {
			return LocalDate.parse(dateString).atStartOfDay();
		} catch (DateTimeParseException ignored) {
			// Try parsing as OffsetDateTime
		}

		try {
			return OffsetDateTime.parse(dateString).atZoneSameInstant(ZONEID_NORGE).toLocalDateTime();
		} catch (DateTimeParseException ignored) {
			// Try parsing as LocalDateTime
		}

		try {
			return LocalDateTime.parse(dateString);
		} catch (DateTimeParseException e) {
			String message = "Klarte ikke parse tekst=%s til LocalDateTime".formatted(dateString);
			throw JsonMappingException.from(jsonParser, message);
		}
	}
}
