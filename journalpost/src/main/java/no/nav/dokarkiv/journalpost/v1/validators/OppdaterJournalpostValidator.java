package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNumeric;

public final class OppdaterJournalpostValidator {

	private static final int FNR_LENGTH = 11;
	private static final int AKTOERID_LENGTH = 13;
	private static final int ORGNR_LENGTH = 9;

	private static final EnumSet<JournalStatusCode> INNGAAENDE_RESTRICTED_JOURNALSTATUS = EnumSet.of(J);
	private static final EnumSet<JournalStatusCode> UTGAAENDE_RESTRICTED_JOURNALSTATUS = EnumSet.of(FS, FL, E);
	private static final EnumSet<JournalStatusCode> NOTAT_RESTRICTED_JOURNALSTATUS = EnumSet.of(FS, FL, E);

	private static final Pattern BEHANDLINGSTEMA_PATTERN = Pattern.compile("ab\\d{4}");

	private OppdaterJournalpostValidator() {
	}

	public static void validateOppdaterteFelt(OppdaterJournalpostRequest request, Journalpost journalpost) {
		JournalStatusCode journalpostStatus = journalpost.getJournalstatus();
		JournalpostTypeCode journalpostType = journalpost.getJournalposttype();
		List<String> feilmeldinger = new ArrayList<>();

		if (I.equals(journalpostType)) {
			feilmeldinger.addAll(validateInngaaende(request, journalpost));
		} else if (U.equals(journalpostType)) {
			feilmeldinger.addAll(validateUtgaaende(request, journalpostStatus, journalpostType));
		} else if (N.equals(journalpostType)) {
			feilmeldinger.addAll(validateNotat(request, journalpostStatus, journalpostType));
		}
		if (isNotBlank(request.getBehandlingstema())) {
			feilmeldinger.add(validateBehandlingstema(request.getBehandlingstema()));
		}
		if (request.getDatoDokument() != null) {
			feilmeldinger.add(validateDatoKanIkkeVaereIFremtid(request.getDatoDokument(), "datoDokument"));
		}

		String feilmelding = feilmeldinger.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.joining(", "));

