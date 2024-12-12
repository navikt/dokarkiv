package no.nav.dokarkiv.safintern.journalpost;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class SafinternJournalpostRepository {
	private final EntityManager em;
	private final CriteriaBuilderFactory cbf;
	private final EntityViewManager evm;

	SafinternJournalpostRepository(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
		this.em = entityManager;
		this.cbf = cbf;
		this.evm = evm;
	}

	Optional<JournalpostView> hentJournalpostById(final Long journalpostId, EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs) {
		try {
			CriteriaBuilder<Journalpost> cb = cbf.create(em, Journalpost.class, "j")
					.where("journalpostId").eq(journalpostId);

			CriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, dokumenterOrder(evs, cb));
			return Optional.of(journalpostBuilder.getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	Optional<JournalpostView> hentJournalpostByIdDokumentInfoId(final Long journalpostId, final Long dokumentInfoId, EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs) {
		try {
			CriteriaBuilder<Journalpost> cb = cbf.create(em, Journalpost.class, "j")
					.where("j.journalpostDokumentInfoRelasjoner.embeddedId.journalpostId").eq(journalpostId)
					.where("j.journalpostDokumentInfoRelasjoner.embeddedId.dokumentInfoId").eq(dokumentInfoId);

			CriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, dokumenterOrder(evs, cb));
			return Optional.of(journalpostBuilder.getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
		}
	}

	Optional<JournalpostView> hentJournalpostByEksternReferanseId(final String eksternReferanseId, EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs) {
		try {
			CriteriaBuilder<Journalpost> cb = cbf.create(em, Journalpost.class, "j")
					.where("kanalReferanseId").eq(eksternReferanseId);

			CriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, dokumenterOrder(evs, cb));
			return Optional.of(journalpostBuilder.getSingleResult());
		} catch (NoResultException e) {
			return Optional.empty();
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
