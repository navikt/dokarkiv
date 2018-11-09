package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.transaction.annotation.Transactional;

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

    public JoarkRepositoryBegrenset(JoarkRepository joarkRepository) {
        this.joarkRepository = joarkRepository;
    }

    public Optional<Journalpost> findById(Long id) {
        Optional<Journalpost> journalpost = joarkRepository.findById(id);
        return journalpost.filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? Optional.empty() : journalpost;
    }

    public Journalpost save(Journalpost journalpost) {
        return joarkRepository.save(journalpost);
    }

    public boolean existsById(Long id) {
        return joarkRepository.findById(id)
                .filter(jp -> isFalse(jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .isPresent();
    }

    /**
     * Only use in test!
     */
    public void deleteAll() {
        joarkRepository.deleteAll();
    }

    public Iterable<Journalpost> findAll() {
        return StreamSupport.stream(joarkRepository.findAll().spliterator(), true)
                .filter(journalpost -> isFalse(journalpost.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .collect(Collectors.toList());
    }

    public Long findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
        Long jpId = joarkRepository.findJournalpostIdByTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
        return joarkRepository.findById(jpId)
                .filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? null : jpId;
    }

    public Optional<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, String mottakskanal) {
        Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottakskanal);
        return journalpost.filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? Optional.empty() : journalpost;
    }

    public Long findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(String nokkel, String verdi) {
        Long jpId = joarkRepository.findDokumentinfoIdIdByDokumentinfoTilleggsopplysningerNokkelAndVerdi(nokkel, verdi);
        return jpId == null ? null : joarkRepository.findById(jpId)
                .filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? null : jpId;
    }

    public Long findJournalpostIdByDokumentinfoId(String dokumentinfoId) {
        Long jpId = joarkRepository.findJournalpostIdByDokumentinfoId(dokumentinfoId);
        return jpId == null ? null : joarkRepository.findById(jpId)
                .filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? null : jpId;
    }

    public Optional<Journalpost> findJournalpostByKanalReferanseId(String kanalReferanseId) {
        Optional<Journalpost> journalpost = joarkRepository.findJournalpostByKanalReferanseId(kanalReferanseId);
        return journalpost.filter(jp -> jp.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT))
                .isPresent() ? Optional.empty() : journalpost;
    }

    public List<Journalpost> findJournalpostByKanalReferanseIdAndMottakskanal(String kanalReferanseId, MottaksKanalCode mottaksKanalCode) {
        List<Journalpost> journalpostList = joarkRepository.findJournalpostByKanalReferanseIdAndMottakskanal(kanalReferanseId, mottaksKanalCode);
        return journalpostList.stream()
                .filter(journalpost -> isFalse(journalpost.isBegrenset(BegrensningTypeCode.UTILGJENGELIGGJORT)))
                .collect(Collectors
                        .toList());
    }


}
