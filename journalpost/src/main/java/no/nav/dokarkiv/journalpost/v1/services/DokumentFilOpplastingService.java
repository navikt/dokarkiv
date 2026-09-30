package no.nav.dokarkiv.journalpost.v1.services;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokumentFilUliktInnholdException;
import no.nav.dokarkiv.core.exceptions.DokumentFilUnderOpplastingException;
import no.nav.dokarkiv.core.repository.DokumentFilOpplastingRepository;
import no.nav.dokarkiv.journalpost.v1.validators.Sha256ContentDigest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.util.Crc32c.base64;

@Slf4j
@Component
public class DokumentFilOpplastingService {

	private final DokumentFilOpplastingRepository dokumentFilOpplastingRepository;
	private final Clock clock;

	public DokumentFilOpplastingService(DokumentFilOpplastingRepository dokumentFilOpplastingRepository,
										Clock clock) {
		this.dokumentFilOpplastingRepository = dokumentFilOpplastingRepository;
		this.clock = clock;
	}

	/// Slår opp eksternDokumentReferanseId og håndterer idempotens:
	///
	///  - Finnes ikke fra før: oppretter og lagrer en ny [DokumentFilOpplasting] med tilstand LASTER_OPP.
	///  - Finnes fra før med annet sha256Sjekksum: kaster [DokumentFilUliktInnholdException] (422).
	///  - Finnes fra før med samme sha256Sjekksum og tilstand LASTER_OPP: kaster [DokumentFilUnderOpplastingException] (409).
	///  - Finnes fra før: returnerer den eksisterende
	///
	@Transactional
	public DokumentFilOpplasting behandleIdempotensOgOpprett(String eksternDokumentReferanseId, String mediaType, Sha256ContentDigest sha256ContentDigest) {
		if (!dokumentFilOpplastingRepository.existsByEksternDokumentReferanseId(eksternDokumentReferanseId)) {
			return opprettNyOpplasting(eksternDokumentReferanseId, mediaType, sha256ContentDigest);
		}
		DokumentFilOpplasting eksisterende = dokumentFilOpplastingRepository.findByEksternDokumentReferanseId(eksternDokumentReferanseId);
		if (!eksisterende.harSammeSha256Sjekksum(sha256ContentDigest.sha256Sjekksum())) {
			throw new DokumentFilUliktInnholdException(format(
					"Idempotency-Key=%s er allerede brukt for opplasting av et dokument med et annet innhold sha256=%s",
					eksternDokumentReferanseId,
					base64(eksisterende.getSha256Sjekksum())));
		}

		if (eksisterende.erUnderOpplasting()) {
			throw dokumentFilUnderOpplastingException(eksternDokumentReferanseId);
		}
		return eksisterende;
	}

	static DokumentFilUnderOpplastingException dokumentFilUnderOpplastingException(String eksternDokumentReferanseId) {
		return new DokumentFilUnderOpplastingException(format(
				"Idempotency-Key=%s brukes allerede av en opplasting som pågår", eksternDokumentReferanseId));
	}

	private DokumentFilOpplasting opprettNyOpplasting(String eksternDokumentReferanseId, String mediaType, Sha256ContentDigest sha256ContentDigest) {
		LocalDateTime naa = LocalDateTime.now(clock);
		DokumentFilOpplasting dokumentFilOpplasting = new DokumentFilOpplasting(
				UuidCreator.getTimeOrderedEpoch(),
				eksternDokumentReferanseId,
				mediaType,
				sha256ContentDigest.sha256Sjekksum(),
				MDC.get(MDC_CONSUMER_ID),
				naa.plusMinutes(2));
		return dokumentFilOpplastingRepository.persist(dokumentFilOpplasting);
	}

	@Transactional
	public DokumentFilOpplasting ferdigstillOpplasting(UUID dokumentFilId, long antallBytes) {
		DokumentFilOpplasting dokumentFilOpplasting = dokumentFilOpplastingRepository.findById(dokumentFilId)
				.orElseThrow();
		dokumentFilOpplasting.markerLastetOpp(MDC.get(MDC_CONSUMER_ID), antallBytes, LocalDateTime.now(clock));
		return dokumentFilOpplasting;
	}
}
