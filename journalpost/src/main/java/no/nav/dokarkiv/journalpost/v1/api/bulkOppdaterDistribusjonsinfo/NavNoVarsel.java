package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@AllArgsConstructor
public class NavNoVarsel {
	@NotBlank
	@Size(max = 200)
	@Schema(description = "Mottakerens digitale kontaktinformasjon som varsel er sendt til", required = true, maxLength = 200)
	private String digitalkontaktinformasjon;
	@NotBlank
	@Size(max = 4000)
	@Schema(description = "Varslingstekst som er sendt til mottaker", required = true, maxLength = 200)
	private String varseltekst;
}
