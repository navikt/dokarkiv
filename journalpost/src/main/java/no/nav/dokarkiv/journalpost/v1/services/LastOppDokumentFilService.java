package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.storage.BucketStorageOperations;
import no.nav.dokarkiv.core.storage.OpplastetDokumentFil;
import no.nav.dokarkiv.core.storage.DokarkivMellomlagerBucketStorage;
import no.nav.dokarkiv.journalpost.v1.validators.ContentDigest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.CRC32C;
import java.util.zip.CheckedInputStream;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.util.Crc32c.base64;
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

	public LastOppDokumentFilResult lastOppDokumentFil(String idempotencyKey, ContentDigest contentDigest, String contentType, InputStream inputStream) {
		DokumentFilOpplasting dokumentFilOpplasting = hentDokumentFilOpplasting(idempotencyKey, contentType, contentDigest);
		if (dokumentFilOpplasting.erOpplastet()) {
			return LastOppDokumentFilResult.fra(TEKNISK_RETRY, dokumentFilOpplasting);
		}
		UUID dokumentFilId = dokumentFilOpplasting.getDokumentFilId();
		OpplastetDokumentFil opplastetDokumentFil = lastOppOgValider(dokumentFilId, idempotencyKey, inputStream, contentType, contentDigest);
		DokumentFilOpplasting dokumentFilOpplastingFerdig = dokumentFilOpplastingService.ferdigstillOpplasting(dokumentFilId, opplastetDokumentFil.antallBytes());
		return LastOppDokumentFilResult.fra(LASTET_OPP, dokumentFilOpplastingFerdig);
	}

	private DokumentFilOpplasting hentDokumentFilOpplasting(String idempotencyKey, String mediaType, ContentDigest contentDigest) {
		try {
			return dokumentFilOpplastingService.behandleIdempotensOgOpprett(idempotencyKey, mediaType, contentDigest);
		} catch (DataIntegrityViolationException e) {
			throw dokumentFilUnderOpplastingException(idempotencyKey);
		}
	}

	/// Streamer payload til dokarkivmellomlager, og validerer at både den beregnede sha256-sjekksummen og
	/// crc32c-sjekksummen til det opplastede objektet stemmer med det som var forventet.
	///
	/// @throws InputValideringFeiletException hvis sha256- eller crc32c-sjekksum ikke stemmer.
	public OpplastetDokumentFil lastOppOgValider(UUID dokumentFilId, String eksternDokumentReferanseId, InputStream payload,
												 String contentType, ContentDigest contentDigest) {
		MessageDigest sha256MessageDigest = getSha256Instance();
		CRC32C crc32c = new CRC32C();

		try (DigestInputStream digestInputStream = new DigestInputStream(new CheckedInputStream(payload, crc32c), sha256MessageDigest)) {
			OpplastetDokumentFil opplastetDokumentFil = dokarkivMellomlagerBucketStorage.uploadObject(dokumentFilId.toString(), digestInputStream, contentType);

			byte[] beregnetSha256Sjekksum = sha256MessageDigest.digest();
			byte[] contentDigestSha256Sjekksum = contentDigest.sha256Sjekksum();
			if (!Arrays.equals(beregnetSha256Sjekksum, contentDigestSha256Sjekksum)) {
				throw new InputValideringFeiletException(format(
						"Beregnet sha256 sjekksum av mottatt payload stemmer ikke med Content-Digest for Idempotency-Key=%s. Beregnet=%s, Content-Digest=%s",
						eksternDokumentReferanseId, Base64.getEncoder().encodeToString(beregnetSha256Sjekksum), Base64.getEncoder().encodeToString(contentDigestSha256Sjekksum)));
			}

			String beregnetCrc32cSjekksum = base64(crc32c.getValue());
			if (!beregnetCrc32cSjekksum.equals(opplastetDokumentFil.crc32cBase64())) {
				throw new InputValideringFeiletException(format(
						"crc32c-sjekksummen til objektet i dokarkivmellomlager stemmer ikke med det som ble beregnet lokalt for Idempotency-Key=%s", eksternDokumentReferanseId));
			}

			return opplastetDokumentFil;
		} catch (IOException e) {
			throw new DokarkivTechnicalException(format("Feilet ved lesing av payload for eksternDokumentReferanseId=%s", eksternDokumentReferanseId), e);
		}
	}
}
