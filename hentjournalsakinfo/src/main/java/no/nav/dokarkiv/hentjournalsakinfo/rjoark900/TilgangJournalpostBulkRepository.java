package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.hibernate.query.Query;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class TilgangJournalpostBulkRepository {
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Arrays.asList(false);
	private static final List<Boolean> ALL_JOURNALPOST = Arrays.asList(true, false);

	private final EntityManager entityManager;

	@Inject
	public TilgangJournalpostBulkRepository(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> tilgangJournalposter(final List<String> sakIds,
												  final Arkivsaksystem arkivsaksystem,
												  TilgangJournalposterFilter tilgangJournalposterFilter) {
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"join fetch j.saksrelasjon s " +
						"left outer join fetch j.behandlingsrelasjon " +
						"join fetch j.journalpostDokumentInfoRelasjoner jprel " +
						"join fetch jprel.dokumentInfo " +
						"where " +
						"s.sakId in :sakIds and s.fagsystem = :arkivsaksystem " +
						"and (s.feilregistrert is null or s.feilregistrert in :visFeilregistrert) " +
						"and j.changeStamp.createdDate > :fraDato " +
						"and j.fagomrade in :inkluderTema " +
						"and j.journalposttype in :inkluderJournalpostType " +
						"and j.journalstatus in :inkluderJournalStatus", Journalpost.class)
				.unwrap(Query.class)
				.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE)
				.setParameter("sakIds", sakIds)
				.setParameter("arkivsaksystem", arkivsaksystem.getJoarkMapping())
				.setParameter("fraDato", Timestamp.valueOf(tilgangJournalposterFilter.getFraDato().atStartOfDay()))
				.setParameter("inkluderTema", tilgangJournalposterFilter.getInkluderTema())
				.setParameter("inkluderJournalpostType", tilgangJournalposterFilter.getInkluderJournalpostType())
				.setParameter("inkluderJournalStatus", tilgangJournalposterFilter.getInkluderJournalStatus())
				.setParameter("visFeilregistrert", tilgangJournalposterFilter.isVisFeilregistrerte() ? ALL_JOURNALPOST : NO_FEILREGISTRERT_JOURNALPOST)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Journalpost> tilgangMidlertidigeJournalposter(List<String> alleIdenter,
															  TilgangJournalposterFilter tilgangJournalposterFilter) {
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"join j.brukere b on b.brukerId in :alleIdenter " +
						"left outer join fetch j.saksrelasjon s " +
						"left outer join fetch j.behandlingsrelasjon " +
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
				.setParameter("fraDato", Timestamp.valueOf(tilgangJournalposterFilter.getFraDato().atStartOfDay()))
				.setParameter("inkluderTema", tilgangJournalposterFilter.getInkluderTema())
				.setParameter("inkluderJournalpostType", tilgangJournalposterFilter.getInkluderJournalpostType())
				.setParameter("inkluderJournalStatus", tilgangJournalposterFilter.getInkluderJournalStatus())
				.getResultList();
	}
}
