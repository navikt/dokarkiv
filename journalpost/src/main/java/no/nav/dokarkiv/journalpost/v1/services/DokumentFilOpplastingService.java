package no.nav.dokarkiv.journalpost.v1.services;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokumentFilAlleredeBruktException;
import no.nav.dokarkiv.core.exceptions.DokumentFilUliktInnholdException;
import no.nav.dokarkiv.core.exceptions.DokumentFilUnderOpplastingException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.repository.DokumentFilOpplastingRepository;
import no.nav.dokarkiv.core.storage.OpplastetDokumentFil;
import no.nav.dokarkiv.journalpost.v1.validators.Sha256ContentDigest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;
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

	@Transactional(readOnly = true)
	public Map<UUID, DokumentFilOpplasting> hentTilstander(Collection<UUID> dokumentFilIder) {
		return dokumentFilOpplastingRepository.findAllByDokumentFilIdIn(dokumentFilIder).stream()
				.collect(Collectors.toMap(DokumentFilOpplasting::getDokumentFilId, Function.identity()));
	}

	/// Markerer dokumentFilene med tilstand ARKIVERES. Skal kalles i samme transaksjon som oppretter journalposten.
	///
	/// Sjekker tilstand på nytt i tilfelle den er endret siden validering. Samtidige oppdateringer stoppes av `@Version`.
	///
	/// @throws DokumentFilAlleredeBruktException hvis en eller flere dokumentFiler allerede er brukt av en journalpost.
	/// @throws InputValideringFeiletException    hvis en eller flere dokumentFiler ikke finnes eller ikke har tilstand LASTET_OPP.
	@Transactional
	public Map<UUID, DokumentFilOpplasting> markerArkiveres(Collection<UUID> dokumentFilIder) {
		Map<UUID, DokumentFilOpplasting> opplastinger = dokumentFilOpplastingRepository.findAllByDokumentFilIdIn(dokumentFilIder)
				.stream().collect(Collectors.toMap(DokumentFilOpplasting::getDokumentFilId, Function.identity()));

		validerDokumentFilIdAlleredeBrukt(dokumentFilIder, opplastinger);

		String endretKildeNavn = MDC.get(MDC_CONSUMER_ID);
		opplastinger.values().forEach(dokumentFilOpplasting -> dokumentFilOpplasting.markerArkiveres(endretKildeNavn));
		return opplastinger;
	}

	private static void validerDokumentFilIdAlleredeBrukt(Collection<UUID> dokumentFilIder, Map<UUID, DokumentFilOpplasting> opplastinger) {
		String ugyldige = dokumentFilIder.stream()
				.sorted()
				.filter(dokumentFilId -> eksistererIkkeEllerIkkeLastetOpp(dokumentFilId, opplastinger))
				.map(dokumentFilId -> {
					if (opplastinger.containsKey(dokumentFilId)) {
						return "%s (har tilstand=%s, forventet %s)".formatted(dokumentFilId, opplastinger.get(dokumentFilId).getTilstand(), LASTET_OPP);
					} else {
						return "%s (finnes ikke)".formatted(dokumentFilId);
					}
				})
				.collect(Collectors.joining(", "));
		if (!ugyldige.isEmpty()) {
			String feilmelding = format("Ugyldige dokumentFilId: %s", ugyldige);
			boolean alleredeBrukt = opplastinger.values().stream()
					.anyMatch(DokumentFilOpplasting::erBruktAvJournalpost);
			throw alleredeBrukt ? new DokumentFilAlleredeBruktException(feilmelding) : new InputValideringFeiletException(feilmelding);
		}
	}

	private static boolean eksistererIkkeEllerIkkeLastetOpp(UUID dokumentFilId, Map<UUID, DokumentFilOpplasting> opplastinger) {
		return !opplastinger.containsKey(dokumentFilId) || !opplastinger.get(dokumentFilId).erLastetOpp();
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
	public DokumentFilOpplasting ferdigstillOpplasting(UUID dokumentFilId, OpplastetDokumentFil opplastetDokumentFil) {
		DokumentFilOpplasting dokumentFilOpplasting = dokumentFilOpplastingRepository.findById(dokumentFilId)
				.orElseThrow();
		dokumentFilOpplasting.markerLastetOpp(opplastetDokumentFil.antallBytes(),
				Base64.getDecoder().decode(opplastetDokumentFil.crc32cBase64()),
				LocalDateTime.now(clock),
				MDC.get(MDC_CONSUMER_ID));
		return dokumentFilOpplasting;
	}
}
