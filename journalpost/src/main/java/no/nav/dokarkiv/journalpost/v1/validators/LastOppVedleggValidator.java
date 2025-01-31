package no.nav.dokarkiv.journalpost.v1.validators;

import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.KanIkkeLeggeTilVedleggException;
import no.nav.dokarkiv.journalpost.v1.api.Dokument;
import no.nav.dokarkiv.journalpost.v1.api.DokumentVariant;
import no.nav.dokarkiv.journalpost.v1.api.lastOppVedlegg.LastOppVedleggRequest;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.VariantFormatCode.ARKIV;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class LastOppVedleggValidator {

	private LastOppVedleggValidator() {
	}

	public static void validateRequest(LastOppVedleggRequest request) {
		if (request == null) {
			throw new InputValideringFeiletException("LastOppVedleggRequest kan ikke være null");
		}

		if (request.dokument() == null) {
			throw new InputValideringFeiletException("dokument kan ikke være null");
		}

		validateDokument(request.dokument());
	}

	private static void validateDokument(Dokument dokument) {
		if (isEmpty(dokument.getTittel())) {
			throw new InputValideringFeiletException("dokument.tittel kan ikke være tom eller null");
		}

		if (CollectionUtils.isEmpty(dokument.getDokumentvarianter())) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter kan ikke være null eller en tom liste");
		}

		var dokumentvarianter = dokument.getDokumentvarianter();

		dokumentvarianter.forEach(LastOppVedleggValidator::validateDokumentvariant);

		var duplikateVarianter = dokumentvarianter.stream()
				.collect(groupingBy(DokumentVariant::getVariantformat, Collectors.counting()))
				.entrySet().stream()
				.filter(entry -> entry.getValue() > 1)
				.map(Map.Entry::getKey)
				.toList();

		if (!duplikateVarianter.isEmpty()) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter inneholder mer enn én dokumentvariant med følgende variantformat(er): %s"
					.formatted(duplikateVarianter));
		}
	}

	private static void validateDokumentvariant(DokumentVariant dokumentvariant) {
		if (isEmpty(dokumentvariant.getVariantformat())) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter sin dokumentvariant med filtype=%s mangler variantformat".formatted(dokumentvariant.getFiltype()));
		}

		if (isEmpty(dokumentvariant.getFiltype())) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter sin dokumentvariant med variantformat=%s mangler filtype".formatted(dokumentvariant.getVariantformat()));
		}

		if (ArrayUtils.isEmpty(dokumentvariant.getFysiskDokument())) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter sin dokumentvariant med variantformat=%s mangler fysisk dokument".formatted(dokumentvariant.getVariantformat()));
		}

		if (isEmpty(dokumentvariant.getFilnavn())) {
			throw new InputValideringFeiletException("dokument.dokumentvarianter sin dokumentvariant med variantformat=%s mangler filnavn".formatted(dokumentvariant.getVariantformat()));
		}
	}

	public static void validateJournalpostAndDokument(Journalpost journalpost, Dokument dokument) {
		validateJournalpostStatus(journalpost);
		validateHoveddokument(journalpost);
		validateDuplikatVedlegg(journalpost, dokument);
	}

	private static void validateJournalpostStatus(Journalpost journalpost) {
		if (!D.equals(journalpost.getJournalstatus())) {
			throw new KanIkkeLeggeTilVedleggException("Journalposten har status=%s, men må ha status=%s"
					.formatted(journalpost.getJournalstatus(), D.name()));
		}
	}

	private static void validateHoveddokument(Journalpost journalpost) {
		if (!journalpost.hasHoveddokumentRelasjon()) {
			throw new KanIkkeLeggeTilVedleggException("Journalposten må et hoveddokument");
		}
	}

	private static void validateDuplikatVedlegg(Journalpost journalpost, Dokument dokument) {

		var filnavnForEksisterendeArkivVariant = journalpost.getJournalpostDokumentInfoRelasjoner().stream()
				.filter(it -> it.getDokumentInfo().hasArkivFormat())
				.flatMap(it -> it.getDokumentInfo().getFildetaljerListe().stream())
				.filter(FilDetaljer::isArkivVariant)
				.map(FilDetaljer::getFilnavn)
				.toList();

		dokument.getDokumentvarianter().stream()
				.filter(it -> it.getVariantformat().equals(ARKIV.name()))
				.filter(it -> filnavnForEksisterendeArkivVariant.contains(it.getFilnavn()))
				.findFirst()
				.ifPresent(duplikat -> {
					throw new KanIkkeLeggeTilVedleggException("Dokument med variantformat=%s og filnavn=%s er allerede tilknyttet journalposten"
							.formatted(ARKIV.name(), duplikat.getFilnavn()));
				});
	}
}