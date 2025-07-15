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
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.copyOf;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDF;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.PDFA;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.XLSX;
import static no.nav.dokarkiv.core.domain.codes.FilTypeCode.valueOf;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static no.nav.dokarkiv.journalpost.v1.JournalpostApiConstants.MIN_VEDLEGG_REKKEFOELGE;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.PDF_MAGIC_NUMBER;
import static no.nav.dokarkiv.journalpost.v1.validators.FilMagicNumberValidator.isFileContentContainsValidMagicNumber;
import static no.nav.dokarkiv.journalpost.v1.validators.OpprettJournalpostRequestValidator.validateSkjultTittel;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.cxf.common.util.CollectionUtils.isEmpty;

@Slf4j
public final class DokumentValidator {

	private static final String VALIDERER_IKKE_MOT_KODEVERK = "validerer ikke mot kodeverk";
	private static final EnumSet<FilTypeCode> VARIANTFORMAT_ARKIV_GYLDIGE_FILTYPER = EnumSet.of(PDF, PDFA, XLSX);

	private DokumentValidator() {
	}

	public static void validateDokument(final Dokument dokument) {
		validateDokument(null, dokument);
	}

	public static void validateDokument(final Integer dokumentIdx, Dokument dokument) {
		validateSkjultTittel(dokument.getTittel(), dokumentnummerPrefix(dokumentIdx) + ".tittel");
		validateDokumentKategori(dokumentIdx, dokument);
		validateDokumentVarianter(dokumentIdx, dokument);
		validateRekkefoelge(dokumentIdx, dokument);
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
			validateUniqueDokumentvariant(dokumentIdx, dokument);
			validateOneArkivVariantFormatPerDokument(dokument.getDokumentvarianter(), dokument);
		} else {
			throw new InputValideringFeiletException(format("Alle dokumenter må innholde en dokumentvariant av typen %s", ARKIV.name()));
		}
	}

	private static void validateOneArkivVariantFormatPerDokument(List<DokumentVariant> dokumentvarianter, Dokument dokument) {
		validateOneVariantFormatPerDokument(ARKIV.name(), dokumentvarianter, dokument);
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

	private static void validateUniqueDokumentvariant(final Integer dokumentIdx, Dokument dokument) {
		String duplikater = dokument.getDokumentvarianter()
				.stream()
				.collect(Collectors.groupingBy(DokumentVariant::getVariantformat, Collectors.counting()))
				.entrySet()
				.stream()
				.filter(s -> s.getValue() > 1)
				.map(entry -> format("variantformat=%s funnet %s ganger", entry.getKey(), entry.getValue()))
				.collect(Collectors.joining(", "));

		if (!duplikater.isEmpty()) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].variantformat må være unik. Fant følgende duplikater for dokument med tittel=%s: %s",
					dokumentnummerPrefix(dokumentIdx),
					dokument.getTittel(),
					duplikater));
		}
	}

	private static void validateDokumentvariant(final Integer dokumentIdx, DokumentVariant dokumentVariant) {
		final String variantFormat = dokumentVariant.getVariantformat();

		if (isBlank(dokumentVariant.getFiltype())) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].filtype må være satt for variantformat=%s", dokumentnummerPrefix(dokumentIdx), variantFormat));
		}

		try {
			FilTypeCode.valueOf(dokumentVariant.getFiltype());
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].filtype %s for variantformat=%s. Gyldige verdier for filtype er %s. Mottatt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					VALIDERER_IKKE_MOT_KODEVERK,
					variantFormat,
					Arrays.toString(FilTypeCode.values()),
					dokumentVariant.getFiltype()));
		}

		if (isBlank(variantFormat)) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].variantformat må være satt", dokumentnummerPrefix(dokumentIdx)));
		}

		try {
			VariantFormatCode.valueOf(variantFormat);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].variantformat %s. Gyldige verdier for variantformat er %s. Mottatt variantformat=%s",
					dokumentnummerPrefix(dokumentIdx),
					VALIDERER_IKKE_MOT_KODEVERK,
					Arrays.toString(VariantFormatCode.values()),
					variantFormat));
		}

		if (variantFormat.equals(ARKIV.name()) && !VARIANTFORMAT_ARKIV_GYLDIGE_FILTYPER.contains(valueOf(dokumentVariant.getFiltype()))) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].filtype må være %s for variantformat=ARKIV. Mottatt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					prettyPrintList("eller", VARIANTFORMAT_ARKIV_GYLDIGE_FILTYPER.stream().map(FilTypeCode::name).toArray(String[]::new)),
					dokumentVariant.getFiltype()));
		}

		if (dokumentVariant.getFysiskDokument() == null || dokumentVariant.getFysiskDokument().length == 0) {
			throw new InputValideringFeiletException(format("%s.dokumentvarianter[].fysiskDokument for variantformat=%s må være en base64 representert fil større enn 0 bytes",
					dokumentnummerPrefix(dokumentIdx), variantFormat));
		}

		if (!isFileContentContainsValidMagicNumber(dokumentVariant.getFiltype(), dokumentVariant.getFysiskDokument())) {
			throw new InvalidPdfException(format("%s.dokumentvarianter[].fysiskDokument med variantformat=%s kan ikke lagres i fagarkivet. fysiskDokument magicNumber={%s} matcher ikke angitt filtype=%s",
					dokumentnummerPrefix(dokumentIdx),
					variantFormat,
					HexFormat.of().withUpperCase()
							.withDelimiter(" ")
							.formatHex(copyOf(dokumentVariant.getFysiskDokument(), PDF_MAGIC_NUMBER.length)),
					dokumentVariant.getFiltype()));
		}
	}

	private static void validateRekkefoelge(Integer dokumentIdx, Dokument dokument) {
		if (dokument.getRekkefoelge() != null && dokument.getRekkefoelge() < MIN_VEDLEGG_REKKEFOELGE) {
			throw new InputValideringFeiletException("%s.rekkefoelge må være null eller et positivt heltall. Mottatt rekkefoelge=%s"
					.formatted(dokumentnummerPrefix(dokumentIdx), dokument.getRekkefoelge()));
		}
	}

	private static String dokumentnummerPrefix(Integer dokumentIdx) {
		return dokumentIdx == null ? "dokumenter[0]" : "dokumenter[%s]".formatted(dokumentIdx);
	}

	private static String prettyPrintList(String konjunksjon, String... list) {
		if (list.length == 1) {
			return list[0];
		}

		StringBuilder stringBuilder = new StringBuilder();
		int length = list.length - 1;
		for (int i = 0; i < length - 1; i++) {
			stringBuilder.append(list[i]).append(", ");
		}
		return stringBuilder.append(list[length - 1]).append(" ").append(konjunksjon).append(" ").append(list[length]).toString();
	}
}