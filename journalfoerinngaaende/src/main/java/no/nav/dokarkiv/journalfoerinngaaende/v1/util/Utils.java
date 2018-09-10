package no.nav.dokarkiv.journalfoerinngaaende.v1.util;

import no.nav.dok.tjenester.journalfoerinngaaende.Dokument;
import no.nav.dok.tjenester.journalfoerinngaaende.GetJournalpostResponse;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.DokumentIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeInngaaendeException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Utils {

	public static final String JOURNALPOST_ID = "journalpostId";
	public static final String DOKUMENT_ID = "dokumentId";
	public static final String LOGISK_VEDLEGG_ID = "logiskVedleggId";

	public static void validateIds(String journalpostId, String dokumentId, String logiskVedleggId) {
		try {
			hasText(journalpostId, JOURNALPOST_ID);
			hasText(dokumentId, DOKUMENT_ID);
			hasText(logiskVedleggId, LOGISK_VEDLEGG_ID);
			convertStringToLong(journalpostId, JOURNALPOST_ID);
			convertStringToLong(dokumentId, DOKUMENT_ID);
			convertStringToLong(logiskVedleggId, LOGISK_VEDLEGG_ID);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalpostId=%s, dokumentinfoId=%s, logiskVedleggId=%s", e
					.getMessage(), journalpostId, dokumentId, logiskVedleggId));
		}
	}

	public static void validateJournalpostIdAndDokumentId(String journalpostId, String dokumentId) {
		try {
			hasText(journalpostId, JOURNALPOST_ID);
			hasText(dokumentId, DOKUMENT_ID);
			convertStringToLong(journalpostId, JOURNALPOST_ID);
			convertStringToLong(dokumentId, DOKUMENT_ID);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalpostId=%s, dokumentinfoId=%s", e.getMessage(), journalpostId, dokumentId));
		}
	}

	public static void validateId(String journalpostId, String feltnavn) {
		try {
			hasText(journalpostId, feltnavn);
			convertStringToLong(journalpostId, feltnavn);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(String.format("%s. journalpostId=%s", e.getMessage(), journalpostId));
		}
	}

	public static Long convertStringToLong(String input, String feltnavn) {
		try {
			return Long.parseLong(input);
		} catch (Exception e) {
			throw new IllegalArgumentException(String.format("%s er ikke et tall", feltnavn));
		}
	}

	public static void hasText(String input, String feltnavn) {
		if (StringUtils.isBlank(input)) {
			throw new IllegalArgumentException(String.format("%s kan ikke være null eller tom", feltnavn));
		}
	}

	public static void assertJournalpostIsInngaaende(Journalpost journalpost) {
		if (!journalpost.isInngaende()) {
			throw new JournalpostIkkeInngaaendeException(String.format("Journalpost med journalpostId=%s er ikke av type Inngaaende", journalpost
					.getJournalpostId()));
		}
	}

	public static void assertDokumentInfoNotNull(DokumentInfo dokumentInfo, String journalpostId, String dokumentId) {
		if (dokumentInfo == null) {
			throw new DokumentIkkeFunnetException(String.format("Fant ingen dokument med dokumentId=%s paa journalpost med journalpostId=%s", dokumentId, journalpostId));
		}
	}

	//Used for logging
	public static List<String> getDokumentIds(GetJournalpostResponse response) {
		return response.getDokumentListe().stream()
				.map(Dokument::getDokumentId)
				.collect(Collectors.toList());
	}

}
