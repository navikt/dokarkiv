package no.nav.dokarkiv.core.repository;

import javafx.util.Pair;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.Base64;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class SafRepository {

	private final EntityManager entityManager;
	private static final String DOKUMENT_BY_ID_AND_VARIANT_SQL = "SELECT f.FIL, fd.K_FIL_T" +
			"from T_FIL_DETALJER fd" +
			"join T_DOKUMENT_FIL f on fd.FIL_UUID = f.FIL_UUID" +
			"where fd.DOKUMENT_INFO_ID = :dokumentinfoId" +
			"and fd.K_VARIANT_FORMAT = :dokumentVariant"; //	441359809, ARKIV


	public SafRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Pair<Base64, String> hentDokumentAndType(String dokumentInfoId, String variantFormat) {

		SafHentDokumentTo safDto = queryForDokumentAndDokumenttype(dokumentInfoId, variantFormat).get(0);

		return new Pair<>(safDto.getDokumentinfoId(), safDto.getDokumentVariant());
	}

	private List<SafHentDokumentTo> queryForDokumentAndDokumenttype(String dokumentInfoId, String variantFormat) {
		return entityManager
				.createNativeQuery(DOKUMENT_BY_ID_AND_VARIANT_SQL, SafHentDokumentTo.class)
				.setParameter("dokumentinfoId", dokumentInfoId)
				.setParameter("dokumentinfoVariant", variantFormat)
				.getResultList(); //.getSingleResult eksisterer, men gir return type Object.
	}
}