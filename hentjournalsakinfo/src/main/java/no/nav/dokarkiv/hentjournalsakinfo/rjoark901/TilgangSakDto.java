package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
@AllArgsConstructor
public class TilgangSakDto {

	static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

	private final String sakId;
	private final FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String orgnr;
	private final String applikasjon;
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
