package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.SER;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MANUELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MASKINELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO_UINNLOGGET;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.JournalpostType.INNGAAENDE;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.SKJULT_TITTEL;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.validateEksternReferanseId;
import static no.nav.dokarkiv.journalpost.v1.validators.DokumentValidator.validateDokument;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

@Slf4j
public class OpprettJournalpostRequestValidator {

	private static final int FNR_LENGTH = 11;
	private static final int AKTOERID_LENGTH = 13;
	private static final int ORGNR_LENGTH = 9;
	public static final String MASKINELL_JOURNALFOERENDE_ENHET = "9999";
	public static final Set<String> LOVLIGE_INNSYNSKODER = Set.of(VISES_MASKINELT_GODKJENT.name(), VISES_MANUELT_GODKJENT.name());

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";
	private static final Pattern JOURNALFOERENDE_ENHET_PATTERN = Pattern.compile("^\\d{4}$");

	public void validateRequest(OpprettJournalpostRequest request, String journalpostFerdigstilt) {

		validateTema(request.getTema());
		validateEksternReferanseId(request.getEksternReferanseId());

		if (request.getAvsenderMottaker() != null) {
			validateAvsenderMottaker(request.getAvsenderMottaker());
		}
		if (request.getBruker() != null) {
			validateBruker(request.getBruker());
		}
		if (isNotBlank(request.getBehandlingstema())) {
			validateBehandlingstema(request.getBehandlingstema());
		}
		if (request.getTittel() != null) {
			validateJournalpostTittel(request.getTittel());
		}
		validateKanal(request);
		if (request.getSak() != null) {
			validateSak(request.getSak(), request.getBruker());
		}
		if (isNotBlank(request.getJournalfoerendeEnhet())) {
			validateJournalfoerendeEnhet(journalpostFerdigstilt, request.getJournalfoerendeEnhet(), request.getJournalposttype());
		}
		if (request.getDatoDokument() != null) {
			validateDato(request.getDatoDokument(), "datoDokument");
		}
		if (request.getDatoMottatt() != null) {
			softValidateDato(request.getDatoMottatt(), "datoMottatt");
		}

		List<Dokument> dokumenter = request.getDokumenter();
		if (!dokumenter.isEmpty()) {
			IntStream.range(0, dokumenter.size())
					.forEach(dokumentIdx -> {
						Dokument dokument = dokumenter.get(dokumentIdx);
						validateDokument(dokumentIdx, dokument);
					});
		} else {
			throw new InputValideringFeiletException("Kan ikke opprette journalpost uten dokumenter.");
		}

		validatejournalfoerendeEnhet(request.getJournalfoerendeEnhet());

		if (request.getOverstyrInnsynsregler() != null) {
			validateOverstyrInnsynsregler(request.getOverstyrInnsynsregler());
		}
	}

