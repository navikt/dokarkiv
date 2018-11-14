package no.nav.dokarkiv.logiskslettdokument.rjoark101;

import no.nav.dokarkiv.core.domain.entities.JournalpostDokumentInfoRelasjon;
import no.nav.dokarkiv.logiskslettdokument.AbstractSlettDokumentValidator;
import no.nav.dokarkiv.logiskslettdokument.rjoark100.LogiskSlettDokumentRequestTo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AngreLogiskSlettDokumentValidator extends AbstractSlettDokumentValidator {

	protected void validerAngreLogiskSlettAvEttDokument(
			List<JournalpostDokumentInfoRelasjon> jpDokInfoRelasjonList,
			LogiskSlettDokumentRequestTo requestTo) {
		validerAtKunEnGyldigJpDokInfoRelasjonFinnes(jpDokInfoRelasjonList, requestTo.getDokumentInfoId());
	}
}
