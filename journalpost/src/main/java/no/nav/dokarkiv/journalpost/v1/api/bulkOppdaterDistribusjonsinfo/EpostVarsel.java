package no.nav.dokarkiv.journalpost.v1.api.bulkOppdaterDistribusjonsinfo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.entities.UtsendingsInfo;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Builder
@Getter
@AllArgsConstructor
public class EpostVarsel {
	private String tittel;
	private String tekst;
	@NotBlank
	private String epostadresse;
	private LocalDateTime varslingstidspunkt;

	public UtsendingsInfo.EpostVarsel toInternal() {
		if (varslingstidspunkt == null) {
			throw new DokarkivFunctionalException("epostvarsel.varslingstidspunkt er null");
		}
		return new UtsendingsInfo.EpostVarsel(tittel, tekst, epostadresse, varslingstidspunkt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	}
}
