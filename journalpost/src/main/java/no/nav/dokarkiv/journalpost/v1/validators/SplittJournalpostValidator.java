package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.domain.validator.EksternReferanseIdValidator;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.api.Bruker;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest;
import no.nav.dokarkiv.journalpost.v1.api.splittJournalpost.SplittJournalpostRequest.SplittDokument;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.FNR;
import static no.nav.dokarkiv.journalpost.v1.api.BrukerIdType.ORGNR;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SplittJournalpostValidator {

	private static final String FNR_REGEX = "\\d{11}";
	private static final String ORGNR_REGEX = "\\d{9}";

	private SplittJournalpostValidator() {}

	public static void valider(SplittJournalpostRequest request, Journalpost journalpost) {

		List<String> feilmeldinger = new ArrayList<>();

		feilmeldinger.addAll(validerTema(request.tema()));
		feilmeldinger.addAll(validerBruker(request.bruker()));
		feilmeldinger.addAll(validerJournalfoerendeEnhet(request.journalfoerendeEnhet()));
		feilmeldinger.addAll(validerEksternReferanseId(request.eksternReferanseId()));

		feilmeldinger.addAll(validerDokumenter(request.dokumenter(), journalpost));

		if (!feilmeldinger.isEmpty()) {
			throw new InputValideringFeiletException(String.join(", ", feilmeldinger));
		}
	}

	private static List<String> validerEksternReferanseId(String eksternReferanseId) {
		try {
			EksternReferanseIdValidator.validateEksternReferanseId(eksternReferanseId);
		} catch (InputValideringFeiletException e) {
			return List.of("Feltet %s".formatted(e.getMessage()));
		}

		return List.of();
	}

	private static List<String> validerJournalfoerendeEnhet(String journalfoerendeEnhet) {
		if (isNotBlank(journalfoerendeEnhet)) {
			try {
				CommonValidator.validateJournalfoerendeEnhet(journalfoerendeEnhet, "journalfoerendeEnhet");
			} catch (InputValideringFeiletException e) {
				return List.of(e.getMessage());
			}
		}

		return List.of();
	}

	private static List<String> validerTema(String tema) {
		if (isBlank(tema)) {
			return List.of();
		}

		try {
			FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			return List.of("Feltet tema=%s er ikke et gyldig tema.".formatted(tema));
		}
		return List.of();
	}

	private static List<String> validerDokumenter(List<SplittDokument> dokumenter, Journalpost journalpost) {
		List<Long> eksisterendeDokumenter = journalpost.findAllDokumentInfos().stream()
				.map(DokumentInfo::getDokumentInfoId)
				.toList();

		if (dokumenter.isEmpty()) {
			return List.of("Feltet dokumenter kan ikke være tomt.");
		}

		return dokumenter.stream()
				.flatMap(dokument -> validerDokument(dokument, eksisterendeDokumenter).stream())
				.toList();
	}

	private static List<String> validerDokument(SplittDokument dokument, List<Long> eksisterendeDokumenter) {
		List<String> feilmeldinger = new ArrayList<>();

		if (dokument.kopierUtenEndringer() == null) {
			feilmeldinger.add("Feltet dokument.kopierUtenEndringer kan ikke være null.");
		}

		if (!eksisterendeDokumenter.contains(dokument.dokumentInfoId())) {
			feilmeldinger.add("Dokument med dokumentInfoId=%s finnes ikke på den originale journalposten.".formatted(dokument.dokumentInfoId()));
		}

		if (TRUE.equals(dokument.kopierUtenEndringer()) && !dokument.dokumentvarianter().isEmpty()) {
			feilmeldinger.add("Feltet dokument.dokumentvarianter skal kun oppgis dersom dokument.kopierUtenEndringer=%s.".formatted(FALSE));
		} else {
			dokument.dokumentvarianter().forEach(it -> feilmeldinger.addAll(validerDokumentvariant(it)));
		}

		return feilmeldinger;
	}

	private static List<String> validerDokumentvariant(DokumentVariant dokumentvariant) {
		List<String> feilmeldinger = new ArrayList<>();

		if (isBlank(dokumentvariant.getFiltype())) {
			feilmeldinger.add("Feltet dokumentvariant.filtype kan ikke være null eller tomt.");
		} else {
			try {
				FilTypeCode.valueOf(dokumentvariant.getFiltype());
			} catch (IllegalArgumentException e) {
				feilmeldinger.add("Feltet dokumentvariant.filtype er ikke gyldig. Mottatt filtype=%s".formatted(dokumentvariant.getFiltype()));
			}
		}

		if (isBlank(dokumentvariant.getVariantformat())) {
			feilmeldinger.add("Feltet dokumentvariant.variantformat kan ikke være null eller tomt.");
		} else {
			try {
				VariantFormatCode.valueOf(dokumentvariant.getVariantformat());
			} catch (IllegalArgumentException e) {
				feilmeldinger.add("Feltet dokumentvariant.variantformat er ikke gyldig. Mottatt variantformat=%s".formatted(dokumentvariant.getVariantformat()));
			}
		}

		if (dokumentvariant.getFysiskDokument() == null || dokumentvariant.getFysiskDokument().length == 0) {
			feilmeldinger.add("Feltet dokumentvariant.fysiskDokument kan ikke være null eller tomt.");
		}

		return feilmeldinger;

	}

	private static List<String> validerBruker(Bruker bruker) {
		List<String> feilmeldinger = new ArrayList<>();

		if (bruker == null) {
			return feilmeldinger;
		}

		if (isBlank(bruker.getId())) {
			feilmeldinger.add("Feltet bruker.id må være satt");
		}

		if (bruker.getIdType() == null) {
			feilmeldinger.add("Feltet bruker.idType må være satt");
		}

		if (isNotBlank(bruker.getId()) && bruker.getIdType() != null) {
			switch (bruker.getIdType()) {
				case FNR -> {
					if (!bruker.getId().matches(FNR_REGEX)) {
						feilmeldinger.add("Feltet bruker.id må være 11 siffer dersom bruker.idType=%s.".formatted(FNR.name()));
					}
				}
				case ORGNR -> {
					if (!bruker.getId().matches(ORGNR_REGEX)) {
						feilmeldinger.add("Feltet bruker.id må være 9 siffer dersom bruker.idType=%s.".formatted(ORGNR.name()));
					}
				}
				default ->
					feilmeldinger.add("Feltet bruker.idType må være en av %s".formatted(List.of(FNR.name(), ORGNR.name())));
			}
		}
		return feilmeldinger;
	}
}
