package no.nav.dokarkiv.safintern.tilknyttetjournalpost;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Collections.emptyList;

@Repository
public class SafinternTilknyttetJournalpostRepository {
	private final EntityManager em;
	private final CriteriaBuilderFactory cbf;
	private final EntityViewManager evm;

	SafinternTilknyttetJournalpostRepository(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
		this.em = entityManager;
		this.cbf = cbf;
		this.evm = evm;
	}

	List<JournalpostView> hentTilknyttedeJournalposterGjenbruk(long dokumentInfoId, EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs) {
		try {
			CriteriaBuilder<Journalpost> cb = cbf.create(em, Journalpost.class, "j")
					.where("j.journalpostDokumentInfoRelasjoner.dokumentInfo.dokumentInfoId").eq(dokumentInfoId);

			CriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, dokumenterOrder(evs, cb));
			return journalpostBuilder.getResultList();
		} catch (NoResultException e) {
			return emptyList();
		}

	}

	private static CriteriaBuilder<Journalpost> dokumenterOrder(EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs, CriteriaBuilder<Journalpost> cb) {
		if (evs.getFetches().isEmpty() || evs.getFetches().stream().anyMatch(f -> f.contains("dokumenter"))) {
			return cb.orderByAsc("journalpostDokumentInfoRelasjoner.tilknyttetJournalpostSom")
					.orderByAsc("journalpostDokumentInfoRelasjoner.journalpostDokumentInfoRelasjonId");
		}
		return cb;
	}
}
