package no.nav.dokarkiv.journalpost.v1.services;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokumentFilAnnenTilstandException;
import no.nav.dokarkiv.core.exceptions.DokumentFilUliktInnholdException;
import no.nav.dokarkiv.core.exceptions.InitierDokumentFilAnnenTjenesteException;
import no.nav.dokarkiv.core.repository.DokumentFilOpplastingRepository;
import no.nav.dokarkiv.core.storage.BucketStorageOperations;
import no.nav.dokarkiv.core.storage.SignertOpplasting;
import no.nav.dokarkiv.journalpost.v1.validators.InitierDokumentFilOpplastingValidator;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.time.temporal.ChronoUnit.MILLIS;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.journalpost.v1.services.InitierDokumentFilOpplastingResult.Utfall.SIGNERT_URL_PRODUSERT;
import static no.nav.dokarkiv.journalpost.v1.services.InitierDokumentFilOpplastingResult.Utfall.TEKNISK_RETRY;
import static no.nav.dokarkiv.journalpost.v1.services.InitierDokumentFilOpplastingResult.fra;

@Slf4j
@Component
public class InitierDokumentFilOpplastingService {

	private static final long OPPLASTING_LEVETID_SEKUNDER = TimeUnit.HOURS.toSeconds(1);

	private final DokumentFilOpplastingRepository dokumentFilOpplastingRepository;
	private final BucketStorageOperations dokarkivMellomlagerBucketStorage;
	private final Clock clock;

	public InitierDokumentFilOpplastingService(DokumentFilOpplastingRepository dokumentFilOpplastingRepository,
											   BucketStorageOperations dokarkivMellomlagerBucketStorage,
											   Clock clock) {
		this.dokumentFilOpplastingRepository = dokumentFilOpplastingRepository;
		this.dokarkivMellomlagerBucketStorage = dokarkivMellomlagerBucketStorage;
		this.clock = clock;
	}

	@Transactional
	public InitierDokumentFilOpplastingResult initierDokumentFilOpplasting(InitierDokumentFilOpplastingValidator.ValidertInitierDokumentFilOpplastingRequest request) {
		if (dokumentFilOpplastingRepository.existsByEksternDokumentReferanseId(request.idempotencyKey())) {
			DokumentFilOpplasting eksisterende = dokumentFilOpplastingRepository.findByEksternDokumentReferanseId(request.idempotencyKey());
			validerEksisterendeDokumentFilOpplasting(request, eksisterende);
			return fra(TEKNISK_RETRY, eksisterende);
		}

		UUID dokumentFilId = UuidCreator.getTimeOrderedEpoch();
		LocalDateTime utloeper = LocalDateTime.now(clock)
				.plusSeconds(OPPLASTING_LEVETID_SEKUNDER)
				.truncatedTo(MILLIS);
		String opplastingUrl = opprettSignertOpplasting(request, dokumentFilId).url().toString();
		DokumentFilOpplasting dokumentFilOpplasting = dokumentFilOpplastingRepository.persistAndFlush(opprettNyOpplasting(request, dokumentFilId, utloeper, opplastingUrl));
		return fra(SIGNERT_URL_PRODUSERT, dokumentFilOpplasting);
	}

	private static @NonNull DokumentFilOpplasting opprettNyOpplasting(InitierDokumentFilOpplastingValidator.ValidertInitierDokumentFilOpplastingRequest request,
																	  UUID dokumentFilId,
																	  LocalDateTime utloeper,
																	  String opplastingUrl) {
		return new DokumentFilOpplasting(dokumentFilId,
				request.idempotencyKey(),
				request.mediaType(),
				request.crc32c().crc32c(),
				MDC.get(MDC_CONSUMER_ID),
				utloeper,
				opplastingUrl,
				request.stoerrelse());
	}

	private SignertOpplasting opprettSignertOpplasting(InitierDokumentFilOpplastingValidator.ValidertInitierDokumentFilOpplastingRequest request,
													   UUID dokumentFilId) {
		return dokarkivMellomlagerBucketStorage.signertOpplasting(
				dokumentFilId.toString(),
				request.mediaType(),
				request.crc32c().base64(),
				request.stoerrelse(),
				OPPLASTING_LEVETID_SEKUNDER);
	}

	private static void validerEksisterendeDokumentFilOpplasting(
			InitierDokumentFilOpplastingValidator.ValidertInitierDokumentFilOpplastingRequest request,
			DokumentFilOpplasting eksisterende) {
		if (!eksisterende.harSammeCrc32cSjekksum(request.crc32c().crc32c())) {
			throw new DokumentFilUliktInnholdException(format(
					"Idempotency-Key=%s er allerede brukt for opplasting av et dokument med et annet innhold crc32c=%s",
					request.idempotencyKey(),
					Base64.getEncoder().encodeToString(eksisterende.getCrc32cSjekksum())));
		}
		if (!eksisterende.erUnderOpplasting()) {
			throw new DokumentFilAnnenTilstandException(format(
					"Idempotency-Key=%s er i en annen tilstand og kan ikke initialiseres lenger. tilstand=%s",
					request.idempotencyKey(), eksisterende.getTilstand()));
		}
		if (eksisterende.getOpplastingUrl() == null) {
			throw new InitierDokumentFilAnnenTjenesteException(format("Idempotency-Key=%s er opprettet av en annen tjeneste og kan ikke initialiseres",
					request.idempotencyKey()));
		}
	}

}
