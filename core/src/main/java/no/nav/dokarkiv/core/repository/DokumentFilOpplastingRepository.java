package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DokumentFilOpplastingRepository extends HibernateRepository<DokumentFilOpplasting>, BaseJpaRepository<DokumentFilOpplasting, UUID> {
	boolean existsByEksternDokumentReferanseId(String eksternDokumentReferanseId);
	DokumentFilOpplasting findByEksternDokumentReferanseId(String eksternDokumentReferanseId);
	List<DokumentFilOpplasting> findAllByDokumentFilIdIn(Collection<UUID> dokumentFilIder);
}
