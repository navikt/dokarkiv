package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark910.SaksrelasjonDto;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class JournalpostDto {
	private final Long journalpostId;
	private final String journalForendeEnhetId;
	private final Date journalDato;
	private final Date sendtPrintDato;
	private final String innhold;
	private final FagomradeCode fagomrade;
	private final JournalStatusCode journalstatus;
	private final Date dokumentDato;
	private final String avsenderMottakerNavn;
	private final String journalfortAvNavn;
	private final Date mottattDato;
	private final MottaksKanalCode mottakskanal;
	private final UtsendingsKanalCode utsendingskanal;
	private final Date ekspedertDato;
	private final Date lestDato;
	private final Date mottattAdressatDato;
	private final JournalpostTypeCode journalposttype;
	private final Date datoOpprettet;
	private final SaksrelasjonDto saksrelasjon;
	private List<DokumentInfoDto> dokumenter;
}
