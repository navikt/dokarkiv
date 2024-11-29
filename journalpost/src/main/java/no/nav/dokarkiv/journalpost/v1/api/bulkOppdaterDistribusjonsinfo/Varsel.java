package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class Varsel {
	@Size(max = 200)
	@Schema(description = "Deprecated, bruk Epost- og SMSvarsel. Mottakerens digitale kontaktinformasjon som varsel er sendt til", maxLength = 200)
	@Deprecated
	private String digitalkontaktinformasjon;
	@Size(max = 4000)
	@Schema(description = "Deprecated, bruk Epost- og SMSvarsel. Varslingstekst som er sendt til mottaker", maxLength = 200)
	@Deprecated
	private String varseltekst;

	private List<EpostVarsel> epostvarsel;

	private List<SmsVarsel> smsvarsel;
}
