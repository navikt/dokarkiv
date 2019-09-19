package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class SaksrelasjonDto {

	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private final String sakId;
	private final Boolean feilregistrert;
	private final FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;
	private final String orgnr;
	private final String opprettetAv;
	private final String opprettetTidspunkt;

	// "Opprettet tidspunkt iht. ISO-8601"
	public String getOpprettetTidspunkt() {
		if (this.opprettetTidspunkt == null) {
			return null;
		}
		return ZonedDateTime.of(LocalDateTime.parse(opprettetTidspunkt, formatter),
				ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}