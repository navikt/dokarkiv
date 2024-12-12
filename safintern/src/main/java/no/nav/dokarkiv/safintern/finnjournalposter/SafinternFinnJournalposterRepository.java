package no.nav.dokarkiv.safintern.finnjournalposter;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.LeafOngoingSetOperationCTECriteriaBuilder;
import com.blazebit.persistence.PagedList;
import com.blazebit.persistence.PaginatedCriteriaBuilder;
import com.blazebit.persistence.view.EntityViewManager;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.EntityManager;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostIdSubqueryCte;
import no.nav.dokarkiv.core.domain.entities.Saksrelasjon;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.FS22;
import static no.nav.dokarkiv.core.domain.codes.FagsystemCode.PEN;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.M;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.MO;

@Repository
public class SafinternFinnJournalposterRepository {
	private static final Collection<JournalStatusCode> ALL_LEGAL_JOURNALSTATUSER = List.of(JournalStatusCode.values());
	private static final int GSAKER_PARTITION_SIZE = 1000;

	private final EntityManager em;
	private final CriteriaBuilderFactory cbf;
	private final EntityViewManager evm;

	SafinternFinnJournalposterRepository(EntityManager entityManager, CriteriaBuilderFactory cbf, EntityViewManager evm) {
		this.em = entityManager;
		this.cbf = cbf;
		this.evm = evm;
	}

	PagedList<JournalpostView> finnJournalposterStatus(List<Long> psakSaker, List<Long> gsakSaker, boolean visFeilregistrerte, boolean visKunFeilregistrerte,
													   List<String> brukerIdenterPadded, Instant fraDato, Optional<Instant> tilDato,
													   List<JournalStatusCode> journalstatuser, List<JournalpostTypeCode> journalposttyper,
													   EntityViewSetting<JournalpostView, PaginatedCriteriaBuilder<JournalpostView>> evs) {

		var queryRoot = cbf.create(em, Journalpost.class, "j").with(JournalpostIdSubqueryCte.class)

				// dette er et triks for å få samme type på alle query-delene som sendes inn under
				.bind("journalpostId").select("null").where("1").eq(0)
				.union();

		var withGsakSakerSubQuery = createGsakSakerQuery(queryRoot, gsakSaker, visFeilregistrerte, visKunFeilregistrerte)
				.union();
		var withPsakSakerSubQuery = createPsakSakerQuery(withGsakSakerSubQuery, psakSaker, visFeilregistrerte, visKunFeilregistrerte)
				.union();
		var withAllSakSubQueries = createMidlertidigSakerQuery(visFeilregistrerte, visKunFeilregistrerte, brukerIdenterPadded, withPsakSakerSubQuery)
				.endSet().end();

		CriteriaBuilder<Journalpost> cb = withAllSakSubQueries
				.where("j.journalpostId").in().from(JournalpostIdSubqueryCte.class).select("journalpostId").end()

				.whereOr()
					.whereAnd()
						.where("j.saksrelasjon.feilregistrert").eq(true)
						.where("j.journalstatus").in(ALL_LEGAL_JOURNALSTATUSER)
					.endAnd()
					.whereAnd()
						.where("j.saksrelasjon.feilregistrert").isNull()
						.where("j.journalstatus").in(journalstatuser)
					.endAnd()
					.whereAnd()
						.where("j.saksrelasjon.feilregistrert").eq(false)
						.where("j.journalstatus").in(journalstatuser)
					.endAnd()
				.endOr()
				.where("j.changeStamp.createdDate").ge(Date.from(fraDato))
				.where("j.journalstatus").in(journalstatuser)
				.where("j.journalposttype").in(journalposttyper);

		if (tilDato.isPresent()) {
			cb = cb.where("j.changeStamp.createdDate").le(Date.from(tilDato.get()));
		}

		cb = cb.orderByDesc("j.journalpostId");

		PaginatedCriteriaBuilder<JournalpostView> journalpostBuilder = evm.applySetting(evs, cb);
		return journalpostBuilder.getResultList();
	}

	private static LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> createMidlertidigSakerQuery(boolean visFeilregistrerte, boolean visKunFeilregistrerte, List<String> brukerIdenterPadded, LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> queryRoot) {
		var partialSubQuery = queryRoot
				.from(Bruker.class, "b")
				.innerJoin("b.journalpost", "jp")
				.leftJoin("jp.saksrelasjon", "s")
				.bind("journalpostId").select("jp.journalpostId")
				.where("b.brukerId").in(brukerIdenterPadded)
				.where("jp.journalstatus").in(M, MO, D);
		return createFeilregistrertClause(partialSubQuery, visFeilregistrerte, visKunFeilregistrerte);
	}

	private static LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> createGsakSakerQuery(LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> queryRoot, List<Long> gsakSaker, boolean visFeilregistrerte, boolean visKunFeilregistrerte) {
		// gsak-saker-listen må deles opp i deler på 1000 elementer som er maks for en IN clause i oracle
		LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> basicSakerQuery;
		basicSakerQuery = createBasicSakerQuery(queryRoot, FS22,
				gsakSaker.subList(0, Math.min(GSAKER_PARTITION_SIZE, gsakSaker.size())),
				visFeilregistrerte, visKunFeilregistrerte);

		for (int i = GSAKER_PARTITION_SIZE; i < gsakSaker.size(); i += GSAKER_PARTITION_SIZE) {
			basicSakerQuery = createBasicSakerQuery(basicSakerQuery.union(), FS22,
					gsakSaker.subList(i, Math.min(i + GSAKER_PARTITION_SIZE, gsakSaker.size())),
					visFeilregistrerte, visKunFeilregistrerte);
		}
		return basicSakerQuery;
	}

	private static LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> createPsakSakerQuery(LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> queryRoot, List<Long> psakSaker, boolean visFeilregistrerte, boolean visKunFeilregistrerte) {
		return createBasicSakerQuery(queryRoot, PEN, psakSaker, visFeilregistrerte, visKunFeilregistrerte);
	}

	private static LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> createBasicSakerQuery(LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> queryRoot, FagsystemCode fagsystemCode, List<Long> sakIds, boolean visFeilregistrerte, boolean kunFeilregistrerte) {
		var partialSubQuery = queryRoot
				.from(Saksrelasjon.class, "s")
				.bind("journalpostId").select("s.journalpostId")
				.where("s.fagsystem").eq(fagsystemCode)
				.where("s.sakId").in(sakIds);
		return createFeilregistrertClause(partialSubQuery, visFeilregistrerte, kunFeilregistrerte);
	}

	private static LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> createFeilregistrertClause(LeafOngoingSetOperationCTECriteriaBuilder<CriteriaBuilder<Journalpost>> queryBuilder, boolean visFeilregistrerte, boolean kunFeilregistrerte) {
		if (kunFeilregistrerte) {
			return queryBuilder.where("s.feilregistrert").eq(true);
		} else {
			return queryBuilder
					.whereOr()
						.where("s.feilregistrert").isNull()
						.where("s.feilregistrert").in(visFeilregistrerte, false)
					.endOr();
		}
	}

}
