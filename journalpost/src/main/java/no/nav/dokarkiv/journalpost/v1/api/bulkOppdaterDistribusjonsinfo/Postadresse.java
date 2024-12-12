package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Postadresse {
	@Size(max = 200)
	@Schema(maxLength = 200)
	private String adresselinje1;
	@Size(max = 200)
	@Schema(maxLength = 200)
	private String adresselinje2;
	@Size(max = 200)
	@Schema(maxLength = 200)
	private String adresselinje3;
	@Size(max = 10)
	@Schema(maxLength = 10)
	private String postnummer;
	@Size(max = 200)
	@Schema(maxLength = 200)
	private String poststed;
	@Size(max = 2)
	@Schema(maxLength = 2)
	private String landkode;
}
