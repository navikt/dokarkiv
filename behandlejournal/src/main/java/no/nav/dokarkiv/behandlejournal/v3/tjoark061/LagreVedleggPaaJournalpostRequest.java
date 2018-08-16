package no.nav.dokarkiv.behandlejournal.v3.tjoark061;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Domain request object for service LagreVedleggPaaJournalpost.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Data
@Builder
@RequiredArgsConstructor
public class LagreVedleggPaaJournalpostRequest {

	private final Long journalpostId;
	private final DokumentInfo dokumentInfo;
	private final SporingsMetaData sporingsMetaData;

	/**
	 * Validate request.
	 */
	public void validate() {
		if (journalpostId == null) {
			throw new ApplicationException("Missing parameter in request: journalpostId");
		}
		if (dokumentInfo == null) {
			throw new ApplicationException("Missing parameter in request: dokumentInfo");
		}
		if (sporingsMetaData == null) {
			throw new ApplicationException("Missing parameter in request: sporingsMetaData");
		}
	}
}
