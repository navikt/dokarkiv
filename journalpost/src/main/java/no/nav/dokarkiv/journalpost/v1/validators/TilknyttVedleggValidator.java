package no.nav.dokarkiv.journalpost.v1.validators;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.KanIkkeTilknytteVedleggException;

import java.util.EnumSet;

import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.FERDIGSTILT;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.D;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.E;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FL;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.FS;
import static no.nav.dokarkiv.core.domain.codes.JournalStatusCode.J;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.N;
import static no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode.U;

@Slf4j
public class TilknyttVedleggValidator {

	private static final EnumSet<JournalpostTypeCode> GYLDIGE_JOURNALPOSTTYPER = EnumSet.of(U, N);
	private static final EnumSet<JournalStatusCode> ORIGIN_JOURNALSTATUS_LIST = EnumSet.of(J, FL, FS, E);

	public void validateJournalpostStatus(Journalpost targetJournalpost) {
		if (!D.equals(targetJournalpost.getJournalstatus())) {
			throw new KanIkkeTilknytteVedleggException(String.format("Kan ikke legge til vedlegg på journalpostId=%s med journalstatus=%s, journalpost må ha journalstatus=D",
					targetJournalpost.getJournalpostId(), targetJournalpost.getJournalstatus()));
		}

		if (!GYLDIGE_JOURNALPOSTTYPER.contains(targetJournalpost.getJournalposttype())) {
			throw new KanIkkeTilknytteVedleggException(String.format("Kan ikke legge til vedlegg på journalpostId=%s med journalpostTypeCode=%s, journalpost må være en av typene %s",
					targetJournalpost.getJournalpostId(), targetJournalpost.getJournalposttype(), GYLDIGE_JOURNALPOSTTYPER));
		}
	}

	public boolean validateSourceJournalpost(Journalpost sourceJournalpost) {
		if (sourceJournalpost.getJournalstatus() == null) {
			return false;
		}
		JournalStatusCode statusCode = sourceJournalpost.getJournalstatus();
		return ORIGIN_JOURNALSTATUS_LIST.contains(statusCode);
	}

	public boolean validateSourceDokumentInfo(DokumentInfo dokumentInfo) {
		if (dokumentInfo.getDokumentstatus() != null && dokumentInfo.getDokumentstatus() != FERDIGSTILT) {
			return false;
		} else return !dokumentInfo.isKassert();
	}

}
