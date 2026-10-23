package no.nav.dokarkiv.journalpost.v1.services;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;
import no.nav.dokarkiv.core.exceptions.DokumentFilAlleredeBruktException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.storage.BucketStorageOperations;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.DokumentFilOpplastingTilstand.LASTET_OPP;

/// Validerer at alle `dokumentFilId` i en [OpprettJournalpostRequest] er klare for arkivering:
///
///  - `dokumentFilId` har tilstand LASTET_OPP i [no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting].
///  - `dokumentFilId` finnes i dokarkivmellomlager.
///
/// Når valideringen er OK settes hold på objektene i dokarkivmellomlager slik at de ikke kan slettes eller endres før de er arkivert.
/// Sjekk og hold i dokarkivmellomlager er web-kall. Denne klassen skal derfor ikke være `@Transactional`.
@Slf4j
@Service
public class OpprettJournalpostDokumentFilService {

	private final DokumentFilOpplastingService dokumentFilOpplastingService;
	private final BucketStorageOperations dokarkivMellomlagerBucketStorage;

	public OpprettJournalpostDokumentFilService(DokumentFilOpplastingService dokumentFilOpplastingService,
												BucketStorageOperations dokarkivMellomlagerBucketStorage) {
		this.dokumentFilOpplastingService = dokumentFilOpplastingService;
		this.dokarkivMellomlagerBucketStorage = dokarkivMellomlagerBucketStorage;
	}

	/// @throws DokumentFilAlleredeBruktException hvis en eller flere `dokumentFilId` allerede er brukt av en journalpost.
	/// @throws InputValideringFeiletException    hvis en eller flere `dokumentFilId` er ugyldige.
	public Set<UUID> validerDokumentFiler(OpprettJournalpostRequest request) {
		List<UUID> dokumentFilIder = hentDokumentFilIder(request);
		if (dokumentFilIder.isEmpty()) {
			return Set.of();
		}

		validerIngenDuplikater(dokumentFilIder);

		Set<UUID> unikeDokumentFilIder = Set.copyOf(dokumentFilIder);
		validerTilstand(unikeDokumentFilIder);
		validerFinnesIDokarkivMellomlager(unikeDokumentFilIder);
		return unikeDokumentFilIder;
	}

	private static String formatterFeilmelding(Map<UUID, List<String>> feil) {
		return format("Ugyldige dokumentFilId: %s", feil.entrySet().stream()
				.map(entry -> format("%s (%s)", entry.getKey(), String.join("; ", entry.getValue())))
				.collect(Collectors.joining(", ")));
	}

	public void hindreSlettingAvDokumentFiler(Set<UUID> dokumentFilIder) {
		int antallDokumentFiler = dokarkivMellomlagerBucketStorage.holdObjects(dokumentFilIder.stream()
				.map(UUID::toString)
				.toList());

		log.info("Hindrer sletting av {} dokumentFiler i bucket", antallDokumentFiler);
	}

	static List<UUID> hentDokumentFilIder(OpprettJournalpostRequest request) {
		if (request.getDokumenter() == null) {
			return List.of();
		}
		return request.getDokumenter().stream()
				.flatMap(dokument -> dokument.getDokumentvarianter().stream())
				.map(DokumentVariant::getDokumentFilId)
				.filter(Objects::nonNull)
				.toList();
	}

	private static void validerIngenDuplikater(List<UUID> dokumentFilIder) {
		List<UUID> duplikater = dokumentFilIder.stream()
				.collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.toList();
		if (!duplikater.isEmpty()) {
			throw new InputValideringFeiletException(format("dokumentFilId kan bare brukes i én dokumentvariant. Fant følgende duplikater: %s", duplikater));
		}
	}

	private void validerTilstand(Set<UUID> dokumentFilIder) {
		Map<UUID, List<String>> feil = new TreeMap<>();
		Map<UUID, DokumentFilOpplasting> opplastinger = dokumentFilOpplastingService.hentTilstander(dokumentFilIder);
		dokumenterTilstandFeil(dokumentFilIder, feil, opplastinger);
		if (erBruktAvJournalpost(opplastinger)) {
			throw new DokumentFilAlleredeBruktException(formatterFeilmelding(feil));
		}

		if (!feil.isEmpty()) {
			throw new InputValideringFeiletException(formatterFeilmelding(feil));
		}
	}

	private static void dokumenterTilstandFeil(Set<UUID> dokumentFilIder, Map<UUID, List<String>> feil, Map<UUID, DokumentFilOpplasting> opplastinger) {
		dokumentFilIder.forEach(dokumentFilId -> {
			DokumentFilOpplasting dokumentFilOpplasting = opplastinger.get(dokumentFilId);
			if (dokumentFilOpplasting == null) {
				leggTilFeil(feil, dokumentFilId, "finnes ikke");
			} else if (!dokumentFilOpplasting.erLastetOpp()) {
				leggTilFeil(feil, dokumentFilId, format("har tilstand=%s, forventet %s", dokumentFilOpplasting.getTilstand(), LASTET_OPP));
			}
		});
	}

	private static boolean erBruktAvJournalpost(Map<UUID, DokumentFilOpplasting> opplastinger) {
		return opplastinger.values().stream().anyMatch(DokumentFilOpplasting::erBruktAvJournalpost);
	}

	private void validerFinnesIDokarkivMellomlager(Set<UUID> dokumentFilIder) {
		Map<UUID, List<String>> feil = new TreeMap<>();
		Set<String> finnes = dokarkivMellomlagerBucketStorage.existsObjects(dokumentFilIder.stream()
				.map(UUID::toString)
				.toList());
		dokumentFilIder.stream()
				.filter(dokumentFilId -> !finnes.contains(dokumentFilId.toString()))
				.forEach(dokumentFilId -> leggTilFeil(feil, dokumentFilId, "finnes ikke i dokarkivmellomlager"));

		if (!feil.isEmpty()) {
			throw new InputValideringFeiletException(formatterFeilmelding(feil));
		}
	}

	private static void leggTilFeil(Map<UUID, List<String>> feil, UUID dokumentFilId, String aarsak) {
		feil.computeIfAbsent(dokumentFilId, _ -> new ArrayList<>()).add(aarsak);
	}
}
