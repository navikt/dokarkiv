package no.nav.dokarkiv.safintern.hentdokument;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import java.sql.Blob;
import java.util.Optional;

@Repository
class HentDokumentRepository {
	private static final String DOKUMENT_BY_AND_VARIANT_SQL = """
			select /*+ NO_PARALLEL */	fd.k_fil_t	as fil_type,
										df.fil		as fil
			from t_fil_detaljer fd
				left outer join t_dokument_fil df on fd.fil_uuid = df.fil_uuid
			where fd.dokument_info_id = :dokumentInfoId
				and fd.k_variant_format = :variantFormat
			""";

	private final EntityManager entityManager;

	public HentDokumentRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public Optional<HentDokumentDto> hentDokumentFromJoark(Long dokumentInfoId, VariantFormatCode variantFormat) {
		var resultList = entityManager
				.createNativeQuery(DOKUMENT_BY_AND_VARIANT_SQL, Tuple.class)
				.setParameter("dokumentInfoId", dokumentInfoId)
				.setParameter("variantFormat", variantFormat.name())
				.getResultList();

		if (resultList.isEmpty()) {
			return Optional.empty();
		}
		Tuple tuple = (Tuple) resultList.get(0);
		return Optional.of(new HentDokumentDto(tuple.get("fil_type", String.class), tuple.get("fil", Blob.class)));
	}
}