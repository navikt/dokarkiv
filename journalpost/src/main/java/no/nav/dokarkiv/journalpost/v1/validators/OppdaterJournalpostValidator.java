package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentInfo;
import no.nav.dokarkiv.journalpost.v1.api.OppdaterJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.Sak;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.BRUK_STANDARDREGLER;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_BRUKERS_ONSKE;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.SKJULES_FEILSENDT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MANUELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MASKINELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.I;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.ALTINN;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.EESSI;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO_CHAT;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
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
	private static final EnumSet<MottaksKanalCode> DIGITALE_KANALER = EnumSet.of(NAV_NO, NAV_NO_CHAT, ALTINN, EESSI);
	public static final Set<String> LOVLIGE_INNSYNSKODER = Set.of(
			BRUK_STANDARDREGLER.name(), VISES_MASKINELT_GODKJENT.name(), VISES_MANUELT_GODKJENT.name(),
			SKJULES_FEILSENDT.name(), /*SKJULES_BRUKERS_SIKKERHET.name(),*/ SKJULES_BRUKERS_ONSKE.name()
	);

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

		if (request.getTittel() != null) {
			feilmeldinger.add(validateJournalpostTittel(request.getTittel()));
		}

		if (isNotBlank(request.getBehandlingstema())) {
			feilmeldinger.add(validateBehandlingstema(request.getBehandlingstema()));
		}

		if (request.getDatoDokument() != null) {
			feilmeldinger.add(validateDatoKanIkkeVaereIFremtid(request.getDatoDokument(), "datoDokument"));
		}

		if (request.getOverstyrInnsynsregler() != null) {
			feilmeldinger.add(validateOverstyrInnsynsregler(request.getOverstyrInnsynsregler()));
		}

		if (request.getDokumenter() != null && !request.getDokumenter().isEmpty()) {
			request.getDokumenter().forEach(dokumentInfo -> feilmeldinger.add(validateDokument(dokumentInfo)));
		}

		if (DIGITALE_KANALER.contains(journalpost.getMottakskanal()) && request.getAvsenderMottaker() != null) {
			feilmeldinger.add(validateKanIkkeOppdatereAvsenderPaaDigitaltInnsendteDokumenter(request, journalpost));
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

		feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "datoRetur", journalpostStatus, journalpostType));

		if (INNGAAENDE_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "tema", journalpostStatus, journalpostType));
			if (request.getAvsenderMottaker() != null) {
				feilmeldinger.add(validateAvsenderMottakerInngaaende(request.getAvsenderMottaker()));
			}
			if (checkIfJournalChangeIsOld(journalpost)) {
				if (request.getAvsenderMottaker() != null) {
					feilmeldinger.add(checkIfFieldIsBeingUpdatedAfterLockDate(request.getAvsenderMottaker().getId(), "avsenderMottaker.id", journalpost.getJournalDato()));
					feilmeldinger.add(checkIfFieldIsBeingUpdatedAfterLockDate(request.getAvsenderMottaker().getNavn(), "avsenderMottaker.navn", journalpost.getJournalDato()));
				}
				feilmeldinger.add(checkIfFieldIsBeingUpdatedAfterLockDate(request.getTittel(), "tittel", journalpost.getJournalDato()));
			}
		} else if (request.getSak() != null) {
			feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
		}

		return feilmeldinger;
	}

	private static boolean checkIfJournalChangeIsOld(Journalpost journalpost) {
		return journalpost.getJournalDato() != null &&
				journalpost.getJournalDato().toInstant().atZone(ZoneId.of("Europe/Oslo")).toLocalDateTime().isBefore(LocalDateTime.now().minusYears(1));
	}

	private static String checkIfFieldIsBeingUpdatedAfterLockDate(Object field, String fieldName, Date journalDato) {
		if (field != null) {
			SimpleDateFormat datoFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			return format("%s kan ikke oppdateres da journalposten er journalført for over 1 år siden. journalDato=%s",
					fieldName,
					datoFormat.format(journalDato));
		}
		return null;
	}

	private static List<String> validateUtgaaende(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {
		List<String> feilmeldinger = new ArrayList<>();

		if (UTGAAENDE_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "tema", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTittel(), "tittel", journalpostStatus, journalpostType));
			if (request.getAvsenderMottaker() != null) {
				feilmeldinger.addAll(validateAvsenderMottaker(request.getAvsenderMottaker(), journalpostStatus, journalpostType));
			}
		} else {
			if (request.getSak() != null) {
				feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
			}
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "datoRetur", journalpostStatus, journalpostType));
		}

		return feilmeldinger;
	}

	private static List<String> validateNotat(OppdaterJournalpostRequest request, JournalStatusCode journalpostStatus, JournalpostTypeCode journalpostType) {
		List<String> feilmeldinger = new ArrayList<>();

		feilmeldinger.add(checkIfIllegalFieldIsSet(request.getDatoRetur(), "datoRetur", journalpostStatus, journalpostType));

		if (request.getAvsenderMottaker() != null) {
			feilmeldinger.addAll(validateAvsenderMottaker(request.getAvsenderMottaker(), journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getAvsenderMottaker().getLand(), "avsenderMottaker.land", journalpostStatus, journalpostType));
		}

		if (NOTAT_RESTRICTED_JOURNALSTATUS.contains(journalpostStatus)) {
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getBruker(), "bruker", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getSak(), "sak", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getJournalfoerendeEnhet(), "journalfoerendeEnhet", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTema(), "tema", journalpostStatus, journalpostType));
			feilmeldinger.add(checkIfIllegalFieldIsSet(request.getTittel(), "tittel", journalpostStatus, journalpostType));
		} else if (request.getSak() != null) {
			feilmeldinger.addAll(validateSak(request.getSak(), request.getBruker(), request.getTema()));
		}

		return feilmeldinger;
	}

	private static String validateJournalpostTittel(String tittel) {
		if (SKJULT_TITTEL.equals(tittel)) {
			return "tittel kan ikke oppdateres til " + SKJULT_TITTEL;
		}
		return null;
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
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getId(), "avsenderMottaker.id", journalpoststatus, journalpostType));
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getIdType(), "avsenderMottaker.idType", journalpoststatus, journalpostType));
		feilmeldinger.add(checkIfIllegalFieldIsSet(avsenderMottaker.getNavn(), "avsenderMottaker.navn", journalpoststatus, journalpostType));
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
			feilmeldinger.add("tema må være satt dersom sakstype=FAGSAK");
		}

		feilmeldinger.add(validateBruker(bruker, "FAGSAK"));

		if (isBlank(sak.getFagsakId())) {
			feilmeldinger.add("sak.fagsakId må være satt dersom sak.sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			feilmeldinger.add("sak.fagsaksystem må være satt dersom sak.sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("sak.arkivsaksnummer kan ikke være satt dersom sak.sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			feilmeldinger.add("sak.arkivsaksystem kan ikke være satt dersom sak.sakstype=FAGSAK");
		}
		if (FAGSAK == sak.getSakstype() && PP01 == sak.getFagsaksystem()) {
			if (!isNumeric(sak.getFagsakId())) {
				feilmeldinger.add("sak.fagsakId må være et heltall for saker opprettet i PSAK");
			}
		}
		return feilmeldinger;
	}

	private static List<String> validateGenerellSak(Sak sak, Bruker bruker, String tema) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isBlank(tema)) {
			feilmeldinger.add("tema må være satt dersom sak.sakstype=GENERELL_SAK");
		}

		feilmeldinger.add(validateBruker(bruker, "GENERELL_SAK"));

		if (isNotBlank(sak.getFagsakId())) {
			feilmeldinger.add("sak.fagsakId kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			feilmeldinger.add("sak.fagsaksystem kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("sak.arkivsaksnummer kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			feilmeldinger.add("sak.arkivsaksystem kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		return feilmeldinger;
	}

	private static List<String> validateArkivsak(Sak sak) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isNotBlank(sak.getFagsakId())) {
			feilmeldinger.add("sak.fagsakId kan ikke være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			feilmeldinger.add("sak.fagsaksystem kan ikke være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			feilmeldinger.add("sak.arkivsaksnummer må være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() == null) {
			feilmeldinger.add("sak.arkivsaksystem må være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (!isNumeric(sak.getArkivsaksnummer())) {
			feilmeldinger.add("sak.arkivsaksnummer må være et heltall, og saken må være opprettet i GSAK/PSAK");
		}
		return feilmeldinger;
	}

	private static String validateBehandlingstema(String behandlingstema) {
		if (!BEHANDLINGSTEMA_PATTERN.matcher(behandlingstema).matches()) {
			return format("behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=%s", behandlingstema);
		}
		return null;
	}

	private static String validateBruker(Bruker bruker, String sakstype) {
		if (bruker == null) {
			return format("bruker må være satt dersom sak.sakstype=%s", sakstype);
		} else if (isBlank(bruker.getId()) || bruker.getIdType() == null) {
			return format("bruker.id og bruker.idType må være satt dersom sak.sakstype=%s. Mottatt id=%s idType=%s", sakstype, masker(bruker.getId()), bruker.getIdType());
		} else if (!isNumeric(bruker.getId())) {
			return format("bruker.id kan kun bestå av tall. Mottatt id=%s", bruker.getId());
		} else if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			return format("bruker.id må være 11 siffer dersom bruker.idType=FNR. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			return format("bruker.id må være 9 siffer dersom bruker.idType=ORGNR. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			return format("bruker.id må være 11 siffer dersom bruker.idType=AKTOERID. Mottatt id=%s har lengde=%s", masker(bruker.getId()), bruker.getId().length());
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

	private static String validateOverstyrInnsynsregler(String overstyrInnsynsregler) {
		if (!LOVLIGE_INNSYNSKODER.contains(overstyrInnsynsregler)) {
			return format("overstyrInnsynsregler må være en av følgende verdier: null eller %s. Mottatt: %s", LOVLIGE_INNSYNSKODER, overstyrInnsynsregler);
		}
		return null;
	}

	private static String validateDokument(DokumentInfo dokumentInfo) {
		if (dokumentInfo != null) {
			if (SKJULT_TITTEL.equals(dokumentInfo.getTittel())) {
				return "dokumenter.tittel kan ikke oppdateres til %s for dokument med dokumentInfoId=%s".formatted(SKJULT_TITTEL, dokumentInfo.getDokumentInfoId());
			}
		}
		return null;
	}

	private static String validateKanIkkeOppdatereAvsenderPaaDigitaltInnsendteDokumenter(OppdaterJournalpostRequest request, Journalpost journalpost) {
		boolean nameChanged = isChangeAndNotFromEmpty(request.getAvsenderMottaker().getNavn(), journalpost.getAvsenderMottaker());
		boolean idChanged = isChangeAndNotFromEmpty(request.getAvsenderMottaker().getId(), journalpost.getAvsenderMottakerId());
		boolean idTypeChanged = request.getAvsenderMottaker().getIdType() != null &&
				journalpost.getAvsenderMottakerIdType() != null &&
				!request.getAvsenderMottaker().getIdType().name().equals(journalpost.getAvsenderMottakerIdType().name());
		int changeCounter = (nameChanged ? 1 : 0) + (idChanged ? 1 : 0) + (idTypeChanged ? 1 : 0);

		if (nameChanged || idChanged || idTypeChanged) {
			String changes = "";
			if (nameChanged) {
				changes += "navn" + addCommaAndSpaceOrNothing(changeCounter);
				if (!addCommaAndSpaceOrNothing(changeCounter).isEmpty()) {
					changeCounter--;
				}
			}
			if (idChanged) {
				changes += "id" + addCommaAndSpaceOrNothing(changeCounter);
			}
			if (idTypeChanged) {
				changes += "idType ";
			}
			return "avsenderMottaker på digitalt innsendt journalpost kan ikke endres. Følgende felter ble forsøkt endret: %s".formatted(changes);
		}
		return null;
	}

	private static String addCommaAndSpaceOrNothing(int changeCounter) {
		if (changeCounter > 2)
			return ", ";
		else if (changeCounter > 1)
			return " og ";
		else if (changeCounter > 0)
			return " ";
		else
			return "";
	}

	private static boolean isChangeAndNotFromEmpty(String newValueFromUpdateRequest, String existingValue) {
		return newValueFromUpdateRequest != null &&
				isNotBlank(existingValue) &&
				!newValueFromUpdateRequest.equals(existingValue);
	}
}
