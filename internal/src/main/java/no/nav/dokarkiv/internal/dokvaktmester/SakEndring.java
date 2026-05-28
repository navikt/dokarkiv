package no.nav.dokarkiv.internal.dokvaktmester;

import no.nav.dokarkiv.core.domain.entities.Sak;

import static io.micrometer.common.util.StringUtils.isBlank;

public record SakEndring(String tema, String brukerId, String aktoerId, String fagsakId, String fagsaksystem, String begrunnelseNokkel) {

	private static final String GENERELL_SAK = "FS22";

	static SakEndring opprett(EndreFerdigstiltJournalpostRequest request, String aktoerId, Sak tilknyttetSak) {
		EndreSak endreSak = request.sak();
		String tema = isBlank(request.tema()) ? tilknyttetSak.getTema() : request.tema();
		if (endreSak == null) {
			return new SakEndring(
					tema,
					request.brukerId(), aktoerId,
					tilknyttetSak.getFagsakNr(), tilknyttetSak.getApplikasjon(),
					request.begrunnelseNokkel()
			);
		} else {
			return switch (endreSak.sakstype()) {
				case FAGSAK -> new SakEndring(
						tema,
						request.brukerId(), aktoerId,
						endreSak.fagsakId(), endreSak.fagsaksystem().name(),
						request.begrunnelseNokkel()
				);
				case GENERELL_SAK -> new SakEndring(
						tema,
						request.brukerId(), aktoerId,
						null, GENERELL_SAK,
						request.begrunnelseNokkel()
				);
				case ARKIVSAK -> throw new UnsupportedOperationException("sak.sakstype ARKIVSAK støttes ikke");
			};
		}
	}
}