	private void validateAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
		if (isNotBlank(avsenderMottaker.getId()) && avsenderMottaker.getIdType() == null) {
			throw new InputValideringFeiletException("avsenderMottaker.idType må være satt dersom avsenderMottaker.id er satt.");
		}
		if (avsenderMottaker.getIdType() != null && isBlank(avsenderMottaker.getId())) {
			throw new InputValideringFeiletException("avsenderMottaker.id må være satt dersom avsenderMottaker.idType er satt.");
		}
		if (avsenderMottaker.getIdType() != null) {
			switch (avsenderMottaker.getIdType()) {
				case FNR:
					if (!avsenderMottaker.getId().matches("^\\d{11}$")) {
						throw new InputValideringFeiletException("avsenderMottaker.id må være 11 siffer dersom avsenderMottaker.idType=FNR.");
					}
					break;
				case ORGNR:
					if (!avsenderMottaker.getId().matches("^\\d{9}$")) {
						throw new InputValideringFeiletException("avsenderMottaker.id må være 9 siffer dersom avsenderMottaker.idType=ORGNR.");
					}
					break;
				case HPRNR:
					if (!avsenderMottaker.getId().matches("^\\d{7,9}$")) {
						throw new InputValideringFeiletException("avsenderMottaker.id må være 7-9 siffer dersom avsenderMottaker.idType=HPRNR.");
					}
					break;
				default:
					// noop
					break;
			}
		}
	}

	private void validateDato(LocalDateTime dato, String datoFeltNavn) {
		LocalDateTime naaTid = LocalDateTime.now().plusSeconds(3);
		if (dato.isAfter(naaTid)) {
			throw new InputValideringFeiletException(format("Validering av %s feilet. Dato kan ikke være frem i tid. %s er %s og nåtid er %s",
					datoFeltNavn,
					datoFeltNavn,
					dato,
					naaTid
			));
		}
	}

	private void softValidateDato(Date dato, String datoFeltNavn) {
		var innsendtDato = dato.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); // Konverterer til lokaltid på formatet "yyyy-MM-dd"
		var dagensDato = LocalDate.now();

		if (innsendtDato.isAfter(dagensDato)) {
			log.warn(format("Validering av %s feilet. %s kan ikke være etter dagens dato=%s, men %s=%s (%s)",
					datoFeltNavn,
					datoFeltNavn,
					dagensDato,
					datoFeltNavn,
					innsendtDato,
					dato
			));
		}
	}

	private void validateBruker(Bruker bruker) {
		if (isBlank(bruker.getId())) {
			throw new InputValideringFeiletException("bruker.id må være satt.");
		}
		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("bruker.id må bestå av tall.");
		}
		if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 11 siffer dersom bruker.idType er FNR.");
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 9 siffer dersom bruker.idType er ORGNR.");
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			throw new InputValideringFeiletException("bruker.id må være 13 siffer dersom bruker.idType er AKTOERID.");
		}
	}

	private void validateTema(String tema) {
		if (StringUtils.isEmpty(tema)) {
			throw new InputValideringFeiletException(format("Kan ikke opprette journalpost uten tema. Mottok tema=%s", tema));
		}

		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Mottatt tema=%s %s. Gyldige verdier for tema er %s",
					tema,
					VALIDERER_IKKE_MOT_KODEVERK,
					Arrays.toString(FagomradeCode.values())));
		}
	}

	private void validatejournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (journalfoerendeEnhet != null && !JOURNALFOERENDE_ENHET_PATTERN.matcher(journalfoerendeEnhet).matches()) {
			throw new InputValideringFeiletException(format("journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=%s", journalfoerendeEnhet));
		}
	}

	private void validateJournalfoerendeEnhet(String journalpostFerdigstilt, String journalfoerendeEnhet, JournalpostType journalposttype) {
		if (FALSE.toString().equals(journalpostFerdigstilt) && MASKINELL_JOURNALFOERENDE_ENHET.equals(journalfoerendeEnhet) && INNGAAENDE.equals(journalposttype)) {
			throw new InputValideringFeiletException(format("Ikke mulig å opprette journalpost med type inngaaende på journalfoerendeEnhet=%s (maskinell) så lenge journalposten ikke forsøkes å ferdigstilles",
					MASKINELL_JOURNALFOERENDE_ENHET));
		}
	}

	private void validateBehandlingstema(String behandlingstema) {
		if (behandlingstema.length() != 6 || !behandlingstema.startsWith("ab")) {
			throw new InputValideringFeiletException(format("behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=%s", behandlingstema));
		}
	}

	private void validateJournalpostTittel(String tittel) {
		validateSkjultTittel(tittel, "tittel");
	}

	static void validateSkjultTittel(String tittel, String felt) {
		if (SKJULT_TITTEL.equals(tittel)) {
			throw new InputValideringFeiletException(felt + " kan ikke være " + SKJULT_TITTEL);
		}
	}

	private void validateKanal(OpprettJournalpostRequest request) {
		if (request.isInngaaende()) {
			if (request.getKanal() == null) {
				throw new InputValideringFeiletException("kanal er påkrevd for inngående journalposter");
			}

			try {
				MottaksKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Mottatt kanal=%s %s. Gyldige verdier for kanal er %s",
						request.getKanal(),
						VALIDERER_IKKE_MOT_KODEVERK,
						Arrays.toString(MottaksKanalCode.values())));
			}

			if (MottaksKanalCode.valueOf(request.getKanal()) == NAV_NO_UINNLOGGET && !request.getTema().equalsIgnoreCase(SER.name())) {
				throw new InputValideringFeiletException(format("Det er kun mulig å arkivere med mottakskanal=%s dersom tema=%s",
						NAV_NO_UINNLOGGET,
						SER.name()));
			}

		} else if (isNotBlank(request.getKanal())) {
			try {
				UtsendingsKanalCode.valueOf(request.getKanal());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Mottatt kanal=%s %s. Gyldige verdier for kanal er %s",
						request.getKanal(),
						VALIDERER_IKKE_MOT_KODEVERK,
						Arrays.toString(UtsendingsKanalCode.values())));
			}
		}
	}

	private void validateSak(Sak sak, Bruker bruker) {
		if (FAGSAK.equals(sak.getSakstype())) {
			validateFagsak(sak, bruker);
		}
		if (GENERELL_SAK.equals(sak.getSakstype())) {
			validateGenerellSak(sak, bruker);
		}
		if (ARKIVSAK.equals(sak.getSakstype()) || sak.getSakstype() == null) {
			validateArkivsak(sak);
		}
	}

	private void validateFagsak(Sak sak, Bruker bruker) {
		if (bruker == null) {
			throw new InputValideringFeiletException("bruker må være satt dersom sak.sakstype=FAGSAK");
		}
		if (isBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("sak.fagsakId må være satt dersom sak.sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			throw new InputValideringFeiletException("sak.fagsaksystem må være satt dersom sak.sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("sak.arkivsaksnummer kan ikke være satt dersom sak.sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("sak.arkivsaksystem kan ikke være satt dersom sak.sakstype=FAGSAK");
		}
		if (FAGSAK == sak.getSakstype() && PP01 == sak.getFagsaksystem()) {
			if (!isNumeric(sak.getFagsakId())) {
				throw new InputValideringFeiletException("sak.fagsakId må være et heltall dersom saken er opprett i PSAK");
			}
		}
	}

	private void validateGenerellSak(Sak sak, Bruker bruker) {
		if (bruker == null) {
			throw new InputValideringFeiletException("bruker må være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("sak.fagsakId kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("sak.fagsaksystem kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("sak.arkivsaksnummer kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("sak.arkivsaksystem kan ikke være satt dersom sak.sakstype=GENERELL_SAK");
		}
	}

	private void validateArkivsak(Sak sak) {
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("sak.fagsakId kan ikke være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("sak.fagsaksystem kan ikke være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("sak.arkivsaksnummer må være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (sak.getArkivsaksystem() == null) {
			throw new InputValideringFeiletException("sak.arkivsaksystem må være satt dersom sak.sakstype=ARKIVSAK");
		}
		if (!isNumeric(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("sak.arkivsaksnummer må være et heltall, og saken må være opprettet i GSAK/PSAK");
		}
	}

	private void validateOverstyrInnsynsregler(String overstyrInnsynsregler) {
		if (!LOVLIGE_INNSYNSKODER.contains(overstyrInnsynsregler)) {
			throw new InputValideringFeiletException(format("overstyrInnsynsregler må være en av følgende verdier: null eller %s. Mottatt: %s",
					LOVLIGE_INNSYNSKODER,
					overstyrInnsynsregler));
		}
	}
}