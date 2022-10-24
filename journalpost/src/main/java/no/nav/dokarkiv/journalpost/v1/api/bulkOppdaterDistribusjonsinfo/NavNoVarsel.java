package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class NavNoVarsel {
	@Schema(description = "Mottakerens digitale kontaktinformasjon som varsel er sendt til", required = true)
	private String digitalkontaktinformasjon;
	@Schema(description = "Varslingstekst som er sendt til mottaker", required = true)
	private String varseltekst;
}
