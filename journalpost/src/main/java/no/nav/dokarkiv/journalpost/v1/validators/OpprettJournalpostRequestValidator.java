package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.journalpost.v1.api.AvsenderMottaker;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.Sak;
import no.nav.dokarkiv.journalpost.v1.api.opprettjournalpost.OpprettJournalpostRequest;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static java.util.Arrays.copyOf;
import static no.nav.dokarkiv.core.domain.codes.FagomradeCode.SER;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDFA;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.valueOf;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MANUELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.InnsynCode.VISES_MASKINELT_GODKJENT;
import static no.nav.dokarkiv.core.domain.codes.MottaksKanalCode.NAV_NO_UINNLOGGET;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.PDF_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.isFileMagicNumberValid;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.AKTOERID;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static no.nav.dokarkiv.journalpost.v1.api.Fagsaksystem.PP01;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.ARKIVSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.FAGSAK;
import static no.nav.dokarkiv.journalpost.v1.api.Sakstype.GENERELL_SAK;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

@Slf4j
public class OpprettJournalpostRequestValidator {

	private static final int FNR_LENGTH = 11;
	private static final int AKTOERID_LENGTH = 13;
	private static final int ORGNR_LENGTH = 9;
	public static final String MASKINELL_JOURNALFOERENDE_ENHET = "9999";
	public static final Set<String> LOVLIGE_INNSYNSKODER = Set.of(VISES_MASKINELT_GODKJENT.toString(), VISES_MANUELT_GODKJENT.toString());

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";
	private static final Pattern JOURNALFOERENDE_ENHET_PATTERN = Pattern.compile("^\\d{4}$");

