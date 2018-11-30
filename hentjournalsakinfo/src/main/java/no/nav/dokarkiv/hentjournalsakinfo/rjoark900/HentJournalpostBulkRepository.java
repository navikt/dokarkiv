package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.hibernate.query.Query;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
// TODO fjern denne
@Deprecated
@Repository
public class HentJournalpostBulkRepository {
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Arrays.asList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);
	private final EntityManager entityManager;

	@Inject
	public HentJournalpostBulkRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> hentJournalposter(final List<String> sakIds,
											   final Arkivsaksystem arkivsaksystem,
											   BulkJournalposterFilter bulkJournalposterFilter) {
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"join fetch j.saksrelasjon s " +
						"join fetch j.journalpostDokumentInfoRelasjoner jprel " +
						"join fetch jprel.dokumentInfo " +
						"where " +
						"s.sakId in :sakIds and s.fagsystem = :arkivsaksystem " +
						"and (s.feilregistrert is null or s.feilregistrert = 0) " +
						"and j.changeStamp.createdDate > :fraDato " +
						"and j.journalposttype in :inkluderJournalpostType " +
						"and j.journalstatus in :inkluderJournalStatus", Journalpost.class)
				.unwrap(Query.class)
				.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
				.setParameter("sakIds", sakIds)
				.setParameter("arkivsaksystem", arkivsaksystem.getJoarkMapping())
				.setParameter("fraDato", Timestamp.valueOf(bulkJournalposterFilter.getFraDato().atStartOfDay()))
				.setParameter("inkluderJournalpostType", bulkJournalposterFilter.getInkluderJournalpostType())
				.setParameter("inkluderJournalStatus", bulkJournalposterFilter.getInkluderJournalStatus())
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> hentMidlertidigeJournalposter(List<String> alleIdenter,
														   BulkJournalposterFilter bulkJournalposterFilter) {
		if(bulkJournalposterFilter.getInkluderTema() == null || bulkJournalposterFilter.getInkluderTema().isEmpty()) {
			return new ArrayList<>();
		}
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"join j.brukere b on b.brukerId in :alleIdenter " +
						"left outer join fetch j.saksrelasjon s " +
						"join fetch j.journalpostDokumentInfoRelasjoner jprel " +
						"join fetch jprel.dokumentInfo " +
						"where " +
						"j.changeStamp.createdDate > :fraDato " +
						"and j.fagomrade in :inkluderTema " +
						"and j.journalposttype in :inkluderJournalpostType " +
						"and j.journalstatus in :inkluderJournalStatus", Journalpost.class)
				.unwrap(Query.class)
				.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
				.setParameter("alleIdenter", alleIdenter)
				.setParameter("fraDato", Timestamp.valueOf(bulkJournalposterFilter.getFraDato().atStartOfDay()))
				.setParameter("inkluderTema", bulkJournalposterFilter.getInkluderTema())
				.setParameter("inkluderJournalpostType", bulkJournalposterFilter.getInkluderJournalpostType())
				.setParameter("inkluderJournalStatus", bulkJournalposterFilter.getInkluderJournalStatus())
				.getResultList();
	}
}
