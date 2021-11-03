package no.nav.dokarkiv.journalfoerinngaaende.v1.support;

import no.nav.dok.tjenester.journalfoerinngaaende.PutLogiskVedleggRequest;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalfoerinngaaende.v1.util.Utils;
import org.springframework.stereotype.Component;

import static no.nav.dokarkiv.core.domain.entities.SkannetInnhold.VEDLEGG_INNHOLD_LENGTH;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class OppdaterLogiskVedleggValidator {
	public void validate(String journalpostId, String dokumentId, String logiskVedleggId, PutLogiskVedleggRequest request) {
		Utils.validateIds(journalpostId, dokumentId, logiskVedleggId);
		if (request.getTittel() != null && request.getTittel().length() > VEDLEGG_INNHOLD_LENGTH) {
			throw new InputValideringFeiletException("tittel kan ikke være lengre enn " + VEDLEGG_INNHOLD_LENGTH + " tegn.");
		}
	}
}
