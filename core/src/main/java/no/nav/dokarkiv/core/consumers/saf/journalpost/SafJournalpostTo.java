package no.nav.dokarkiv.core.consumers.saf.journalpost;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SafJournalpostTo {

	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();

	@Value
	@Builder
	public static class DokumentInfo {
		private final String dokumentInfoId;

		@Builder.Default
		private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
	}

	@Value
	@Builder
	public static class Dokumentvariant {
		private final String variantformat;
		private final boolean saksbehandlerHarTilgang;
	}
}
