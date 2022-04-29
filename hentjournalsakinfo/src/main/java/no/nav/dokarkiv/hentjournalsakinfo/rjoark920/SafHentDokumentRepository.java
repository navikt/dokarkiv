package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.persistence.EntityManager;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class SafHentDokumentRepository {
	private static final String DOKUMENT_BY_ID_AND_VARIANT_SQL = "select new " +
			JoarkDokumentDto.class.getCanonicalName() +
			"(" +
			" j.journalpostId," +
			" j.journalposttype," +
			" fd.filUuid," +
			" fd.onDemandId," +
			" fd.filtype," +
			" f.fil" +
			" ) " +
			" from FilDetaljer fd" +
			" join fd.dokumentInfo.journalpostRelasjoner rel" +
			" join rel.journalpost j" +
			" left join DokumentFil f on fd.filUuid = f.filUuid" +
			" where fd.dokumentInfo.dokumentInfoId = :dokumentinfoId" +
			" and fd.variantFormat = :variantFormat";

	private final EntityManager entityManager;

	@Inject
	public SafHentDokumentRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional(readOnly = true)
	public JoarkDokumentDto hentDokumentFromJoark(Long dokumentInfoId, VariantFormatCode variantFormat) {
		return entityManager
				.createQuery(DOKUMENT_BY_ID_AND_VARIANT_SQL, JoarkDokumentDto.class)
				.setParameter("dokumentinfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormat)
				.getResultList()
				// Tilgangskontroll er allerede gjort fra saf så vi tar den første journalposten vi finner
				.get(0);
	}
}