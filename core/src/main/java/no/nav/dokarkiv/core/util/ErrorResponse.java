package no.nav.dokarkiv.core.util;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Data
@Builder
public class ErrorResponse {
	private final String uuid;
	private final String feilmelding;

	public ErrorResponse(String uuid, String feilmelding) {
		this.uuid = uuid;
		this.feilmelding = feilmelding;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE)
				.append("uuid", uuid)
				.append("feilmelding", feilmelding)
				.toString();
	}
}
