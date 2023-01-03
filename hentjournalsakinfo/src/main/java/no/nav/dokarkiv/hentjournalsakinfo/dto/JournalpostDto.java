package no.nav.dokarkiv.hentjournalsakinfo.dto;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;

import java.util.Date;
import java.util.List;

@Value
public class JournalpostDto {
	Long journalpostId;
	Long prevJournalpostId;
	Long nextJournalpostId;
	Long totaltAntall;
	String innhold;
	FagomradeCode fagomrade;
	String behandlingstema;
	String behandlingstemanavn;
	JournalStatusCode journalstatus;
	String avsenderMottakerId;
	AvsenderMottakerIdTypeCode avsenderMottakerIdType;
	String avsenderMottakerNavn;
	String avsenderMottakerLand;
	String journalforendeEnhet;
	String journalfortAvNavn;
	String opprettetAvNavn;
	MottaksKanalCode mottakskanal;
	UtsendingsKanalCode utsendingskanal;
	JournalpostTypeCode journalposttype;
	SaksrelasjonDto saksrelasjon;
	BrukerDto bruker;
	Date datoOpprettet;
	Date mottattDato;
	Date journalDato;
	Date dokumentDato;
	Date avsReturDato;
	Date sendtPrintDato;
	Date ekspedertDato;
	Date lestDato;
	SkjermingTypeCode skjerming;
	String antallRetur;
	String kanalReferanseId;
	List<TilleggsopplysningDto> tilleggsopplysninger;
	List<DokumentInfoDto> dokumenter;
	InnsynCode innsyn;
	UtsendingsInfoDto utsendingsInfo;
}
