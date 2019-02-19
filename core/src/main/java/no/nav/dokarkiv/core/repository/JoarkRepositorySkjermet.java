package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JoarkRepositorySkjermet {

	private final JoarkRepository joarkRepository;
	private final SkjermingService skjermingService;

	@Inject
	public JoarkRepositorySkjermet(JoarkRepository joarkRepository, SkjermingService skjermingService) {
		this.joarkRepository = joarkRepository;
		this.skjermingService = skjermingService;
	}

	public Optional<Journalpost> findById(Long id) {
		return skjermingService.isJournalpostSkjermet(id) ? Optional.empty() :
				joarkRepository.findById(id);
	}

	public Journalpost save(Journalpost journalpost) {
		return joarkRepository.save(journalpost);
	}

	public boolean existsById(Long id) {
		return isFalse(skjermingService.isJournalpostSkjermet(id)) && joarkRepository
				.existsById(id);
	}

	/**
	 * Only use in test!
	 */
	public void deleteAll() {
		joarkRepository.deleteAll();
	}

	public Iterable<Journalpost> findAll() {
		return StreamSupport.stream(joarkRepository.findAll().spliterator(), true)
				.filter(journalpost -> isFalse(skjermingService.isJournalpostSkjermet(journalpost)))
				.collect(Collectors.toList());
	}

	public Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Journalpost journalpost = joarkRepository.findJournalpostByTilleggsopplysningerNokkelAndVerdi(nokkel, verdi)
				.orElse(null);
		if (Objects.nonNull(journalpost) && isFalse(skjermingService.isJournalpostSkjermet(journalpost))) {
			return journalpost.getJournalpostId();
		}
		return null;
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, String mottakskanal) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottakskanal);

		if (journalpost.isPresent()) {
			return skjermingService.isJournalpostSkjermet(journalpost.get()) ? Optional.empty() : journalpost;
		}

		return Optional.empty();
	}

	public Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		return joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
	}

	public Long findJournalpostIdByDokumentinfoId(String dokumentinfoId) {
		Long jpId = joarkRepository.findJournalpostIdByDokumentinfoId(dokumentinfoId);
		if (jpId == null) {
			return null;
		}
		return skjermingService.isJournalpostSkjermet(jpId) ? null : jpId;
	}

	public List<Long> findAllJournalpostIdsByDokumentInfoId(Long dokumentInfoId) {
		List<Long> journalpostIds = joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
				.stream().map(SkjermingService::convertBigToLong).collect(Collectors.toList());
		return journalpostIds.stream()
				.filter(journalpostId -> isFalse(skjermingService.isJournalpostSkjermet(journalpostId)))
				.collect(Collectors.toList());
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId);

		if (journalpost.isPresent()) {
			return skjermingService.isJournalpostSkjermet(journalpost.get()) ? Optional.empty() : journalpost;
		}

		return Optional.empty();
	}

	public List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode) {
		List<Journalpost> journalpostList = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanalCode);
		return journalpostList.stream()
				.filter(journalpost -> isFalse(skjermingService.isJournalpostSkjermet(journalpost)))
				.collect(Collectors.toList());
	}
}
