package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Transactional
public class JournalpostDokumentInfoRelasjonRepositoryBegrenset {

    private final JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository;
    private final BegrensningService begrensningService;

    public JournalpostDokumentInfoRelasjonRepositoryBegrenset(JournalpostDokumentInfoRelasjonRepository journalpostDokumentInfoRelasjonRepository, BegrensningService begrensningService) {
        this.journalpostDokumentInfoRelasjonRepository = journalpostDokumentInfoRelasjonRepository;
        this.begrensningService = begrensningService;
    }

    public Optional<List<JournalpostDokumentInfoRelasjon>> findAllByDokumentInfoDokumentInfoId(Long dokumentInfoId) {
        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository
                .findAllByDokumentInfoDokumentInfoId(dokumentInfoId);
        return journalpostDokumentInfoRelasjons.map(relasjonList -> relasjonList
                .stream()
                .filter(relasjon -> isFalse(begrensningService.isJournalpostDokumentInfoRelasjonBegrenset(
                        relasjon.getJournalpost() == null ? null : relasjon.getJournalpost().getJournalpostId(),
                        relasjon.getDokumentInfo() == null ? null : relasjon.getDokumentInfo()
                                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT))).collect(Collectors.toList()));
    }


    public Optional<List<JournalpostDokumentInfoRelasjon>> findAllByJournalpostJournalpostId(Long journalpostId) {
        Optional<List<JournalpostDokumentInfoRelasjon>> journalpostDokumentInfoRelasjons = journalpostDokumentInfoRelasjonRepository
                .findAllByJournalpostJournalpostId(journalpostId);
        return journalpostDokumentInfoRelasjons.map(relasjonList -> relasjonList
                .stream()
                .filter(relasjon -> isFalse(begrensningService.isJournalpostDokumentInfoRelasjonBegrenset(
                        relasjon.getJournalpost() == null ? null : relasjon.getJournalpost().getJournalpostId(),
                        relasjon.getDokumentInfo() == null ? null : relasjon.getDokumentInfo()
                                .getDokumentInfoId(), BegrensningTypeCode.UTILGJENGELIGGJORT))).collect(Collectors.toList()));
    }

    public void delete(JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon) {
        journalpostDokumentInfoRelasjonRepository.delete(journalpostDokumentInfoRelasjon);
    }


}
