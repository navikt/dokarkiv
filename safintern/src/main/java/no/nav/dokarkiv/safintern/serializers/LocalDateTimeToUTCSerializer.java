package no.nav.dokarkiv.safintern.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static no.nav.dokarkiv.core.CoreConfig.ZONEID_NORGE;
import static no.nav.dokarkiv.core.CoreConfig.ZONEID_UTC;

public class LocalDateTimeToUTCSerializer extends StdSerializer<LocalDateTime> {

	@SuppressWarnings("unused")
	public LocalDateTimeToUTCSerializer() {
		super(LocalDateTime.class);
	}

	@SuppressWarnings("unused")
	protected LocalDateTimeToUTCSerializer(JavaType type) {
		super(type);
	}

	@Override
	public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		OffsetDateTime offsetDateTime = value.atZone(ZONEID_NORGE).withZoneSameInstant(ZONEID_UTC).toOffsetDateTime();
		gen.writeObject(offsetDateTime.toString());
	}
}
