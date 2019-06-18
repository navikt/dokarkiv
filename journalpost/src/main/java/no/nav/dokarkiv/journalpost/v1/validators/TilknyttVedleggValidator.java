package no.nav.dokarkiv.journalpost.v1.validators;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;

import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;
import no.nav.dokarkiv.journalpost.v1.api.JournalpostType;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public class TilknyttVedleggValidator {

	private static final List<JournalStatusCode> ORIGIN_JOURNALSTATUS_LIST = Arrays.asList(J, FL, FS, E);

	public TilknyttVedleggValidator() {
	}

	public void validateJournalpostStatus(Journalpost journalpost) {
		if (journalpost.getJournalstatus().equals(D) || journalpost.getJournalposttype().equals(JournalpostType.UTGAAENDE)) {
			throw new KanIkkeTilknytteVedleggException(String.format("Kan ikke legge til vedlegg på journalpost med journalpostId=%s, journalpost har ugyldig status", journalpost
					.getJournalpostId()));
		}
	}

	public Boolean validateOriginJournalpostStatus(Journalpost journalpost) {
		JournalStatusCode statusCode = journalpost.getJournalstatus();
		if (!ORIGIN_JOURNALSTATUS_LIST.contains(statusCode)) {
			return false;
		} else {
			return true;
		}
	}

	public Boolean validateDokumentInfo(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getDokumentstatus() == null || dokumentInfo.getDokumentstatus() != DokumentStatusCode.FERDIGSTILT) {
			return false;
		} else if (dokumentInfo.getOrganInternt() != null && dokumentInfo.getOrganInternt() == true || dokumentInfo.isKassert() == true) {
			return false;
		} else {
			return true;
		}

	}

}
