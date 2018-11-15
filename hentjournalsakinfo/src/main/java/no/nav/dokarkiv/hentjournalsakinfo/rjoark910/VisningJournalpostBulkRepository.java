package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.hibernate.query.Query;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class VisningJournalpostBulkRepository {
	private final EntityManager entityManager;

	@Inject
	public VisningJournalpostBulkRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> visningJournalposter(final List<Long> journalpostIds) {
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"left outer join fetch j.saksrelasjon s " +
						"left outer join fetch j.behandlingsrelasjon " +
						"join fetch j.journalpostDokumentInfoRelasjoner jprel " +
						"join fetch jprel.dokumentInfo " +
						"where " +
						"j.journalpostId in :journalpostIds " +
						"order by j.changeStamp.createdDate", Journalpost.class)
				.unwrap(Query.class)
				.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
				.setParameter("journalpostIds", journalpostIds)
				.getResultList();
	}

}
