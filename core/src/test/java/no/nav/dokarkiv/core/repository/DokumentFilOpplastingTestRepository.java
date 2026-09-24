package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.entities.DokumentFilOpplasting;

import java.util.UUID;

public interface DokumentFilOpplastingTestRepository extends HibernateRepository<DokumentFilOpplasting>, BaseJpaTestRepository<DokumentFilOpplasting, UUID> {
	DokumentFilOpplasting findByEksternDokumentReferanseId(String eksternDokumentReferanseId);
}
