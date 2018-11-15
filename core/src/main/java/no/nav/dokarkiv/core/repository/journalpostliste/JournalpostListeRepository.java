package no.nav.dokarkiv.core.repository.journalpostliste;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.BegrensningTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.service.BegrensningService;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Transactional
public class JournalpostListeRepository {

    private final EntityManager entityManager;
    private final BegrensningService begrensningService;

    @Inject
    public JournalpostListeRepository(EntityManager entityManager, BegrensningService begrensningService) {
        this.entityManager = entityManager;
        this.begrensningService = begrensningService;
    }

    @SuppressWarnings("unchecked")
    public List<Journalpost> findJournalpostListe(HentMinJPListeParameters hentMinJPListeParameters) {
        // If empty saksliste, return empty list
        if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
            return new ArrayList<>();
        }
        Session session = entityManager.unwrap(Session.class);
        JournalpostCriterionBuilder criterionBuilder = new JournalpostCriterionBuilder(session);

        Criteria criteria = criterionBuilder.buildCriteria(hentMinJPListeParameters);

        if (hentMinJPListeParameters.getMaxResults() > 0) {
            criteria.setMaxResults((int) hentMinJPListeParameters.getMaxResults());
        }

        if (hentMinJPListeParameters.getMaxResults() > 0 && hentMinJPListeParameters.getPageNr() > 0) {
            criteria.setFirstResult((int) (hentMinJPListeParameters.getMaxResults() * hentMinJPListeParameters.getPageNr()));
        }

        List<Journalpost> journalpostList = criteria.list();

        if (isFalse(hentMinJPListeParameters.isIncludeBegrensetJournalpost())) {
            journalpostList = journalpostList.stream()
                    .filter(journalpost -> isFalse(begrensningService.isJournalpostBegrenset(journalpost.getJournalpostId(), BegrensningTypeCode.UTILGJENGELIGGJORT)))
                    .collect(Collectors.toList());
            journalpostList.forEach(begrensningService::addBegrensetRelasjonerToJournalpost);
        }

        return journalpostList;
    }

    public long findTotalNumberOfJournalposts(HentMinJPListeParameters hentMinJPListeParameters) {
        if (hentMinJPListeParameters.getSaksListe().isEmpty()) {
            return 0;
        }
        Session session = entityManager.unwrap(Session.class);
        JournalpostCriterionBuilder criterionBuilder = new JournalpostCriterionBuilder(session);

        Criteria criteria = criterionBuilder.buildCriteria(hentMinJPListeParameters);
        addCountToCriteria(criteria);
        return (Long) criteria.uniqueResult();
    }

    private void addCountToCriteria(Criteria criteria) {
        criteria.setProjection(Projections.rowCount());
    }
}
