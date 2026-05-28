package no.nav.dokarkiv.internal.dokvaktmester;

import no.nav.dokarkiv.core.api.Sakstype;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
public class EndreFerdigstiltJournalpostValidator {
	private static final Pattern TEMA_PATTERN = Pattern.compile("^[A-Z]{3}$");

	void validate(EndreFerdigstiltJournalpostRequest request) {
		if (isBlank(request.brukerId()) && request.sak() == null && isBlank(request.tema())) {
			throw new InputValideringFeiletException("En av brukerId, sak og tema må være satt");
		}
		validateTema(request.tema());
		validateBrukerId(request.brukerId());
		validateSak(request.sak());
		validateBegrunnelseNokkel(request.begrunnelseNokkel());
	}

	private void validateTema(String tema) {
		if (tema == null) {
			return;
		}
		if (!TEMA_PATTERN.matcher(tema).matches()) {
			throw new InputValideringFeiletException("tema matcher ikke pattern");
		}
	}

	private static void validateBrukerId(String brukerId) {
		if (brukerId == null) {
			return;
		}
		if (!isNumeric(brukerId)) {
			throw new InputValideringFeiletException("brukerId er ikke numerisk");
		}

		if (brukerId.length() != 11) {
			throw new InputValideringFeiletException("brukerId må ha lengde 11");
		}
	}

	private static void validateSak(EndreSak sak) {
		if (sak == null) {
			return;
		}
		if (sak.sakstype() == Sakstype.FAGSAK) {
			if (isBlank(sak.fagsakId()) || sak.fagsaksystem() == null) {
				throw new InputValideringFeiletException("sak.fagsakId og sak.fagsaksystem må være satt hvis sak.sakstype er FAGSAK");
			}
		}
		if (sak.sakstype() == Sakstype.GENERELL_SAK) {
			if (!isBlank(sak.fagsakId()) || sak.fagsaksystem() != null) {
				throw new InputValideringFeiletException("sak.fagsakId og sak.fagsaksystem burde ikke være satt hvis sak.sakstype er GENERELL_SAK");
			}
		}
		if (sak.sakstype() == Sakstype.ARKIVSAK) {
			throw new InputValideringFeiletException("sak.sakstype FAGSAK støttes ikke");
		}
	}

	private void validateBegrunnelseNokkel(String begrunnelseNokkel) {
		if (isBlank(begrunnelseNokkel)) {
			throw new InputValideringFeiletException("begrunnelseNokkel må være satt");
		}
		if (begrunnelseNokkel.length() > 40) {
			throw new InputValideringFeiletException("begrunnelseNokkel må være kortere enn 40 tegn");
		}
	}
}
