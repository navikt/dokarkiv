package no.nav.dokarkiv.journalpost.v1.api.dokumentfil;

import io.swagger.v3.oas.annotations.media.Schema;

/// DokumentFil tilstander
public enum DokumentFilTilstand {
	@Schema(description = "DokumentFil er under opplasting")
	UNDER_OPPLASTING,
	@Schema(description = "DokumentFil er lastet opp OK til mellomlager")
	LASTET_OPP,
	@Schema(description = "DokumentFil har blitt tilknyttet journalpost og er klar til arkivering")
	ARKIVERES,
	@Schema(description = "DokumentFil har blitt arkivert")
	ARKIVERT,
	@Schema(description = "DokumentFil har feilet")
	FEILET
}
