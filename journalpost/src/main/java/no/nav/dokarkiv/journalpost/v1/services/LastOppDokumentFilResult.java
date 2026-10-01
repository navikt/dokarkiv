package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilTilstand;

import java.time.LocalDateTime;
import java.util.UUID;

public record LastOppDokumentFilResult(
		Utfall utfall,
		UUID dokumentFilId,
		String eksternDokumentReferanseId,
		String mediaType,
		byte[] sha256Sjekksum,
		DokumentFilTilstand tilstand,
		LocalDateTime datoOpprettet,
		LocalDateTime datoSistEndret
) {
	public static LastOppDokumentFilResult fra(Utfall utfall, DokumentFilOpplasting dokumentFilOpplasting) {
		return new LastOppDokumentFilResult(utfall,
				dokumentFilOpplasting.getDokumentFilId(),
				dokumentFilOpplasting.getEksternDokumentReferanseId(),
				dokumentFilOpplasting.getMediaType(),
				dokumentFilOpplasting.getSha256Sjekksum(),
				fra(dokumentFilOpplasting.getTilstand()),
				dokumentFilOpplasting.getDatoOpprettet(),
				dokumentFilOpplasting.getDatoEndret());
	}

	public static DokumentFilTilstand fra(no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand dokumentFilOpplastingTilstand) {
		return switch (dokumentFilOpplastingTilstand) {
			case LASTER_OPP -> DokumentFilTilstand.LASTER_OPP;
			case LASTET_OPP -> DokumentFilTilstand.LASTET_OPP;
			case ARKIVERES -> DokumentFilTilstand.ARKIVERES;
			case ARKIVERT -> DokumentFilTilstand.ARKIVERT;
			case FEILET -> DokumentFilTilstand.FEILET;
		};
	}

	public enum Utfall {
		TEKNISK_RETRY,
		LASTET_OPP
	}
}
