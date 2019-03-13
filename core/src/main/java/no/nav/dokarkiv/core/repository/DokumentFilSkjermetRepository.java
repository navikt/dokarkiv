package no.nav.dokarkiv.core.repository;

import static org.apache.commons.lang3.BooleanUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.DokumentFil;

import javax.persistence.EntityManager;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class DokumentFilSkjermetRepository {
	public static final String FIL_UUID_DUMMY_DOKUMENT_KASSERT = "DUMMY_DOKUMENT_KASSERT";

	private final DokumentFilRepository dokumentFilRepository;
	private final EntityManager entityManager;

	public DokumentFilSkjermetRepository(DokumentFilRepository dokumentFilRepository, EntityManager entityManager) {
		this.dokumentFilRepository = dokumentFilRepository;
		this.entityManager = entityManager;
	}

	public DokumentFil findByFilUuid(String filUuid){
		String maybeDummyfilUuid = filUuid;

		/*Skal returnere DUMMY dokument hvis filUuid tilhører ARKIV variant og er skjermet eller hvis filUuid starter med DUMMY_DOKUMENT. Sjekk @KasserDokumentService **/
		if (isFildetaljerWithArkivVariantSkjermet(filUuid) || containsDummyDokumentKassert(filUuid)) {
			maybeDummyfilUuid = FIL_UUID_DUMMY_DOKUMENT_KASSERT;
		}

		return dokumentFilRepository.findByFilUuid(maybeDummyfilUuid);
	}

	private boolean containsDummyDokumentKassert(String filUuid) {
		return filUuid.contains(FIL_UUID_DUMMY_DOKUMENT_KASSERT);
	}


	private boolean isFildetaljerWithArkivVariantSkjermet(String filUuid) {
		return isFalse(entityManager.createQuery("select 'fildetaljer er skjermet' from FilDetaljer where filUuid=:filUuid and skjermingType is not null and variantFormat='ARKIV'").setParameter("filUuid", filUuid).getResultList().isEmpty());
	}
}
