package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilTilstand;

import java.time.LocalDateTime;
import java.util.UUID;

public record LastOppDokumentFilResult(
		Utfall utfall,
		UUID dokumentFilId,
		String eksternDokumentReferanseId,
		String mediaType,
		byte[] crc32c,
		byte[] sha256,
		long stoerrelse,
		DokumentFilTilstand tilstand,
		LocalDateTime datoOpprettet,
		LocalDateTime datoSistEndret
) {
	public static LastOppDokumentFilResult fra(Utfall utfall, DokumentFilOpplasting dokumentFilOpplasting) {
		return new LastOppDokumentFilResult(utfall,
				dokumentFilOpplasting.getDokumentFilId(),
				dokumentFilOpplasting.getEksternDokumentReferanseId(),
				dokumentFilOpplasting.getMediaType(),
				dokumentFilOpplasting.getCrc32cSjekksum(),
				dokumentFilOpplasting.getSha256Sjekksum(),
				dokumentFilOpplasting.getAntallBytes(),
				fra(dokumentFilOpplasting.getTilstand()),
				dokumentFilOpplasting.getDatoOpprettet(),
				dokumentFilOpplasting.getDatoEndret());
	}

	public static DokumentFilTilstand fra(DokumentFilOpplastingTilstand dokumentFilOpplastingTilstand) {
		return switch (dokumentFilOpplastingTilstand) {
			case UNDER_OPPLASTING -> DokumentFilTilstand.UNDER_OPPLASTING;
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
