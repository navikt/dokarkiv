package no.nav.dokarkiv.behandlejournal.v3.tjoark062;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import no.nav.dokarkiv.core.exceptions.ApplicationException;

/**
 * Request object for the FerdigstillDokumentOpplasting operation.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Data
@Builder
@RequiredArgsConstructor
public class FerdigstillDokumentopplastingRequest {

	private final Long journalpostId;
	private final SporingsMetaData sporingsMetaData;

	/**
	 * Check that journalpostId is set. If not, throw {@link ApplicationException}.
	 */
	public void validate() {
		if (journalpostId == null) {
			throw new ApplicationException("Missing parameter: journalpostId");
		}
		if (sporingsMetaData == null) {
			throw new ApplicationException("Missing parameter: sporingsMetaData");
		}
	}
}
