package no.nav.dokarkiv.journalpost.v1.services;

import no.nav.dokarkiv.core.repository.AvstemReferanseRepository;
import no.nav.dokarkiv.journalpost.v1.api.AvstemmingReferanser;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

import static java.util.function.Predicate.not;

@Service
public class AvstemReferanserService {
	private final AvstemReferanseRepository avstemReferanseRepository;

	public AvstemReferanserService(AvstemReferanseRepository avstemReferanseRepository) {
		this.avstemReferanseRepository = avstemReferanseRepository;
	}

	public List<String> avstemReferanser(AvstemmingReferanser referanser) {
		Set<String> existingReferences = avstemReferanseRepository.findKanalReferanseIdsNotMatchedInDB(referanser.referanser());
		return referanser.referanser().stream()
				.filter(not(existingReferences::contains))
				.toList();
	}
}
