package no.nav.dokarkiv.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import org.springframework.stereotype.Repository;

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
                    .getSingleResult();
            return Optional.of(journalpostDokumentInfoRelasjon);
        } catch (NonUniqueResultException | NoResultException e) {
            return Optional.empty();
        }
    }
}
