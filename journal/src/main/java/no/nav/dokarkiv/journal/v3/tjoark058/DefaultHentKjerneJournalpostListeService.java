package no.nav.dokarkiv.journal.v3.tjoark058;

import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.repository.JoarkRepository;

import javax.inject.Inject;
import java.util.List;

/**
 * Service for TJOARK058
 *
 * @author Stig Strøm, Acando
 */
public class DefaultHentKjerneJournalpostListeService implements HentKjerneJournalpostListeService {

	@Inject
	private JoarkRepository joarkRepository;

//	@Inject
//	@Qualifier("rep.joark.hibernateTemplate")
//	private HibernateTemplate hibernateTemplate; FIXME

	@Override
	public HentKjerneJournalpostListeResponseTo hentKjerneJournalpostListe(
			HentKjerneJournalpostListeRequestTo requestTo) {

//		HentMinJPListeParameters params = new HentMinJPListeParameters(); DIXME
//		params.setSaksListe(requestTo.getSaksListe());
//		params.setJournalFom(requestTo.getJournalFom());
//		params.setJournalTom(requestTo.getJournalTom());
//		params.setVisFeilRegistrert(true);
//		params.setJournalpostTypeCode(requestTo.getJournalpostType());
//		params.getFagomraade()
//				.addAll(requestTo.getTema() == null ? new ArrayList<FagomradeCode>() : requestTo.getTema());
//		params.setMaxResults(requestTo.getResultatSettStoerrelse());
//		params.setPageNr(requestTo.getResultatSettNr());
//
//		List<Journalpost> journalposts = joarkRepository.findJournalpostListe(params);
//		long totalNrJournalposts = joarkRepository.findTotalNumberOfJournalposts(params);
//		evictJournalposts(journalposts);

//		return HentKjerneJournalpostListeResponseTo.builder().journalpostListe(journalposts)
//				.sisteIntervall(isSisteIntervall(journalposts, totalNrJournalposts, requestTo)).build();
		return null;
	}

	private void evictJournalposts(List<Journalpost> journalposts) {
//		Session currentSession = hibernateTemplate.getSessionFactory().getCurrentSession();
//		if (currentSession.contains(journalposts)) {
//			currentSession.evict(journalposts);
//		} FIXME
	}

	private boolean isSisteIntervall(List<Journalpost> journalpostListe, long totalNrJournalposts,
									 HentKjerneJournalpostListeRequestTo requestTo) {
		if (journalpostListe == null || journalpostListe.isEmpty() || requestTo.getResultatSettStoerrelse() == 0) {
			return true;
		}
		
		if (requestTo.getResultatSettNr() == 0 && requestTo.getResultatSettStoerrelse() >= totalNrJournalposts) {
			return true;
		}
		
		int lastPageNumber = (int) ((totalNrJournalposts + requestTo.getResultatSettStoerrelse() - 1)
                        / requestTo.getResultatSettStoerrelse());
		return lastPageNumber == requestTo.getResultatSettNr() + 1;
	}
}
