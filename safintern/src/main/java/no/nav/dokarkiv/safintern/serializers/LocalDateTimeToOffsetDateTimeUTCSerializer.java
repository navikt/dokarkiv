package no.nav.dokarkiv.safintern.serializers;

import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_UTC;

/**
 * Serialiserer jackson-annotert json-felt fra LocalDateTime Europe/Oslo til OffsetDateTime UTC String
 */
public class LocalDateTimeToOffsetDateTimeUTCSerializer extends StdSerializer<LocalDateTime> {

	@SuppressWarnings("unused")
	public LocalDateTimeToOffsetDateTimeUTCSerializer() {
		super(LocalDateTime.class);
	}

	@SuppressWarnings("unused")
	protected LocalDateTimeToOffsetDateTimeUTCSerializer(JavaType type) {
		super(type);
	}

	@Override
	public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext provider) throws JacksonException {
		OffsetDateTime offsetDateTime = value.atZone(ZONEID_NORGE).withZoneSameInstant(ZONEID_UTC).toOffsetDateTime();
		gen.writeString(offsetDateTime.toString());
	}
}
