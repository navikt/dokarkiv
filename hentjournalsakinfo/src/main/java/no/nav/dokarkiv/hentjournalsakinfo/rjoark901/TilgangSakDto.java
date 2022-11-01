package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Value
@Builder
@AllArgsConstructor
public class TilgangSakDto {

	String sakId;
	FagsystemCode fagsystem;
	Boolean feilregistrert;
	String aktoerId;
	String tema;
	String fagsakNr;
	String orgnr;
	String applikasjon;
	String opprettetAv;
	LocalDateTime opprettetTidspunkt;

	// "Opprettet tidspunkt iht. ISO-8601"
	public String getOpprettetTidspunkt() {
		if (this.opprettetTidspunkt == null) {
			return null;
		}
		return ZonedDateTime.of(opprettetTidspunkt, ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}
