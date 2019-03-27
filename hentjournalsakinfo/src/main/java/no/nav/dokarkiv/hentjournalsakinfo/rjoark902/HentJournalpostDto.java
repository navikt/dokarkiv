package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;

import lombok.AllArgsConstructor;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.Behandlingstema;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.BrukerDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilleggsopplysningDto;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@AllArgsConstructor
public class HentJournalpostDto {

	private final Long journalpostId;
	private final String innhold;
	private final FagomradeCode fagomrade;
	private final Behandlingstema behandlingstema;
	private final String behandlingstemanavn;
	private final JournalStatusCode journalstatus;
	private final String avsenderMottakerId;
	private final String avsenderMottakerNavn;
	private final String avsenderMottakerLand;
	private final String journalforendeEnhet;
	private final String journalfortAvNavn;
	private final String opprettetAvNavn;
	private final MottaksKanalCode mottakskanal;
	private final UtsendingsKanalCode utsendingskanal;
	private final JournalpostTypeCode journalposttype;
	private final SaksrelasjonDto saksrelasjon;
	private final BrukerDto bruker;
	private final Date datoOpprettet;
	private final Date mottattDato;
	private final Date journalDato;
	private final Date dokumentDato;
	private final Date avsReturDato;
	private final Date sendtPrintDato;
	private final Date ekspedertDato;
	private final SkjermingTypeCode skjerming;
	private final List<TilleggsopplysningDto> tilleggsopplysninger;
	private final List<DokumentInfoDto> dokumenter;
}
