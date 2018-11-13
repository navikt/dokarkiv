package no.nav.dokarkiv.core.consumer.gsak.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@JsonDeserialize(builder = SakInfoTo.GsakSakerToBuilder.class)
@Value
@Builder
public class SakInfoTo {
	private final Integer id;
	private final String tema;
	private final String applikasjon;
	private final String aktoerId;
	private final String orgnr;
	private final String fagsakNr;
	private final String opprettetAv;
	private final OffsetDateTime opprettetTidspunkt;

	@JsonPOJOBuilder(withPrefix = "")
	public static class GsakSakerToBuilder {

	}
}