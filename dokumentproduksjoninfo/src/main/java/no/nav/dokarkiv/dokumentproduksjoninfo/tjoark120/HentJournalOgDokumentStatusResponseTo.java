package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120;

import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

/**
 * Response object for DokumentproduksjonInfo.hentJournalOgDokumentStatus.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Data
public class HentJournalOgDokumentStatusResponseTo {

	private final JournalStatusCode journalStatus;
	private final DokumentStatusCode dokumentStatus;
	private final Long metaforceInstanceId;
}
