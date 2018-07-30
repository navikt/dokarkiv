package no.nav.dokarkiv.journal.v3.tjoark058;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;

import java.util.Comparator;

/**
 * Sorts using journalpostDokumentInfoRelasjonId
 * 
 * @author Stig Strøm, Acando
 *
 */
public class DokumentInfoRelasjonComperator implements Comparator<JournalpostDokumentInfoRelasjon> {

		@Override
		public int compare(JournalpostDokumentInfoRelasjon relasjon1, JournalpostDokumentInfoRelasjon relasjon2) {
			return relasjon1.getJournalpostDokumentInfoRelasjonId().compareTo(relasjon2.getJournalpostDokumentInfoRelasjonId());
		}
	}