package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;

import javax.persistence.EntityManager;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DefaultDokumentFilRepository {
	public static final String FIL_UUID_DUMMY_DOKUMENT = "DUMMY_DOKUMENT";

	private final DokumentFilRepository dokumentFilRepository;
	private final EntityManager entityManager;

	public DefaultDokumentFilRepository(DokumentFilRepository dokumentFilRepository, EntityManager entityManager) {
		this.dokumentFilRepository = dokumentFilRepository;
		this.entityManager = entityManager;
	}

	public DokumentFil findByFilUuid(String filUuid){
		String maybeDummyfilUuid = filUuid;
		if (isFildetaljerWithArkivVariantSkjermet(filUuid)) {
			maybeDummyfilUuid = FIL_UUID_DUMMY_DOKUMENT;
		}

		return dokumentFilRepository.findByFilUuid(maybeDummyfilUuid);
	}

	private boolean isFildetaljerWithArkivVariantSkjermet(String filUuid) {
		return isFalse(entityManager.createQuery("select 'fildetaljer er skjermet' from FilDetaljer where filUuid=:filUuid and skjermingType is not null and variantFormat='ARKIV'").setParameter("filUuid", filUuid).getResultList().isEmpty());
	}
}
