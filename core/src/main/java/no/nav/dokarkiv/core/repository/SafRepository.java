package no.nav.dokarkiv.core.repository;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class SafRepository {

	private final EntityManager entityManager;
	private static final String DOKUMENT_BY_ID_AND_VARIANT_SQL = "select new " +
			" no.nav.dokarkiv.core.repository.SafHentDokumentDto(" +
			" f.fil," +
			" fd.filtype" +
			" ) " +
			" from FilDetaljer fd" +
			" join DokumentFil f on fd.filUuid = f.filUuid" +
			" where fd.dokumentInfo.dokumentInfoId = :dokumentinfoId" +
			" and fd.variantFormat = :dokumentVariant";


	public SafRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public SafHentDokumentDto queryForDokumentAndDokumenttype(Long dokumentInfoId, VariantFormatCode variantFormat) {
		return entityManager
				.createQuery(DOKUMENT_BY_ID_AND_VARIANT_SQL, SafHentDokumentDto.class)
				.setParameter("dokumentinfoId", dokumentInfoId)
				.setParameter("dokumentVariant", variantFormat)
				.getSingleResult();
	}
}