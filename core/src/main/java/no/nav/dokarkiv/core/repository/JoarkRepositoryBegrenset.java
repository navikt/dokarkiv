package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
public class JoarkRepositoryBegrenset {

	private final JoarkRepository joarkRepository;
	private final BegrensningService begrensningService;
	private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;

	public JoarkRepositoryBegrenset(JoarkRepository joarkRepository, BegrensningService begrensningService, JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository) {
		this.joarkRepository = joarkRepository;
		this.begrensningService = begrensningService;
		this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
	}

	public Optional<Journalpost> findById(Long id) {
		if (begrensningService.isJournalpostBegrenset(id, BegrensningTypeCode.UTILGJENGELIGGJORT)) {
			return Optional.empty();
		}

		Optional<Journalpost> journalpost = joarkRepository.findById(id);
		return journalpost.map(this::addBegrensetRelasjonerToJournalpost);
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
				.map(this::addBegrensetRelasjonerToJournalpost)
				.collect(Collectors.toList());
	}

	public Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Long jpId = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, String mottakskanal) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottakskanal);
		return journalpost.isPresent() ? begrensningService.isJournalpostBegrenset(journalpost.get()
				.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional.empty() : Optional.of(addBegrensetRelasjonerToJournalpost(journalpost
				.get())) : Optional.empty();
	}

	public Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
		Long jpId = joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public Long findJournalpostIdByDokumentinfoId(String dokumentinfoId) {
		Long jpId = joarkRepository.findJournalpostIdByDokumentinfoId(dokumentinfoId);
		return begrensningService.isJournalpostBegrenset(jpId, BegrensningTypeCode.UTILGJENGELIGGJORT) ? null : jpId;
	}

	public Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId) {
		Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId);

		if (journalpost.isPresent()) {
			return begrensningService.isJournalpostBegrenset(journalpost.get()
					.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT) ? Optional.empty() : Optional.of(addBegrensetRelasjonerToJournalpost(journalpost
					.get()));
		}
		return Optional.empty();
	}

	public Optional<List<Journalpost>> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode) {
		Optional<List<Journalpost>> journalpostList = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanalCode);
		return journalpostList.map(journalposts -> journalposts.stream()
				.filter(journalpost -> isFalse(begrensningService.isJournalpostBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT)))
				.map(this::addBegrensetRelasjonerToJournalpost)
				.collect(Collectors.toList()));
	}

	private Journalpost addBegrensetRelasjonerToJournalpost(Journalpost journalpost) {
		List<Long> begrensetDokumentInfoIds = journalpostDokumentInfoRelasjonRepository.findBegrensetRelasjonDokumentInfoIdByJournalpostId(journalpost
				.getJournalpostId()).orElseGet(ArrayList::new).stream().map(BigInteger::longValue).collect(Collectors.toList());
		journalpost.addAllbegrensetRelasjonerDokumentInfoIds(begrensetDokumentInfoIds);
		return journalpost;
	}

}
