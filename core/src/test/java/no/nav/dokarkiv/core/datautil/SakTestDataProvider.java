package no.nav.dokarkiv.core.datautil;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.SakStatusCode;
import no.nav.dokarkiv.core.domain.entities.Sak;

import java.time.LocalDateTime;

public final class SakTestDataProvider {

	private SakTestDataProvider() {

	}

	public static Sak.SakBuilder createSakWithStatus(SakStatusCode sakStatusCode) {
		return Sak.builder()
				.sakStatus(sakStatusCode)
				.tema(FagsystemCode.PEN.name())
				.opprettetAv("SakTestDataProvider")
				.opprettetTidspunkt(LocalDateTime.now());
	}
}
