package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.BrukerIdType;
import no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.knyttTilAnnenSak.KnyttTilAnnenSakRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.String.format;
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

	public void validate(KnyttTilAnnenSakRequest request, long kildeJournalpostId) {
		try {
			validateSakstype(request);
			validateBruker(request.getBruker());
			validateTema(request.getTema());
			validateJournalfoerendeEnhet(request.getJournalfoerendeEnhet());
			validateDokumenter(request.getDokumenter());
		} catch (InputValideringFeiletException e) {
			throw new InputValideringFeiletException(format("Validering feilet for journalpostId=%s. Feilmelding=%s", kildeJournalpostId, e.getMessage()));
		}
	}

	private void validateSakstype(KnyttTilAnnenSakRequest request) {
		if (isBlank(request.getSakstype())) {
			throw new InputValideringFeiletException("sakstype kan ikke være null eller tom");
		}

		if (request.getSakstype().equals(SAKSTYPE_FAGSAK)) {
			if (isBlank(request.getFagsakId())) {
				throw new InputValideringFeiletException("fagsakId kan ikke være null eller tom dersom sakstype=FAGSAK");
			}
			if (isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("fagsaksystem kan ikke være null eller tom dersom sakstype=FAGSAK");
			}
		} else if (request.getSakstype().equals(SAKSTYPE_GENERELL)) {
			if (!isBlank(request.getFagsakId()) || !isBlank(request.getFagsaksystem())) {
				throw new InputValideringFeiletException("fagsakId og fagsaksystem skal ikke oppgis dersom sakstype=GENERELL_SAK");
			}
		} else {
			throw new InputValideringFeiletException(format("Ugyldig sakstype: %s", request.getSakstype()));
		}
	}

	private void validateBruker(Bruker bruker) {
		BrukerIdType idtype = bruker.getIdType();

		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("bruker.id er ikke et tall.");
		}
		if (idtype == null) {
			throw new InputValideringFeiletException("bruker.idType kan ikke være null");
		}
		if (idtype.equals(FNR)) {
			if (bruker.getId().length() != FNR_LENGTH) {
				throw new InputValideringFeiletException("bruker.id må ha 11 siffer dersom bruker.idType=FNR.");
			}
		} else if (idtype.equals(ORGNR)) {
			if (bruker.getId().length() != ORGNR_LENGTH) {
				throw new InputValideringFeiletException("bruker.id må ha 9 siffer dersom bruker.idType=ORGNR.");
			}
		} else if (!idtype.equals(AKTOERID)) {
			throw new InputValideringFeiletException(format("bruker.idType har ukjent verdi: %s.", idtype));
		}
	}

	private void validateTema(String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema kan ikke være null eller tom");
		}
		if (!StringUtils.isAlpha(tema) || tema.length() != 3) {
			throw new InputValideringFeiletException("tema må ha 3 tegn");
		}
	}

	private void validateJournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (isBlank(journalfoerendeEnhet)) {
			throw new InputValideringFeiletException("journalfoerendeEnhet kan ikke være null eller tom");
		}
		if (journalfoerendeEnhet.length() != JOURNALFOERENDE_ENHET_LENGTH) {
			throw new InputValideringFeiletException("journalfoerendeEnhet må ha 4 siffer");
		}
	}

	private void validateDokumenter(List<Dokument> dokumenter) {
		if (dokumenter == null) {
			return;
		}

		if (dokumenter.isEmpty()) {
			throw new InputValideringFeiletException("dokumenter[] kan ikke være tom");
		}

		validateUniqueDokumenter(dokumenter);
		validateNumericDokumenter(dokumenter);
	}

	private static void validateNumericDokumenter(List<Dokument> dokumenter) {
		var ugyldigeDokumentInfoIDer = dokumenter.stream()
				.map(Dokument::dokumentInfoId)
				.filter(d -> !isNumeric(d))
				.toList();

		if (!ugyldigeDokumentInfoIDer.isEmpty()) {
			throw new InputValideringFeiletException(
					"dokumenter[].dokumentInfoId kan ikke inneholde ikke-numeriske verdier. Mottok følgende dokumentInfoId(er) med ikke-numeriske verdier: %s"
					.formatted(ugyldigeDokumentInfoIDer));
		}
	}

	private static void validateUniqueDokumenter(List<Dokument> dokumenter) {
		var duplikateDokumentInfoIDer = dokumenter.stream()
				.collect(Collectors.groupingBy(Dokument::dokumentInfoId, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.toList();

		if (!duplikateDokumentInfoIDer.isEmpty()) {
			throw new InputValideringFeiletException(
					"dokumenter[] kan ikke inneholde duplikate dokumentInfoId-er. Mottok følgende duplikate dokumentInfoId-er: %s"
					.formatted(duplikateDokumentInfoIDer));
		}
	}
}
