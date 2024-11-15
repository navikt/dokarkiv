package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;

import java.time.ZonedDateTime;

@Data
@Builder
public class SaksrelasjonDto {

	private final String sakId;
	private final boolean feilregistrert;
	private final FagsystemCode fagsystem;
	private final String aktoerId;
	private final String tema;
	private final String fagsakNr;
	private final String applikasjon;
	private final String orgnr;
	private final String opprettetAv;

	@Getter(AccessLevel.NONE)
	@JsonProperty("opprettetTidspunkt")
	private final ZonedDateTime opprettetTid;
}