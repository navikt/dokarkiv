package no.nav.dokarkiv.core.repository.projections;

import java.util.Set;


public interface MottattJournalpostProjectionMedBruker extends MottattJournalpostProjection {
	Set<MottattBrukerProjection> getBrukere();
}

