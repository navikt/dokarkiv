package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class JournalpostDto {
	private final Long journalpostId;
	private final Long prevJournalpostId;
	private final Long nextJournalpostId;
	private final Long totaltAntall;
	private final String journalForendeEnhetId;
	private final String innhold;
	private final FagomradeCode fagomrade;
	private final JournalStatusCode journalstatus;
	private final String avsenderMottakerNavn;
	private final String journalfortAvNavn;
	private final MottaksKanalCode mottakskanal;
	private final UtsendingsKanalCode utsendingskanal;
	private final JournalpostTypeCode journalposttype;
	private final SaksrelasjonDto saksrelasjon;
	private final Date datoOpprettet;
	private final Date mottattDato;
	private final Date journalDato;
	private final Date dokumentDato;
	private final Date avsReturDato;
	private final Date sendtPrintDato;
	private final Date ekspedertDato;
	private final SkjermingTypeCode skjerming;
	private final List<DokumentInfoDto> dokumenter;
}
