package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class TilgangJournalpostBulkRepository {
	private static final List<Boolean> NO_FEILREGISTRERT_JOURNALPOST = Collections.singletonList(false);
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
						"join j.saksrelasjon s on s.sakId in :sakIds and s.fagsystem = :arkivsaksystem and s.feilregistrert in(:visFeilregistrert) " +
						"where j.changeStamp.createdDate > :fraDato " +
						"and j.fagomrade in :inkluderTema " +
						"and j.journalposttype in :inkluderJournalpostType " +
						"and j.journalstatus in :inkluderJournalStatus", Journalpost.class)
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
															  TilgangMidlertidigeJournalposterFilter tilgangMidlertidigeJournalposterFilter) {
		return entityManager.createQuery(
				"select j " +
						"from Journalpost j " +
						"left join j.brukere b on b.brukerId in :alleIdenter " +
						"where j.changeStamp.createdDate > :fraDato " +
						"and j.fagomrade in :inkluderTema " +
						"and j.journalposttype in :inkluderJournalpostType " +
						"and j.journalstatus in :inkluderJournalStatus", Journalpost.class)
				.setParameter("alleIdenter", alleIdenter)
				.setParameter("fraDato", Timestamp.valueOf(tilgangMidlertidigeJournalposterFilter.getFraDato().atStartOfDay()))
				.setParameter("inkluderTema", tilgangMidlertidigeJournalposterFilter.getInkluderTema())
				.setParameter("inkluderJournalpostType", tilgangMidlertidigeJournalposterFilter.getInkluderJournalpostType())
				.setParameter("inkluderJournalStatus", tilgangMidlertidigeJournalposterFilter.getInkluderJournalStatus())
				.getResultList();
	}
}
