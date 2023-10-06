package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
@AllArgsConstructor
public class SmsVarsel {
	private String tekst;
	@NotBlank
	private String mobilnummer;
	private LocalDateTime varslingstidspunkt;

	public UtsendingsInfo.SmsVarsel toInternal() {
		if (varslingstidspunkt == null) {
			throw new DokarkivFunctionalException("smsvarsel.varslingstidspunkt er null");
		}
		return new UtsendingsInfo.SmsVarsel(tekst, mobilnummer, varslingstidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}
}
