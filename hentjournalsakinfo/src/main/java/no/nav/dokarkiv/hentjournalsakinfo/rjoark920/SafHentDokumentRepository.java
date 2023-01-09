package no.nav.dokarkiv.hentjournalsakinfo.rjoark920;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

@Repository
public class SafHentDokumentRepository {
	private static final String DOKUMENT_BY_AND_VARIANT_SQL = """
			select /*+ NO_PARALLEL */ 	j.journalpost_id   as journalpost_id,
										fd.fil_uuid        as fil_uuid,
										fd.on_demand_id_fk as ondemand_id,
										fd.k_fil_t         as fil_type,
										df.fil as fil
			from t_fil_detaljer fd
				inner join t_dokument_info di on fd.dokument_info_id = di.dokument_info_id
				inner join t_jp_dok_info_rel jpr on di.dokument_info_id = jpr.dokument_info_id
				inner join t_journalpost j on jpr.journalpost_id = j.journalpost_id
				left outer join t_dokument_fil df on (fd.fil_uuid = df.fil_uuid)
			where fd.dokument_info_id = :dokumentInfoId
				and fd.k_variant_format = :variantFormat
			""";

	private final EntityManager entityManager;

	public SafHentDokumentRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Transactional(readOnly = true)
	public JoarkDokumentDto hentDokumentFromJoark(Long dokumentInfoId, VariantFormatCode variantFormat) {
		Object[] singleResult = (Object[]) entityManager
				.createNativeQuery(DOKUMENT_BY_AND_VARIANT_SQL)
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormat.name())
				.getResultList().get(0);
		return new JoarkDokumentDto(singleResult);
	}
}