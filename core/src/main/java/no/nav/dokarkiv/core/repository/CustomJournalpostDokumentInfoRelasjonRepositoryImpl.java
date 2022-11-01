package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.hibernate.jpa.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import java.util.Optional;

@Repository
public class CustomJournalpostDokumentInfoRelasjonRepositoryImpl implements CustomJournalpostDokumentInfoRelasjonRepository {
    private final EntityManager entityManager;

    public CustomJournalpostDokumentInfoRelasjonRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<JournalpostDokumentInfoRelasjon> findByJournalpostJournalpostIdAndDokumentInfoDokumentInfoId(Long journalpostId, Long dokumentInfoId) {
        try {
            final JournalpostDokumentInfoRelasjon journalpostDokumentInfoRelasjon = entityManager.createQuery(
                    "select distinct jpr from JournalpostDokumentInfoRelasjon jpr where jpr.embeddedId.journalpostId = :journalpostId and jpr.embeddedId.dokumentInfoId = :dokumentInfoId",
                    JournalpostDokumentInfoRelasjon.class)
                    .setParameter("journalpostId", journalpostId)
                    .setParameter("dokumentInfoId", dokumentInfoId)
                    .setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false)
                    .getSingleResult();
            return Optional.of(journalpostDokumentInfoRelasjon);
        } catch (NonUniqueResultException | NoResultException e) {
            return Optional.empty();
        }
    }
}
