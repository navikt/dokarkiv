package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.KnyttTilAnnenSakRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Component
public class KnyttTilAnnenSakValidator {

	private static final int FNR_LENGTH = 11;
	private static final int ORGNR_LENGTH = 9;
	private static final String SAKSTYPE_FAGSAK = "FAGSAK";
	private static final String SAKSTYPE_GENERELL = "GENERELL_SAK";
	private static final int JOURNALFOERENDE_ENHET_LENGTH = 4;

	public void validate(KnyttTilAnnenSakRequest request, String kildeJournalpostId) {
		try {
			if (!isNumeric(kildeJournalpostId)){
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
		if (isBlank(request.getSakstype())) {
			throw new InputValideringFeiletException("Sakstype kan ikke være null eller tom");
		}

		if (request.getSakstype().equals(SAKSTYPE_FAGSAK)) {
			if (isBlank(request.getFagsakId())) {
				throw new InputValideringFeiletException("FagsakId kan ikke være null eller tom for sakstype FAGSAK");
			}
			if (isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("Fagsaksystem kan ikke være null eller tom sakstype FAGSAK");
			}
		} else if (request.getSakstype().equals(SAKSTYPE_GENERELL)) {
			if (!isBlank(request.getFagsakId()) || !isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("FagsakId og fagsaksystem skal ikke oppgis for sakstype GENERELL_SAK");
			}
		} else {
			throw new InputValideringFeiletException(String.format("Ugyldig sakstype: %s", request.getSakstype()));
		}
	}

	private void validateBruker(Bruker bruker) {
		BrukerIdType idtype = bruker.getIdType();

		if (!isNumeric(bruker.getId())){
			throw new InputValideringFeiletException("Id er ikke et tall.");
		}
		if(idtype == null){
			throw new InputValideringFeiletException("idType kan ikke være null eller tom");
		}
		if (idtype.equals(FNR)){
			if (bruker.getId().length() != FNR_LENGTH) {
				throw new InputValideringFeiletException("Fnr må ha 11 siffer.");
			}
		} else if (idtype.equals(ORGNR)){
			if (bruker.getId().length() != ORGNR_LENGTH) {
				throw new InputValideringFeiletException("Orgnr må ha 9 siffer.");
			}
		} else if (!idtype.equals(AKTOERID)){
			throw new InputValideringFeiletException(String.format("Ukjent idType for bruker: %s.", idtype));
		}
	}

	private void validateTema(String tema) {
		if (isBlank(tema)){
			throw new InputValideringFeiletException("Tema kan ikke være null eller tom");
		}
		if (!StringUtils.isAlpha(tema) || tema.length() != 3) {
			throw new InputValideringFeiletException("Tema må ha 3 tegn");
		}
	}

	private void validateJournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (isBlank(journalfoerendeEnhet)) {
			throw new InputValideringFeiletException("JournalfoerendeEnhet kan ikke være null eller tom");
		}
		if (journalfoerendeEnhet.length() != JOURNALFOERENDE_ENHET_LENGTH){
			throw new InputValideringFeiletException("JournalfoerendeEnhet må ha 4 siffer");
		}
	}
}
