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
public class DigitalPost {
	@Schema(description = "Mottakerens digitale postkasseadresse som forsendelsen er distribuert til",
			required = true, maxLength = 100)
	@NotBlank
	@Size(max = 100)
	private String digitalpostkasseadresse;
	@Schema(description = "Mottakerens digitale postkasseleverandør som forsendelsen er distribuert til",
			required = true, maxLength = 20)
	@NotBlank
	@Size(max = 20)
	private String digitalpostkasseleverandor;
}
