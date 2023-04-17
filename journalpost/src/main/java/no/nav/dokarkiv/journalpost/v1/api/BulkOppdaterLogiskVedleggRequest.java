package no.nav.dokarkiv.journalpost.v1.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class BulkOppdaterLogiskVedleggRequest {
	@Schema(
			description = """
					Titlene som skal settes som logiske vedlegg for DokumentInfo.
					""",
			required = true,
			example = "[\"Kvittering fra legekontor på konsultasjon\", \"Uttalelse fra lege\"]"
	)
	private final List<String> titler;

	@JsonCreator
	public BulkOppdaterLogiskVedleggRequest(@JsonProperty("titler") List<String> titler) {
		this.titler = titler;
	}
}
