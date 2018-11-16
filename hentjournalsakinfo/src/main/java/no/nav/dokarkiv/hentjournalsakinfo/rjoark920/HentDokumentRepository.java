package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.SafHentDokumentDto;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class HentDokumentRepository {

	private final EntityManager entityManager;
	public static final String SAFHENDOKUMENTDTO_CANONICAL_NAME = SafHentDokumentDto.class.getCanonicalName();
	private static final String DOKUMENT_BY_ID_AND_VARIANT_SQL = "select new " +
			SAFHENDOKUMENTDTO_CANONICAL_NAME +
			"(" +
			" f.fil," +
			" fd.filtype" +
			" ) " +
			" from FilDetaljer fd" +
			" join DokumentFil f on fd.filUuid = f.filUuid" +
			" where fd.dokumentInfo.dokumentInfoId = :dokumentinfoId" +
			" and fd.variantFormat = :dokumentVariant";


	public HentDokumentRepository(EntityManager entityManager) {
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