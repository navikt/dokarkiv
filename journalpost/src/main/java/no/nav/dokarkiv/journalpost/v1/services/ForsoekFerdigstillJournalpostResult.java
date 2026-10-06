package no.nav.dokarkiv.journalpost.v1.services;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public record ForsoekFerdigstillJournalpostResult(@NonNull Status status, @Nullable String melding) {

	@Getter
	public enum Status {
		MIDLERTIDIG("MIDLERTIDIG", false),
		ENDELIG("ENDELIG", true);

		private final String apiNavn;
		private final boolean ferdigstilt;

		Status(String apiNavn, boolean ferdigstilt) {
			this.apiNavn = apiNavn;
			this.ferdigstilt = ferdigstilt;
		}
	}
}
