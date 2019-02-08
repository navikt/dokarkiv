package no.nav.dokarkiv.kasserdokument.rjoark103;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KasserDokumentResponse {
	private final Long dokumentInfoId;

	@JsonCreator
	public KasserDokumentResponse(
			@JsonProperty("dokumentInfoId") Long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
	}
}
