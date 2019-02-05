package no.nav.dokarkiv.skjermarkivenhet.rjoark100;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SkjermArkivenhetResponse {
	private Long journalpostId;
	private Long dokumentInfoId;
}
