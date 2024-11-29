package no.nav.dokarkiv.safintern.journalstatus;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

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

	PagedList<JournalpostView> finnJournalposterStatus(JournalStatusCode journalStatus, List<JournalpostTypeCode> typer, Instant fraDato,
													   EntityViewSetting<JournalpostView, PaginatedCriteriaBuilder<JournalpostView>> evs) {
		CriteriaBuilder<Journalpost> cb = cbf.create(em, Journalpost.class, "j")
				.where("j.journalstatus").eq(journalStatus)
				.where("j.journalposttype").in(typer)
				.where("j.changeStamp.createdDate").gt(Timestamp.from(fraDato))
				.orderByDesc("j.journalpostId");

		PaginatedCriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, cb);
		return journalpostBuilder.getResultList();
	}

}
