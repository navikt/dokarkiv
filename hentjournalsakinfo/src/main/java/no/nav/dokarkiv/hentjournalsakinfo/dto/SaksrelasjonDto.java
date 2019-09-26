package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
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

	private final String sakId;
	private final Boolean feilregistrert;
	private final FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;
	private final String orgnr;
	private final String opprettetAv;

	@Getter(AccessLevel.NONE)
	@JsonProperty("opprettetTidspunkt")
	private final LocalDateTime opprettetTid;

	// "Opprettet tidspunkt iht. ISO-8601"
	public String getOpprettetTidspunkt() {
		if (this.opprettetTid == null) {
			return null;
		}
		return ZonedDateTime.of(this.opprettetTid, ZoneId.systemDefault())
				.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	}
}