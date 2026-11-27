package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.journalpost.v1.api.dokumentfil.DokumentFilTilstand;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

public record InitierDokumentFilOpplastingResult(Utfall utfall,
												 UUID dokumentFilId,
												 String eksternDokumentReferanseId,
												 String mediaType,
												 byte[] crc32c,
												 long stoerrelse,
												 DokumentFilTilstand tilstand,
												 LocalDateTime datoOpprettet,
												 LocalDateTime datoSistEndret,
												 URI opplastingUrl,
												 LocalDateTime opplastingUtloeper) {

	public static InitierDokumentFilOpplastingResult fra(Utfall utfall, DokumentFilOpplasting opplasting) {
		return new InitierDokumentFilOpplastingResult(
				utfall,
				opplasting.getDokumentFilId(),
				opplasting.getEksternDokumentReferanseId(),
				opplasting.getMediaType(),
				opplasting.getCrc32cSjekksum(),
				opplasting.getAntallBytes(),
				fra(opplasting.getTilstand()),
				opplasting.getDatoOpprettet(),
				opplasting.getDatoEndret(),
				URI.create(opplasting.getOpplastingUrl()),
				opplasting.getDatoKlientLease());
	}

	public enum Utfall {
		TEKNISK_RETRY,
		SIGNERT_URL_PRODUSERT
	}

	static DokumentFilTilstand fra(DokumentFilOpplastingTilstand dokumentFilOpplastingTilstand) {
		return switch (dokumentFilOpplastingTilstand) {
			case UNDER_OPPLASTING -> DokumentFilTilstand.UNDER_OPPLASTING;
			case LASTET_OPP -> DokumentFilTilstand.LASTET_OPP;
			case ARKIVERES -> DokumentFilTilstand.ARKIVERES;
			case ARKIVERT -> DokumentFilTilstand.ARKIVERT;
			case FEILET -> DokumentFilTilstand.FEILET;
		};
	}
}
