package no.nav.dokarkiv.journalpost.v1.validators;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.Sakstype;

import java.util.Arrays;
import java.util.List;

public final class OppdaterJournalpostValidator {

	private static final String TEMA_PEN = "PEN";
	private static final String TEMA_UFO = "UFO";

	private static List<JournalStatusCode> restrictedJournalpostStatusCodes = Arrays.asList(JournalStatusCode.J, JournalStatusCode.FS, JournalStatusCode.FL, JournalStatusCode.E);

	private OppdaterJournalpostValidator() {
	}

	public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {

		if (restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
			checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType);
			checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType);
		} else if (request.getSak() != null) {
			validateSak(request.getSak(), request.getBruker(), request.getTema());
		}

		if (journalpostType != JournalpostTypeCode.U || !restrictedJournalpostStatusCodes.contains(journalpostStatus)) {
			checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType);
		}
	}


	private static void checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
		if (field != null) {
			throw new InputValideringFeiletException(String.format("%s kan ikke oppdateres for journalpost med journalpostStatus=%s og journalpostType=%s.", fieldName, journalpoststatus
					.name(), journalpostType.name()));
		}
	}

	private static void validateSak(Sak sak, Bruker bruker, String tema) {
		if (Sakstype.FAGSAK.equals(sak.getSakstype())) {
			validateFagsak(sak, bruker, tema);
		}

		if (Sakstype.GENERELL_SAK.equals(sak.getSakstype())) {
			validateGenerellSak(sak, bruker, tema);
		}

		if (Sakstype.ARKIVSAK.equals(sak.getSakstype()) || sak.getSakstype() == null) {
			validateArkivsak(sak);

		}
	}

	private static void validateFagsak(Sak sak, Bruker bruker, String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema må være satt dersom sakstype=FAGSAK");
		}
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=FAGSAK");
		}
		if (isBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId må være satt dersom sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem må være satt dersom sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=FAGSAK");
		}
	}

	private static void validateGenerellSak(Sak sak, Bruker bruker, String tema) {
		if (isBlank(tema)) {
			throw new InputValideringFeiletException("tema må være satt dersom sakstype=GENERELL_SAK");
		}
		if (TEMA_PEN.equals(tema) || TEMA_UFO.equals(tema)) {
			throw new InputValideringFeiletException("tema kan ikke være UFO eller PEN dersom sakstype=GENERELL_SAK");
		}
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem skal ikke være satt dersom sakstype=GENERELL_SAK");
		}
	}

	private static void validateArkivsak(Sak sak) {
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId skal ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem skal ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer må være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem må være satt dersom sakstype=GENERELL_SAK");
		}
		if(!isNumeric(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer skal være opprettet i GSAK/PSAK og må være et numerisk heltall.");
		}
	}

}