	public void validateRequest(OpprettJournalpostRequest request, String journalpostFerdigstilt) {

		validateTema(request.getTema());

		if (request.getAvsenderMottaker() != null) {
			validateAvsenderMottaker(request.getAvsenderMottaker());
		}
		if (request.getBruker() != null) {
			validateBruker(request.getBruker());
		}
		if (isNotBlank(request.getBehandlingstema())) {
			validateBehandlingstema(request.getBehandlingstema());
		}
		if (isNotBlank(request.getKanal())) {
			validateKanal(request);
		}
		if (request.getSak() != null) {
			validateSak(request.getSak(), request.getBruker());
		}
		if (isNotBlank(request.getJournalfoerendeEnhet())) {
			validateJournalpost(journalpostFerdigstilt, request.getJournalfoerendeEnhet());
		}
		if (request.getDatoDokument() != null) {
			validateDato(request.getDatoDokument(), "DatoDokument");
		}
		if (request.getDatoMottatt() != null) {
			softValidateDato(request.getDatoMottatt(), "DatoMottatt");
		}
		if (!request.getDokumenter().isEmpty()) {
			request.getDokumenter().forEach(this::validateDokument);
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
			throw new InputValideringFeiletException("AvsenderMottaker.idType må være satt når AvsenderMottaker.id er satt.");
		}
		if (avsenderMottaker.getIdType() != null && isBlank(avsenderMottaker.getId())) {
			throw new InputValideringFeiletException("AvsenderMottaker.id må være satt når AvsenderMottaker.idType er satt.");
		}
		if (avsenderMottaker.getIdType() != null) {
			switch (avsenderMottaker.getIdType()) {
				case FNR:
					if (!avsenderMottaker.getId().matches("^\\d{11}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 11 siffer når AvsenderMottaker.idType er " + avsenderMottaker
								.getIdType() + ".");
					}
					break;
				case ORGNR:
					if (!avsenderMottaker.getId().matches("^\\d{9}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 9 siffer når AvsenderMottaker.idType er " + avsenderMottaker
								.getIdType() + ".");
					}
					break;
				case HPRNR:
					if (!avsenderMottaker.getId().matches("^\\d{7,9}$")) {
						throw new InputValideringFeiletException("AvsenderMottaker.id må være 7-9 siffer når AvsenderMottaker.idType er " + avsenderMottaker
								.getIdType() + ".");
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
			throw new InputValideringFeiletException("Bruker.id må være satt.");
		}
		if (!isNumeric(bruker.getId())) {
			throw new InputValideringFeiletException("Bruker.id må bestå av tall.");
		}
		if (FNR.equals(bruker.getIdType()) && bruker.getId().length() != FNR_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 11 siffer for FNR.");
		} else if (ORGNR.equals(bruker.getIdType()) && bruker.getId().length() != ORGNR_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 9 siffer for ORGNR.");
		} else if (AKTOERID.equals(bruker.getIdType()) && bruker.getId().length() != AKTOERID_LENGTH) {
			throw new InputValideringFeiletException("Bruker.id må være 11 siffer for AKTOERID.");
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
			throw new InputValideringFeiletException(format("Journalpost.journalfoerendeEnhet må være null eller fire siffer. Mottatt journalfoerendeEnhet=%s", journalfoerendeEnhet));
		}
	}

	private void validateJournalpost(String journalpostFerdigstilt, String journalfoerendeEnhet) {
		if (FALSE.toString().equals(journalpostFerdigstilt) && MASKINELL_JOURNALFOERENDE_ENHET.equals(journalfoerendeEnhet)) {
			throw new InputValideringFeiletException(format("Ikke mulig å opprette journalpost på journalfoerendeEnhet=%s (maskinell) så lenge journalposten ikke forsøkes å ferdigstilles",
					MASKINELL_JOURNALFOERENDE_ENHET));
		}
	}

	private void validateBehandlingstema(String behandlingstema) {
		if (behandlingstema.length() != 6 || !behandlingstema.startsWith("ab")) {
			throw new InputValideringFeiletException(format("Behandlingstema må være på formatet ´ab + 4 siffer´. Mottatt behandlingstema=%s", behandlingstema));
		}
	}

	private void validateKanal(OpprettJournalpostRequest request) {
		if (request.isInngaaende()) {
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

		} else {
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
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=FAGSAK");
		}
		if (isBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId må være satt dersom sakstype=FAGSAK");
		}
		if (sak.getFagsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem må være satt dersom sakstype=FAGSAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer kan ikke være satt dersom sakstype=FAGSAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem kan ikke være satt dersom sakstype=FAGSAK");
		}
		if (FAGSAK == sak.getSakstype() && PP01 == sak.getFagsaksystem()) {
			if (!isNumeric(sak.getFagsakId())) {
				throw new InputValideringFeiletException("Sak.fagsakId må være et heltall dersom saken er opprett i PSAK");
			}
		}
	}

	private void validateGenerellSak(Sak sak, Bruker bruker) {
		if (bruker == null) {
			throw new InputValideringFeiletException("Bruker må være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (isNotBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
		if (sak.getArkivsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem kan ikke være satt dersom sakstype=GENERELL_SAK");
		}
	}

	private void validateArkivsak(Sak sak) {
		if (isNotBlank(sak.getFagsakId())) {
			throw new InputValideringFeiletException("Sak.fagsakId kan ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (sak.getFagsaksystem() != null) {
			throw new InputValideringFeiletException("Sak.fagsaksystem kan ikke være satt dersom sakstype=ARKIVSAK");
		}
		if (isBlank(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer må være satt dersom sakstype=ARKIVSAK");
		}
		if (sak.getArkivsaksystem() == null) {
			throw new InputValideringFeiletException("Sak.arkivsaksystem må være satt dersom sakstype=ARKIVSAK");
		}
		if (!isNumeric(sak.getArkivsaksnummer())) {
			throw new InputValideringFeiletException("Sak.arkivsaksnummer må være et heltall, og saken må være opprettet i GSAK/PSAK");
		}
	}

	private void validateDokument(Dokument dokument) {
		if (isNotBlank(dokument.getDokumentKategori())) {
			try {
				DokumentKategoriCode.valueOf(dokument.getDokumentKategori());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("Dokument.dokumentkategori %s. Mottatt dokumentkategori=%s",
						VALIDERER_IKKE_MOT_KODEVERK,
						dokument.getDokumentKategori()));
			}
		}
		if (!isEmpty(dokument.getDokumentvarianter())) {
			dokument.getDokumentvarianter().forEach(this::validateDokumentVariant);
			validateUniqueVariant(dokument.getDokumentvarianter(), dokument);
		}
	}

	private void validateUniqueVariant(List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		String duplikater = dokumentvarianter
				.stream()
				.collect(Collectors.groupingBy(DokumentVariant::getVariantformat, Collectors.counting()))
				.entrySet()
				.stream()
				.filter(s -> s.getValue() > 1)
				.map(entry -> format("Variantformat=%s funnet %s ganger", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(", "));

		if (!duplikater.isEmpty()) {
			throw new InputValideringFeiletException(format("Dokument.dokumentvariant.variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: %s",
					dokument.getTittel(),
					duplikater));
		}
	}

	private void validateDokumentVariant(DokumentVariant dokumentVariant) {
		if (isBlank(dokumentVariant.getFiltype())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype må være satt");
		}
		try {
			valueOf(dokumentVariant.getFiltype());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Dokument.dokumentvariant.filtype %s. Gyldige verdier for filtype er %s",
					VALIDERER_IKKE_MOT_KODEVERK,
					Arrays.toString(FilTypeCode.values())));
		}
		if (isBlank(dokumentVariant.getVariantformat())) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.variantformat må være satt");
		}
		try {
			VariantFormatCode.valueOf(dokumentVariant.getVariantformat());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Dokument.dokumentvariant.variantformat %s. Gyldige verdier for variantformat er %s",
					VALIDERER_IKKE_MOT_KODEVERK,
					Arrays.toString(VariantFormatCode.values())));
		}
		if (dokumentVariant.getVariantformat().equals(VariantFormatCode.ARKIV.name())
				&& !Arrays.asList(PDF, PDFA).contains(valueOf(dokumentVariant.getFiltype()))) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.filtype må være PDF eller PDFA for Dokument.dokumentvariant.variantformat=ARKIV.");
		}
		if (dokumentVariant.getFysiskDokument() == null || dokumentVariant.getFysiskDokument().length == 0) {
			throw new InputValideringFeiletException("Dokument.dokumentvariant.fysiskDokument må være en base64 representert fil større en 0 bytes.");
		}

		if (!isFileMagicNumberValid(dokumentVariant.getFiltype(), dokumentVariant.getFysiskDokument())) {
			log.warn("Dokument.dokumentvariant.fysiskDokument har ugyldig PDF/A magisk tall={ }.", HexFormat.of()
					.withUpperCase()
					.withDelimiter(" ")
					.formatHex(copyOf(dokumentVariant.getFysiskDokument(), PDF_MAGIC_NUMBER.length)));
			throw new InvalidPdfException(format("Dokument.dokumentvariant.fysiskDokument har ugyldig PDF/A magisk tall={%s}.", HexFormat.of()
					.withUpperCase()
					.withDelimiter(" ")
					.formatHex(copyOf(dokumentVariant.getFysiskDokument(), PDF_MAGIC_NUMBER.length))));
		}
	}

	private void validateOverstyrInnsynsregler(String overstyrInnsynsregler) {
		if (!LOVLIGE_INNSYNSKODER.contains(overstyrInnsynsregler)) {
			throw new InputValideringFeiletException(format("Sak.overstyrInnsynsregler må være en av følgende verdier %s. Mottatt: %s",
					LOVLIGE_INNSYNSKODER,
					overstyrInnsynsregler));
		}
	}
}