package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.DokumentStatusCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;

/**
 * Internal DTO for HentJournalpostInfoService responses
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class HentJournalpostInfoResponseTo {
	private JournalStatusCode journalStatus;
	private DokumentStatusCode dokumentStatus;
	private Long metaforceInstanceId;
	private String journalfEnhet;
	private FagomradeCode fagomrade;
	private String brukerId;
	private BrukerTypeCode brukerType;
	private String saksNummer;
	private FagsystemCode fagsystem;
	private Integer antallRetur;
}
