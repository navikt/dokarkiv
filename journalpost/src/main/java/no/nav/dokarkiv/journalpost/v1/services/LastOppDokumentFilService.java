package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.storage.BucketStorageOperations;
import no.nav.dokarkiv.core.storage.OpplastetDokumentFil;
import no.nav.dokarkiv.journalpost.v1.validators.Crc32cContentDigest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.UUID;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.util.Digest.getSha256Instance;
import static no.nav.dokarkiv.journalpost.v1.services.DokumentFilOpplastingService.dokumentFilUnderOpplastingException;
import static no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult.Utfall.LASTET_OPP;
import static no.nav.dokarkiv.journalpost.v1.services.LastOppDokumentFilResult.Utfall.TEKNISK_RETRY;

@Slf4j
@Service
public class LastOppDokumentFilService {

	private final DokumentFilOpplastingService dokumentFilOpplastingService;
	private final BucketStorageOperations dokarkivMellomlagerBucketStorage;

	public LastOppDokumentFilService(DokumentFilOpplastingService dokumentFilOpplastingService,
									 BucketStorageOperations dokarkivMellomlagerBucketStorage) {
		this.dokumentFilOpplastingService = dokumentFilOpplastingService;
		this.dokarkivMellomlagerBucketStorage = dokarkivMellomlagerBucketStorage;
	}

	public LastOppDokumentFilResult lastOppDokumentFil(String idempotencyKey, Crc32cContentDigest crc32cContentDigest, String contentType, InputStream inputStream) {
		DokumentFilOpplasting dokumentFilOpplasting = hentDokumentFilOpplasting(idempotencyKey, contentType, crc32cContentDigest);
		if (!dokumentFilOpplasting.erUnderOpplasting()) {
			return LastOppDokumentFilResult.fra(TEKNISK_RETRY, dokumentFilOpplasting);
		}
		UUID dokumentFilId = dokumentFilOpplasting.getDokumentFilId();
		ValidertOpplasting validertOpplasting = lastOppOgValider(dokumentFilId, idempotencyKey, inputStream, contentType, crc32cContentDigest);
		DokumentFilOpplasting dokumentFilOpplastingFerdig = dokumentFilOpplastingService.ferdigstillOpplasting(dokumentFilId,
				validertOpplasting.antallBytes(), validertOpplasting.sha256Sjekksum());
		return LastOppDokumentFilResult.fra(LASTET_OPP, dokumentFilOpplastingFerdig);
	}

	private DokumentFilOpplasting hentDokumentFilOpplasting(String idempotencyKey, String mediaType, Crc32cContentDigest crc32cContentDigest) {
		try {
			return dokumentFilOpplastingService.behandleIdempotensOgOpprett(idempotencyKey, mediaType, crc32cContentDigest);
		} catch (DataIntegrityViolationException e) {
			throw dokumentFilUnderOpplastingException(idempotencyKey);
		}
	}

	private ValidertOpplasting lastOppOgValider(UUID dokumentFilId, String eksternDokumentReferanseId, InputStream payload,
												String contentType, Crc32cContentDigest crc32cContentDigest) {
		MessageDigest sha256MessageDigest = getSha256Instance();
		String forventetCrc32c = crc32cContentDigest.base64();

		try (DigestInputStream digestInputStream = new DigestInputStream(payload, sha256MessageDigest)) {
			OpplastetDokumentFil opplastetDokumentFil = dokarkivMellomlagerBucketStorage.uploadObject(dokumentFilId.toString(), digestInputStream, contentType, forventetCrc32c);

			if (!forventetCrc32c.equals(opplastetDokumentFil.crc32cBase64())) {
				throw new InputValideringFeiletException(format(
						"crc32c-sjekksummen til objektet i dokarkivmellomlager stemmer ikke med Content-Digest for Idempotency-Key=%s. Dokarkivmellomlager=%s, Content-Digest=%s",
						eksternDokumentReferanseId, opplastetDokumentFil.crc32cBase64(), forventetCrc32c));
			}

			return new ValidertOpplasting(opplastetDokumentFil.antallBytes(), sha256MessageDigest.digest());
		} catch (IOException e) {
			throw new DokarkivTechnicalException(format("Feilet ved lesing av payload for eksternDokumentReferanseId=%s", eksternDokumentReferanseId), e);
		}
	}

	private record ValidertOpplasting(long antallBytes, byte[] sha256Sjekksum) {
	}
}
