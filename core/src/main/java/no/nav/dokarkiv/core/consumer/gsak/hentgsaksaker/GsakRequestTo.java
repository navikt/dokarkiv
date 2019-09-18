package no.nav.dokarkiv.core.consumer.gsak.hentgsaksaker;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GsakRequestTo {
	private final String tema;
	private final String applikasjon;
	private final String aktoerId;
	private final String orgnr;
	private final String fagsakNr;
}
