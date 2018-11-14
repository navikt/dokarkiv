package no.nav.dokarkiv.logiskslettdokument.rjoark100;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	protected void validerAtDokumentSomSkalSlettesLogiskErKnyttetTilKunEnJournalpost(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonerFoundByDokumentInfoId,
			LogiskSlettDokumentRequestTo requestTo) {
		validerAtKunEnGyldigJpDokInfoRelasjonFinnes(jpDokInfoRelasjonerFoundByDokumentInfoId, requestTo.getDokumentInfoId());
		validerAtJournalpostIdOgDokumentInfoIdFraInputHarEnRelasjon(jpDokInfoRelasjonerFoundByDokumentInfoId.get(0)
				.getJournalpost()
				.getJournalpostId(), requestTo);
	}
}
