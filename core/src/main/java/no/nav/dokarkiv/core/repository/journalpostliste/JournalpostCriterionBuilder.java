package no.nav.dokarkiv.core.repository.journalpostliste;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Helper class for FindJournalpostListe to build the criteria dynamically based
 * on which paramters are set in the input.
 *
 * @author Rune Romundstad, Sirius IT
 * @author Per Kristian Foss, Visma Sirius
 * @author Lamisi Gurah Blackman, Accenture
 * @author Thomas Kåsene, Visma Consulting AS
 */
public class JournalpostCriterionBuilder extends CriterionBuilder {

	/**
	 * Constructs a new JournalpostCriterionBuilder.
	 *
	 * @param session The Hibernate Session.
	 */
	public JournalpostCriterionBuilder(Session session) {
		super(session);
	}

	protected Criteria buildCriteria(HentMinJPListeParameters parameters) {
		criteria = createCriteria(Journalpost.class, "journalpost");
		// Filter out duplicates due to outer join (FetchType.JOIN) on the many-to-many associations
		criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		// This alias is used to query saksliste and to check for feilregistrert
		criteria.createAlias("saksrelasjon", "saksrelasjon").setFetchMode("saksrelasjon", FetchMode.JOIN);

		// To avoid separate select queries for associated entities, we include everything in criteria
		if (parameters.isEagerFetchDokInfo()) {
			// include the following associasions in the query, avoiding separate fetch operations.
			criteria.setFetchMode("behandlingsrelasjon", FetchMode.JOIN);
			criteria.setFetchMode("journalpostDokumentInfoRelasjoner", FetchMode.JOIN);
			criteria.setFetchMode("journalpostDokumentInfoRelasjoner.dokumentInfo.fildetaljerListe", FetchMode.JOIN);

		}

		criteria.add()

		// Note that an empty saksliste would possibly return all journalposts, so we need
		// to return an empty resultset from query if this list is empty.
		if (!parameters.getSaksListe().isEmpty()) {
			addSokPaSak(parameters.getSaksListe());
		}
		if (!parameters.getTillattInnsynStatus().isEmpty()) {
			addJournalStatusListe(parameters.getTillattInnsynStatus());
		}
		if (!parameters.getSkjulFagomraade().isEmpty()) {
			addSkjulFagomraadeListe(parameters.getSkjulFagomraade());
		}		
		if (!parameters.getFagomraade().isEmpty()) {
			addFagomradeListe(parameters.getFagomraade());
		}
		if (!parameters.isVisFeilRegistrert()) {
			addSkjulFeilRegistrert();
		}
		if (!isNull(parameters.getTidligstInnsynDato())) {
			addJournalOpprettetOgJournalfoertFOM(parameters.getTidligstInnsynDato());
		}

		addJournalFom(parameters.getJournalFom());
		addJournalTom(parameters.getJournalTom());
		addJournalpostType(parameters.getJournalpostTypeCode());
		return criteria;
	}

	private void addSokPaSak(List<SakFagsystem> saksListe) {
		// Requires alias "saksrelasjon"
		Disjunction distinctSaksnummerFagsystemCombinations = Restrictions.disjunction();
		for (SakFagsystem fagsystemSak : saksListe) {
			distinctSaksnummerFagsystemCombinations.add(
					Restrictions.and(
							Restrictions.eq("saksrelasjon.sakId", fagsystemSak.getSakId()),
							Restrictions.eq("saksrelasjon.fagsystem", fagsystemSak.getFagsystem())
					)
			);
		}
		criteria.add(distinctSaksnummerFagsystemCombinations);
	}

	private void addFagomradeListe(List<FagomradeCode> fagomrader) {
		if (!fagomrader.isEmpty()) {
			criteria.add(Restrictions.in("fagomrade", fagomrader));
		}
	}

	private void addSkjulFagomraadeListe(List<FagomradeCode> skjulFagomraadeListe) {
		if (!skjulFagomraadeListe.isEmpty()) {
			criteria.add(Restrictions.or(
					Restrictions.not(Restrictions.in("fagomrade", skjulFagomraadeListe)),
					Restrictions.isNull("fagomrade")
			));
		}
	}
	

	private void addSkjulFeilRegistrert() {
		// Requires alias "saksrelasjon"
		criteria.add(
				Restrictions.or(
						Restrictions.eq("saksrelasjon.feilregistrert", Boolean.FALSE),
						Restrictions.isNull("saksrelasjon.feilregistrert")
				)
		);
	}

	private void addJournalpostType(JournalpostTypeCode journalpostType) {
		if (journalpostType != null) {
			criteria.add(Restrictions.eq("journalposttype", journalpostType));
		}
	}

	private void addJournalFom(Date journalFom) {
		if (!isNull(journalFom)) {
			journalFom = DateUtils.setHours(journalFom, 0);
			journalFom = DateUtils.setMinutes(journalFom, 0);
			journalFom = DateUtils.setSeconds(journalFom, 0);
			criteria.add(Restrictions.ge("changeStamp.createdDate", journalFom));
		}

	}

	private void addJournalOpprettetOgJournalfoertFOM(Date tidligstInnsynDato) {
		if (!isNull(tidligstInnsynDato)) {
			DateUtils.truncate(tidligstInnsynDato, Calendar.DAY_OF_MONTH);

			criteria.add(Restrictions.not(
					Restrictions.or(Restrictions.lt("changeStamp.createdDate", tidligstInnsynDato),
							Restrictions.or(Restrictions.lt("journalDato", tidligstInnsynDato),
									Restrictions.isNull("journalDato")))));
		}
	}

	private void addJournalTom(Date journalTom) {
		if (!isNull(journalTom)) {
			journalTom = DateUtils.setHours(journalTom, 23);
			journalTom = DateUtils.setMinutes(journalTom, 59);
			journalTom = DateUtils.setSeconds(journalTom, 59);
			criteria.add(Restrictions.le("changeStamp.createdDate", journalTom));
		}
	}

	private void addJournalStatusListe(List<JournalStatusCode> journalStatusList) {
		criteria.add(Restrictions.in("journalstatus", journalStatusList));
	}

}
