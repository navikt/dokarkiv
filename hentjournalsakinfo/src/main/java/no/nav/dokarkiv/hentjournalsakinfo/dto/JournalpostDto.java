package no.nav.dokarkiv.hentjournalsakinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class JournalpostDto {
	Long journalpostId;
	Long prevJournalpostId;
	Long nextJournalpostId;
	Long totaltAntall;
	String innhold;
	String fagomrade;
	String behandlingstema;
	String behandlingstemanavn;
	String journalstatus;
	String avsenderMottakerId;
	String avsenderMottakerIdType;
	String avsenderMottakerNavn;
	String avsenderMottakerLand;
	String journalforendeEnhet;
	String journalfortAvNavn;
	String opprettetAvNavn;
	String mottakskanal;
	String utsendingskanal;
	String journalposttype;
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
	String skjerming;
	String antallRetur;
	String kanalReferanseId;
	List<TilleggsopplysningDto> tilleggsopplysninger;
	List<DokumentInfoDto> dokumenter;
	String innsyn;
	String innsynbeskrivelse;
	UtsendingsInfoDto utsendingsInfo;

	@JsonIgnore
	public DokumentInfoDto findDokumentInfoDto(long dokumentInfoId) {
		return dokumenter.stream()
				.filter(dokumentInfoDto -> dokumentInfoId == dokumentInfoDto.getDokumentInfoId())
				.findFirst().orElse(null);
	}
}
