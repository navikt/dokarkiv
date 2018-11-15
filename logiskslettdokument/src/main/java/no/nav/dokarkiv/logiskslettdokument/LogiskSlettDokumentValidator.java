package no.nav.dokarkiv.logiskslettdokument;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.core.exceptions.JournalpostDokumentInfoRelasjonIkkeFunnetException;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.springframework.stereotype.Component;

@Component
public class LogiskSlettDokumentValidator {

	public void validerAtJournalpostDokumentInfoRelasjonerFinnes(
			JournalpostDokumentInfoRelasjon jpDokInfoRelasjoner,
			LogiskSlettDokumentRequestTo requestTo) {
		if (jpDokInfoRelasjoner == null) {
			throw new JournalpostDokumentInfoRelasjonIkkeFunnetException(
					String.format("Kan ikke finne noen relasjon mellom journalpost med journalpostId=%s og dokument med dokumentInfoId=%s",
							requestTo.getJournalpostId(),
							requestTo.getDokumentInfoId()));
		}
	}
}