		if (isNotEmpty(feilmelding)) {
			throw new InputValideringFeiletException(feilmelding);
		}
	}

	private static List<String> validateInngaaende(OppdaterJournalpostRequest request, Journalpost journalpost) {
		JournalStatusCode journalpostStatus = journalpost.getJournalstatus();
		JournalpostTypeCode journalpostType = journalpost.getJournalposttype();

		List<String> feilmeldinger = new ArrayList<>();

		feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType));

		if (INNGAAENDE_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType));
			if (request.getAvsenderMottaker() != null) {
				feilmeldinger.add(validateAvsenderMottakerInngaaende(request.getAvsenderMottaker()));
			}
			if (journalpost.getJournalDato() != null &&
					journalpost.getJournalDato().toInstant().atZone(ZoneId.of("Europe/Oslo")).toLocalDateTime().isBefore(LocalDateTime.now().minusYears(1))) {
				if (request.getAvsenderMottaker() != null) {
					feilmeldinger.add(checkIfTooOldFieldIsSet(request.getAvsenderMottaker().getId(), "AvsenderMottaker.Id", journalpost.getJournalDato()));
					feilmeldinger.add(checkIfTooOldFieldIsSet(request.getAvsenderMottaker().getNavn(), "AvsenderMottaker.Navn", journalpost.getJournalDato()));
				}
				feilmeldinger.add(checkIfTooOldFieldIsSet(request.getTittel(), "Tittel", journalpost.getJournalDato()));
			}
		} else if (request.getSak() != null) {
			feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
		}

		return feilmeldinger;
	}

	private static String checkIfTooOldFieldIsSet(Object field, String fieldName, Date journalDato) {
		if (field != null) {
			return format("%s kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s",
					fieldName,
					journalDato);
		}
		return null;
	}


	private static List<String> validateUtgaaende(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {
		List<String> feilmeldinger = new ArrayList<>();

		if (UTGAAENDE_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTittel(), "Tittel", journalpostStatus, journalpostType));
			if (request.getAvsenderMottaker() != null) {
				feilmeldinger.addAll(validateAvsenderMottaker(request.getAvsenderMottaker(), journalpostStatus, journalpostType));
			}
		} else {
			if (request.getSak() != null) {
				feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
			}
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType));
		}

		return feilmeldinger;
	}

	private static List<String> validateNotat(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {
		List<String> feilmeldinger = new ArrayList<>();

		feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "DatoRetur", journalpostStatus, journalpostType));

		if (request.getAvsenderMottaker() != null) {
			feilmeldinger.addAll(validateAvsenderMottaker(request.getAvsenderMottaker(), journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getLand(), "AvsendeMottakerLand", journalpostStatus, journalpostType));
		}

		if (NOTAT_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "Bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "Sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "JournalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "Tema", journalpostStatus, journalpostType));
		} else if (request.getSak() != null) {
			feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
		}

		return feilmeldinger;
	}

	private static String validateAvsenderMottakerInngaaende(AvsenderMottaker avsenderMottaker) {
		if (isEmpty(avsenderMottaker.getId()) && avsenderMottaker.getIdType() != null) {
			return format("Oppdatering av avsenderMottaker.idType for journalpost med journalposttype=INNGAAENDE krever at feltet avsenderMottaker.id er satt. Mottatt id=%s idType=%s",
					avsenderMottaker.getId(),
					avsenderMottaker.getIdType());
		} else if (isNotEmpty(avsenderMottaker.getId()) && avsenderMottaker.getIdType() == null) {
			return format("Oppdatering av avsenderMottaker.id for journalpost med journalposttype=INNGAAENDE krever at feltet avsenderMottaker.idType er satt. Mottatt id=%s idType=null",
					masker(avsenderMottaker.getId()));
		}
		return null;
	}

	private static List<String> validateAvsenderMottaker(AvsenderMottaker avsenderMottaker, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
		List<String> feilmeldinger = new ArrayList<>();
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getId(), "AvsendeMottakerId", journalpoststatus, journalpostType));
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getIdType(), "AvsendeMottakerIdType", journalpoststatus, journalpostType));
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getNavn(), "AvsendeMottakerNavn", journalpoststatus, journalpostType));
		return feilmeldinger;
	}

	private static String checkIfIllegalFieldIsSet(Object field, String fieldName, JournalStatusCode journalpoststatus, JournalpostTypeCode journalpostType) {
		if (field != null) {
			return format("%s kan ikke oppdateres for journalpost med journalpoststatus=%s og journalposttype=%s",
					fieldName,
					journalpoststatus.name(),
					journalpostType.name());
		}
		return null;
	}

	private static List<String> validateSak(Sak sak, Bruker bruker, String tema) {
		List<String> feilmeldinger = new ArrayList<>();

		if (FAGSAK.equals(sak.getSakstype())) {
			feilmeldinger.addAll(validateFagsak(sak, bruker, tema));
		} else if (GENERELL_SAK.equals(sak.getSakstype())) {
			feilmeldinger.addAll(validateGenerellSak(sak, bruker, tema));
		} else if (ARKIVSAK.equals(sak.getSakstype()) || sak.getSakstype() == null) {
			feilmeldinger.addAll(validateArkivsak(sak));
		}

		return feilmeldinger;
	}

	private static List<String> validateFagsak(Sak sak, Bruker bruker, String tema) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isBlank(tema)) {
			feilmeldinger.add("Tema må være satt dersom sakstype=FAGSAK");
		}

		feilmeldinger.add(validateBruker(bruker, "FAKSAK"));

		if (isBlank(sak.getFagsakId())) {
			feilmeldinger.add("Sak.fagsakId må være satt dersom sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			feilmeldinger.add("Sak.fagsaksystem må være satt dersom sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("Sak.arkivsaksnummer kan ikke være satt dersom sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			feilmeldinger.add("Sak.arkivsaksystem kan ikke være satt dersom sakstype=FAGSAK");
		}
		if (FAGSAK == sak.getSakstype() && PP01 == sak.getFagsaksystem()) {
			if (!isNumeric(sak.getFagsakId())) {
				feilmeldinger.add("Sak.fagsakId må være et heltall for saker opprettet i PSAK");
			}
		}
		return feilmeldinger;
	}

	private static List<String> validateGenerellSak(Sak sak, Bruker bruker, String tema) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isBlank(tema)) {
			feilmeldinger.add("Tema må være satt dersom sakstype=GENERELL_SAK");
		}

		feilmeldinger.add(validateBruker(bruker, "GENERELL_SAK"));

		if (isNotBlank(sak.getFagsakId())) {
			feilmeldinger.add("Sak.fagsakId kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			feilmeldinger.add("Sak.fagsaksystem kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("Sak.arkivsaksnummer kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			feilmeldinger.add("Sak.arkivsaksystem kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		return feilmeldinger;
	}

	private static List<String> validateArkivsak(Sak sak) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isNotBlank(sak.getFagsakId())) {
			feilmeldinger.add("Sak.fagsakId kan ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			feilmeldinger.add("Sak.fagsaksystem kan ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("Sak.arkivsaksnummer må være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() == null) {
			feilmeldinger.add("Sak.arkivsaksystem må være satt dersom sakstype=GENERELL_SAK");
		}
		if (!isNumeric(sak.getArkivsaksnummer())) {
			feilmeldinger.add("Sak.arkivsaksnummer må være et heltall, og saken må være opprettet i GSAK/PSAK");
		}
		return feilmeldinger;
	}

	private static String validateBehandlingstema(String behandlingstema) {
		if (!BEHANDLINGSTEMA_PATTERN.matcher(behandlingstema).matches()) {
			return format("Behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=%s", behandlingstema);
		}
		return null;
	}

	private static String validateBruker(Bruker bruker, String sakstype) {
		if (bruker == null) {
			return format("Bruker må være satt dersom sakstype=%s", sakstype);
		} else if (isBlank(bruker.getId()) || bruker.getIdType() == null) {
			return format("Bruker.id og Bruker.idType må være satt dersom sakstype=%s. Mottatt id=%s idType=%s", sakstype, masker(bruker.getId()), bruker.getIdType());
		} else if (!isNumeric(bruker.getId())) {
			return format("Bruker.id kan kun bestå av tall. Mottatt id=%s", bruker.getId());
		} else if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			return format("Bruker.id må være 11 siffer for Bruker.idType=FNR. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			return format("Bruker.id må være 9 siffer for Bruker.idType=ORGNR. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			return format("Bruker.id må være 11 siffer for Bruker.idType=AKTOERID. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
		}
		return null;
	}

	private static String validateDatoKanIkkeVaereIFremtid(LocalDateTime dato, String feltNavn) {
		LocalDateTime naaTid = LocalDateTime.now().plusSeconds(3);
		if (naaTid.isBefore(dato)) {
			return format("%s er ugyldig verdi for %s. Feltet kan ikke settes frem i tid. Nåtid er %s", dato, feltNavn, naaTid);
		}
		return null;
	}

	private static String masker(String s) {
		if (s == null) {
			return null;
		}
		return s.substring(0, s.length() / 2) + "*".repeat(s.length() / 2);
	}
}
