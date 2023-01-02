package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.SkjermingService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

public class JournalpostRepositorySkjermet {

	private final JournalpostRepository journalpostRepository;
	private final SkjermingService skjermingService;

	public JournalpostRepositorySkjermet(JournalpostRepository journalpostRepository, SkjermingService skjermingService) {
		this.journalpostRepository = journalpostRepository;
		this.skjermingService = skjermingService;
	}

	public Optional<Journalpost> findById(Long id) {
		return skjermingService.isJournalpostSkjermet(id) ? Optional.empty() :
				journalpostRepository.findById(id);
	}

	public Journalpost save(Journalpost journalpost) {
		return journalpostRepository.persist(journalpost);
	}

	public boolean existsById(Long id) {
		return isFalse(skjermingService.isJournalpostSkjermet(id)) && journalpostRepository
				.existsById(id);
	}

	public Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Journalpost journalpost = journalpostRepository.findJournalpostByTilleggsopplysningerNokkelAndVerdi(nokkel, verdi)
				.orElse(null);
		if (Objects.nonNull(journalpost) && isFalse(skjermingService.isJournalpostSkjermet(journalpost))) {
			return journalpost.getJournalpostId();
		}
		return null;
	}

	public Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		return journalpostRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
	}

	public Long findJournalpostIdByDokumentinfoId(String dokumentinfoId) {
		Long jpId = journalpostRepository.findJournalpostIdByDokumentinfoId(dokumentinfoId);
		if (jpId == null) {
			return null;
		}
		return skjermingService.isJournalpostSkjermet(jpId) ? null : jpId;
	}

	public List<Long> findAllJournalpostIdsByDokumentInfoId(Long dokumentInfoId) {
		List<Long> journalpostIds = journalpostRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
				.stream().map(SkjermingService::convertBigToLong).collect(Collectors.toList());
		return journalpostIds.stream()
				.filter(journalpostId -> isFalse(skjermingService.isJournalpostSkjermet(journalpostId)))
				.collect(Collectors.toList());
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId) {
		Optional<Journalpost> journalpost = journalpostRepository.findTopByKanalReferanseId(kanalReferanseId);

		if (journalpost.isPresent()) {
			return skjermingService.isJournalpostSkjermet(journalpost.get()) ? Optional.empty() : journalpost;
		}

		return Optional.empty();
	}

	public List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode) {
		List<Journalpost> journalpostList = journalpostRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanalCode);
		return journalpostList.stream()
				.filter(journalpost -> isFalse(skjermingService.isJournalpostSkjermet(journalpost)))
				.collect(Collectors.toList());
	}
}
