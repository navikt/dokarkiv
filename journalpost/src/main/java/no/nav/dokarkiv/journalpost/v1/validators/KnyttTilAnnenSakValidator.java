package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidNavConsumerIdFunctionalException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author Tore Sletten, NAV.
 */

@Component
public class KnyttTilAnnenSakValidator {

	private static final String IDTYPE_FNR = "FNR";
	private static final String IDTYPE_ORGNR = "ORGNR";
	private static final String IDTYPE_AKTOERID = "AKTOERID";
	private static final int FNR_LENGTH = 11;
	private static final int ORGNR_LENGTH = 9;
	private static final String SAKSTYPE_FAGSAK = "FAGSAK";
	private static final String SAKSTYPE_GENERELL = "GENERELL_SAK";
	private static final int JOURNALFOERENDE_ENHET_LENGTH = 4;

	public void validateKnyttTilAnnenSakRequest(KnyttTilAnnenSakRequest request, String kildeJournalpostId, String navConsumerId) {
		try {
			if (navConsumerId == null || navConsumerId.equals("")) {
				throw new InvalidNavConsumerIdFunctionalException("Nav-Consumer-Id kan ikke være null eller tom");
			}
			if (!StringUtils.isNumeric(kildeJournalpostId)){
				throw new InputValideringFeiletException("kildeJournalpostId er ikke et tall.");
			}
			validateSakstype(request);
			validateBruker(request.getBruker());
			validateTema(request.getTema());
			validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet());
		} catch (InputValideringFeiletException e) {
			throw new InputValideringFeiletException(String.format("Validering feilet for journalpostId=%s. Feilmelding=%s", kildeJournalpostId, e
					.getMessage()));
		}
	}

	private void validateSakstype(KnyttTilAnnenSakRequest request) {
		if (StringUtils.isBlank(request.getSakstype())) {
			throw new InputValideringFeiletException("Element sakstype kan ikke være null eller tom");
		}

		if (request.getSakstype().equals(SAKSTYPE_FAGSAK)) {
			if (StringUtils.isBlank(request.getFagsakId())) {
				throw new InputValideringFeiletException("FagsakId er påkrevet for sakstype FAGSAK");
			}
			if (StringUtils.isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("Fagsaksystem er påkrevet for sakstype FAGSAK");
			}
		} else if (request.getSakstype().equals(SAKSTYPE_GENERELL)) {
			if (!StringUtils.isBlank(request.getFagsakId()) || !StringUtils.isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("FagsakId og fagsaksystem skal ikke oppgis for sakstype GENERELL_SAK");
			}
		} else {
			throw new InputValideringFeiletException(String.format("Ugyldig sakstype: %s", request.getSakstype()));
		}
	}

	private void validateBruker(Bruker bruker) {
		String idtype = bruker.getIdType().name();
		if (!StringUtils.isNumeric(bruker.getId())){
			throw new InputValideringFeiletException("Id er ikke et tall.");
		}
		if (idtype.equals(IDTYPE_FNR)){
			if (bruker.getId().length() != FNR_LENGTH) {
				throw new InputValideringFeiletException("Fnr må ha 11 siffer.");
			}
		} else if (idtype.equals(IDTYPE_ORGNR)){
			if (bruker.getId().length() != ORGNR_LENGTH) {
				throw new InputValideringFeiletException("Orgnr må ha 9 siffer.");
			}
		} else if (!idtype.equals(IDTYPE_AKTOERID)){
			throw new InputValideringFeiletException(String.format("Ukjent idType for bruker: %s.", idtype));
		}
	}

	private void validateTema(String tema) {
		if (StringUtils.isBlank(tema)){
			throw new InputValideringFeiletException("Element tema kan ikke være null eller tom");
		}
		if (!StringUtils.isAlpha(tema) || tema.length() != 3) {
			throw new InputValideringFeiletException("Tema må ha 3 tegn");
		}
	}

	private void validateJournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (StringUtils.isBlank(journalfoerendeEnhet)) {
			throw new InputValideringFeiletException("Element journalfoerendeEnhet kan ikke være null eller tom");
		}
		if (journalfoerendeEnhet.length() != JOURNALFOERENDE_ENHET_LENGTH){
			throw new InputValideringFeiletException("JournalfoerendeEnhet må ha 4 siffer");
		}
	}
}
