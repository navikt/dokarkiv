package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.DokumentKategoriCode;
import no.nav.dokarkiv.core.domain.codes.FilTypeCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.InvalidPdfException;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.copyOf;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDFA;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.valueOf;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ORIGINAL;
import static no.nav.dokarkiv.journalpost.v1.validators.CommonValidator.isConsumerFagsystemArgus;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.PDF_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.isFileContentContainsValidMagicNumber;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.validateSkjultTittel;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

@Slf4j
public final class DokumentValidator {

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";

	private DokumentValidator() {
	}

	public static void validateDokument(final Dokument dokument) {
		validateDokument(null, dokument);
	}

	public static void validateDokument(final Integer dokumentIdx, Dokument dokument) {
		validateSkjultTittel(dokument.getTittel(), dokumentnummerPrefix(dokumentIdx) + ".tittel");
		validateDokumentKategori(dokumentIdx, dokument);
		validateDokumentVarianter(dokumentIdx, dokument);
	}

	private static void validateDokumentKategori(Integer dokumentIdx, Dokument dokument) {
		if (isNotBlank(dokument.getDokumentKategori())) {
			try {
				DokumentKategoriCode.valueOf(dokument.getDokumentKategori());
			} catch (IllegalArgumentException e) {
				throw new InputValideringFeiletException(format("%s.dokumentKategori %s. Gyldige verdier for dokumentKategori er %s. Mottatt dokumentKategori=%s",
						dokumentnummerPrefix(dokumentIdx),
						VALIDERER_IKKE_MOT_KODEVERK,
						Arrays.toString(DokumentKategoriCode.values()),
						dokument.getDokumentKategori()));
			}
		}
	}

	private static void validateDokumentVarianter(Integer dokumentIdx, Dokument dokument) {
		if (!isEmpty(dokument.getDokumentvarianter())) {
			dokument.getDokumentvarianter().forEach(dokumentVariant -> validateDokumentvariant(dokumentIdx, dokumentVariant));
			validateUniqueDokumentvariant(dokument.getDokumentvarianter(), dokument);
			// Spesialhåndtering for Argus (dsop-kontroll) slik at de kan ferdigstille Excel-filer som ORIGINAL variant
			if (isConsumerFagsystemArgus()) {
				validateDokumentvarianterFagsystemArgus(dokument);
			} else {
				validateOneArkivVariantFormatPerDokument(dokument.getDokumentvarianter(), dokument);
			}
		} else {
			throw new InputValideringFeiletException(format("Alle dokumenter må innholde en dokumentvariant av typen %s", ARKIV.name()));
		}
	}

	private static void validateDokumentvarianterFagsystemArgus(Dokument dokument) {
		try {
			validateOneArkivVariantFormatPerDokument(dokument.getDokumentvarianter(), dokument);
		} catch (InputValideringFeiletException e) {
			validateOneOriginalVariantFormatPerDokument(dokument.getDokumentvarianter(), dokument);
		}
	}

	private static void validateOneArkivVariantFormatPerDokument(List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		validateOneVariantFormatPerDokument(ARKIV.name(), dokumentvarianter, dokument);
	}

	private static void validateOneOriginalVariantFormatPerDokument(List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		validateOneVariantFormatPerDokument(ORIGINAL.name(), dokumentvarianter, dokument);
	}

	private static void validateOneVariantFormatPerDokument(String variantFormat, List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		if (dokumentvarianter.stream()
					.filter(dokumentVariant -> dokumentVariant.getVariantformat().equals(variantFormat))
					.count() != 1) {
			throw new InputValideringFeiletException(format("Alle dokumenter må innholde en dokumentvariant av typen %s. %s inneholder følgende varianter: %s",
					variantFormat,
					dokument.getTittel(),
					dokumentvarianter.stream()
							.map(DokumentVariant::getVariantformat)
							.collect(Collectors.joining(", "))));
		}
	}

	private static void validateUniqueDokumentvariant(List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		String duplikater = dokumentvarianter
				.stream()
				.collect(Collectors.groupingBy(DokumentVariant::getVariantformat, Collectors.counting()))
				.entrySet()
				.stream()
				.filter(s -> s.getValue() > 1)
				.map(entry -> format("variantformat=%s funnet %s ganger", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(", "));

		if (!duplikater.isEmpty()) {
			throw new InputValideringFeiletException(format("dokumenter.dokumentvarianter.variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: %s",
					dokument.getTittel(),
					duplikater));
		}
	}

	private static void validateDokumentvariant(final Integer dokumentIdx, DokumentVariant dokumentVariant) {
		final String variantFormat = dokumentVariant.getVariantformat();

		if (isBlank(dokumentVariant.getFiltype())) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.filtype må være satt for variantformat=%s", dokumentnummerPrefix(dokumentIdx), variantFormat));
		}

		try {
			FilTypeCode.valueOf(dokumentVariant.getFiltype());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.filtype %s for variantformat=%s. Gyldige verdier for filtype er %s. Mottatt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					VALIDERER_IKKE_MOT_KODEVERK,
					variantFormat,
					Arrays.toString(FilTypeCode.values()),
					dokumentVariant.getFiltype()));
		}

		if (isBlank(variantFormat)) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.variantformat må være satt", dokumentnummerPrefix(dokumentIdx)));
		}

		try {
			VariantFormatCode.valueOf(variantFormat);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.variantformat %s. Gyldige verdier for variantformat er %s. Mottatt variantformat=%s",
					dokumentnummerPrefix(dokumentIdx),
					VALIDERER_IKKE_MOT_KODEVERK,
					Arrays.toString(VariantFormatCode.values()),
					variantFormat));
		}

		if (variantFormat.equals(ARKIV.name()) && !Arrays.asList(PDF, PDFA).contains(valueOf(dokumentVariant.getFiltype()))) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.filtype må være PDF eller PDFA for variantformat=ARKIV. Mottatt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					dokumentVariant.getFiltype()));
		}

		if (dokumentVariant.getFysiskDokument() == null || dokumentVariant.getFysiskDokument().length == 0) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter.fysiskDokument for variantformat=%s må være en base64 representert fil større enn 0 bytes",
					dokumentnummerPrefix(dokumentIdx), variantFormat));
		}

		if (!isFileContentContainsValidMagicNumber(dokumentVariant.getFiltype(), dokumentVariant.getFysiskDokument())) {
			throw new InvalidPdfException(format("%s.dokumentvarianter.fysiskDokument med variantformat=%s kan ikke lagres i fagarkivet. fysiskDokument magicNumber={%s} matcher ikke angitt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					variantFormat,
					HexFormat.of().withUpperCase()
							.withDelimiter(" ")
							.formatHex(copyOf(dokumentVariant.getFysiskDokument(), PDF_MAGIC_NUMBER.length)),
					dokumentVariant.getFiltype()));
		}
	}

	private static String dokumentnummerPrefix(Integer dokumentIdx) {
		return dokumentIdx == null ? "dokumenter[0]" : "dokumenter[%s]".formatted(dokumentIdx);
	}
}