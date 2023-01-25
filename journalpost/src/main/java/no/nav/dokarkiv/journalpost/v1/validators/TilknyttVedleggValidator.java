package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;

import java.util.Arrays;
import java.util.List;

import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
@Slf4j
public class TilknyttVedleggValidator {

	private static final List<JournalStatusCode> ORIGIN_JOURNALSTATUS_LIST = Arrays.asList(J, FL, FS, E);

	public void validateJournalpostStatus(Journalpost targetJournalpost) {
		if (!D.equals(targetJournalpost.getJournalstatus())) {
			throw new KanIkkeTilknytteVedleggException(String.format("Kan ikke legge til vedlegg på journalpostId=%s med journalstatus=%s, journalpost må ha journalstatus=D", targetJournalpost
					.getJournalpostId(), targetJournalpost.getJournalstatus()));
		} else if (!targetJournalpost.getJournalposttype().equals(JournalpostTypeCode.U)) {
			throw new KanIkkeTilknytteVedleggException(String.format("Kan ikke legge til vedlegg på journalpostId=%s med journalpostTypeCode=%s, journalpost må være av type U", targetJournalpost
					.getJournalpostId(), targetJournalpost.getJournalposttype()));
		}
	}

	public boolean validateSourceJournalpostStatus(Journalpost sourceJournalpost) {
		JournalStatusCode statusCode = sourceJournalpost.getJournalstatus();
		return ORIGIN_JOURNALSTATUS_LIST.contains(statusCode);
	}

	public boolean validateDokumentInfo(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getDokumentstatus() != null && dokumentInfo.getDokumentstatus() != DokumentStatusCode.FERDIGSTILT) {
			return false;
		} else if (dokumentInfo.isKassert()) {
			return false;
		} else {
			return true;
		}

	}

}
