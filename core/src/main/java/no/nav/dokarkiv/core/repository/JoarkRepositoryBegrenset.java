package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class JoarkRepositoryBegrenset {

	private final JoarkRepository joarkRepository;
	private final BegrensningService begrensningService;

	@Inject
	public JoarkRepositoryBegrenset(JoarkRepository joarkRepository, BegrensningService begrensningService) {
		this.joarkRepository = joarkRepository;
		this.begrensningService = begrensningService;
	}

	public Optional<Journalpost> findById(Long id) {
		return begrensningService.isJournalpostBegrenset(id, BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional.empty() :
				joarkRepository.findById(id).map(begrensningService::addBegrensetDokumentInfoIdsToJournalpost);
	}

	public Journalpost save(Journalpost journalpost) {
		return joarkRepository.save(journalpost);
	}

	public boolean existsById(Long id) {
		return isFalse(begrensningService.isJournalpostBegrenset(id, BegrensningTypeCode.UTILGJENGELIGGJORT)) && joarkRepository
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
				.filter(journalpost -> isFalse(begrensningService.isJournalpostBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT)))
				.map(begrensningService::addBegrensetDokumentInfoIdsToJournalpost)
				.collect(Collectors.toList());
	}

	public Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Long jpId = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, String mottakskanal) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottakskanal);

		if (journalpost.isPresent()) {
			return begrensningService.isJournalpostBegrenset(journalpost.get()
					.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional.empty() : journalpost.map(begrensningService::addBegrensetDokumentInfoIdsToJournalpost);
		}
		return Optional.empty();
	}

	public Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Long jpId = joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public Long findJournalpostIdByDokumentinfoId(String dokumentinfoId) {
		Long jpId = joarkRepository.findJournalpostIdByDokumentinfoId(dokumentinfoId);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public List<Long> findAllJournalpostIdsByDokumentInfoId(Long dokumentInfoId) {
		List<Long> journalpostIds = joarkRepository.findAllJournalpostIdsByDokumentInfoId(dokumentInfoId)
				.stream().map(BegrensningService::convertBigToLong).collect(Collectors.toList());
		return journalpostIds.stream()
				.filter(journalpostId -> isFalse(begrensningService.isJournalpostBegrenset(journalpostId, BegrensningTypeCode.UTILGJENGELIGGJORT)))
				.collect(Collectors.toList());
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId);

		if (journalpost.isPresent()) {
			return begrensningService.isJournalpostBegrenset(journalpost.get()
					.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional.empty() : Optional.of(begrensningService.addBegrensetDokumentInfoIdsToJournalpost((journalpost
					.get())));
		}
		return Optional.empty();
	}

	public List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode) {
		List<Journalpost> journalpostList = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanalCode);
		return journalpostList.stream()
				.filter(journalpost -> isFalse(begrensningService.isJournalpostBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT)))
				.map(begrensningService::addBegrensetDokumentInfoIdsToJournalpost)
				.collect(Collectors.toList());
	}

}
