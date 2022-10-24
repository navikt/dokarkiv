package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class Postadresse {
	@Schema(maxLength = 200)
	private String adresselinje1;
	@Schema(maxLength = 200)
	private String adresselinje2;
	@Schema(maxLength = 200)
	private String adresselinje3;
	@Schema(maxLength = 10)
	private String postnummer;
	@Schema(maxLength = 200)
	private String poststed;
	@Schema(maxLength = 2)
	private String landkode;
}
