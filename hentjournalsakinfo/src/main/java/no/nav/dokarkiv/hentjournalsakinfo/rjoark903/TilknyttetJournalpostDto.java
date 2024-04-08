package no.nav.dokarkiv.hentjournalsakinfo.rjoark903;

import lombok.AllArgsConstructor;
import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.Innsyn;
import no.nav.dokarkiv.core.domain.codes.InnsynDto;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.BrukerDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilleggsopplysningDto;
import no.nav.dokarkiv.hentjournalsakinfo.dto.UtsendingsInfoDto;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@AllArgsConstructor
public class TilknyttetJournalpostDto {
	private Long journalpostId;
	private String innhold;
	private FagomradeCode fagomrade;
	private String behandlingstema;
	private String behandlingstemanavn;
	private JournalStatusCode journalstatus;
	private String avsenderMottakerId;
	private AvsenderMottakerIdTypeCode avsenderMottakerIdType;
	private String avsenderMottakerNavn;
	private String avsenderMottakerLand;
	private String journalforendeEnhet;
	private String journalfortAvNavn;
	private String opprettetAvNavn;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private JournalpostTypeCode journalposttype;
	private SaksrelasjonDto saksrelasjon;
	private BrukerDto bruker;
	private Date datoOpprettet;
	private Date mottattDato;
	private Date journalDato;
	private Date dokumentDato;
	private Date avsReturDato;
	private Date sendtPrintDato;
	private Date ekspedertDato;
	private Date lestDato;
	private SkjermingTypeCode skjerming;
	private String antallRetur;
	private String kanalReferanseId;
	private List<TilleggsopplysningDto> tilleggsopplysninger;
	private List<DokumentInfoDto> dokumenter;
	private InnsynDto innsyn;
	private UtsendingsInfoDto utsendingsInfo;
}
