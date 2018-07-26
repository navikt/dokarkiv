package no.nav.dokarkiv.behandleinngaaendejournal.v1.tjoark067;

import no.nav.dokarkiv.behandleinngaaendejournal.v1.exceptions.FerdigstillingIkkeMuligException;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.InvalidArgumentException;

import java.util.Collection;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class FerdigstillJournalfoeringFieldValidator {

	public void validate(Journalpost journalpost) {
		Long journalpostId = journalpost.getJournalpostId();

		notNull(journalpostId, journalpost.getSaksrelasjon(), "Journalpost.Saksrelasjon");
		notNull(journalpostId, journalpost.getAvsenderMottakerId(), "Journalpost.avsenderMottakerId");
		notNull(journalpostId, journalpost.getBrukere(), "Journalpost.brukere");
		notEmpty(journalpostId, journalpost.getBrukere(), "Journalpost.brukere");
		try {
			journalpost.getSaksrelasjon().verifyMandatoryFieldsNotEndretAvNavn();
			journalpost.verifyMandatoryFieldsNotEndretAvNavn();
			validateBrukere(journalpost.getBrukere());
			validateRelasjoner(journalpost.getJournalpostDokumentInfoRelasjoner());
		} catch(InvalidArgumentException e) {
			throw new FerdigstillingIkkeMuligException(e.getMessage() + " journalpostId=" + journalpostId, e);
		}
	}

	private void validateBrukere(Collection<Bruker> brukere) {
		for(Bruker bruker : brukere) {
			bruker.verifyMandatoryFields();
		}
	}

	private void validateRelasjoner(Collection<JournalpostDokumentInfoRelasjon> relasjoner) {
		for(JournalpostDokumentInfoRelasjon relasjon : relasjoner) {
			DokumentInfo dokumentInfo = relasjon.getDokumentInfo();
			dokumentInfo.verifyMandatoryFieldsForInngaaendeJournal(relasjon.getJournalpost());
		}
	}

	private void notNull(Long journalpostId, Object object, String field) {
		if (object == null) {
			throw new FerdigstillingIkkeMuligException("field=" + field + " is not set. journalpostId=" + journalpostId);
		}
	}

	private void notEmpty(Long journalpostId, Collection<?> collection, String field) {
		if (collection.isEmpty()) {
			throw new FerdigstillingIkkeMuligException("field=" + field + " is empty. journalpostId=" + journalpostId);
		}
	}
}
