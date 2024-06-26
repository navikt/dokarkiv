package no.nav.dokarkiv.safintern.journalstatus;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.sql.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static no.nav.dokarkiv.safintern.views.FetchPaths.DOKUMENTER;

@Repository
public class SafinternJournalStatusRepository {
	private final EntityManager em;
	private final CriteriaBuilderFactory cbf;
	private final EntityViewManager evm;

	SafinternJournalStatusRepository(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
		this.em = entityManager;
		this.cbf = cbf;
		this.evm = evm;
	}

	List<JournalpostView> finnJournalposterStatus(JournalStatusCode journalStatus, List<JournalpostTypeCode> typer, Date fraDato,
												  Long forrigeJournalpostId, Integer antallRader, EntityViewSetting<JournalpostView,
												  CriteriaBuilder<JournalpostView>> evs) {
		try {
			var rader = ( antallRader == null || antallRader < 1 ) ? 200 : antallRader;
			CriteriaBuilder<Journalpost> cb = forrigeJournalpostId == null ?
					createJournalpostCriteriaBuilderFirstPage(journalStatus, typer, fraDato, rader) :
					createJournalpostCriteriaBuilder(journalStatus, typer, fraDato, rader, forrigeJournalpostId);

			CriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, dokumenterOrder(evs, cb));
			return journalpostBuilder.getResultList();
		} catch (NoResultException e) {
			return emptyList();
		}

	}

	private CriteriaBuilder<Journalpost> createJournalpostCriteriaBuilder(JournalStatusCode journalStatus, List<JournalpostTypeCode> typer, Date fraDato, int antallRader, Long forrigeJournalpostId) {
		return cbf.create(em, Journalpost.class, "jp")
				.innerJoinOnEntitySubquery(Journalpost.class, "j")
					.orderByDesc("j.journalpostId")
					.where("j.journalstatus").eq(journalStatus)
					.where("j.journalposttype").in(typer)
					.where("j.changeStamp.createdDate").gt(fraDato)
					.where("j.journalpostId").lt(forrigeJournalpostId)
					.setMaxResults(antallRader)
				.end()
				.on("j.journalpostId").eqExpression("jp.journalpostId")
				.end();
	}

	private CriteriaBuilder<Journalpost> createJournalpostCriteriaBuilderFirstPage(JournalStatusCode journalStatus, List<JournalpostTypeCode> typer, Date fraDato, int antallRader) {
		return cbf.create(em, Journalpost.class, "jp")
				.innerJoinOnEntitySubquery(Journalpost.class, "j")
				.orderByDesc("j.journalpostId")
				.where("j.journalstatus").eq(journalStatus)
				.where("j.journalposttype").in(typer)
				.where("j.changeStamp.createdDate").gt(fraDato)
				.setMaxResults(antallRader)
				.end()
				.on("j.journalpostId").eqExpression("jp.journalpostId")
				.end();
	}

	private static CriteriaBuilder<Journalpost> dokumenterOrder(EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs, CriteriaBuilder<Journalpost> cb) {
		if (evs.getFetches().isEmpty() || evs.getFetches().stream().anyMatch(f -> f.contains(DOKUMENTER))) {
			return cb.orderByAsc("journalpostDokumentInfoRelasjoner.tilknyttetJournalpostSom")
					.orderByAsc("journalpostDokumentInfoRelasjoner.journalpostDokumentInfoRelasjonId");
		}
		return cb;
	}
}
