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

//	public JournalpostDto(Journalpost journalpost) {
//		this.journalpostId = journalpost.getJournalpostId();
//		this.journalForendeEnhetId = journalpost.getJournalForendeEnhetId();
//		this.journalDato = journalpost.getJournalDato();
//		this.sendtPrintDato = journalpost.getSendtPrintDato();
//		this.innhold = journalpost.getInnhold();
//		this.dokumentDato = journalpost.getDokumentDato();
//		this.journalfortAvNavn = journalpost.getJournalfortAvNavn();
//		this.mottattDato = journalpost.getMottattDato();
//		this.utsendingskanal = journalpost.getUtsendingskanal();
//		this.ekspedertDato = journalpost.getEkspedertDato();
//		this.lestDato = journalpost.getLestDato();
//		this.mottattAdressatDato = journalpost.getMottattAdressatDato();
//		this.journalstatus = journalpost.getJournalstatus();
//		this.journalposttype = journalpost.getJournalposttype();
//		this.fagomrade = journalpost.getFagomrade();
//		this.datoOpprettet = journalpost.getChangeStamp().getCreatedDate();
//		this.mottakskanal = journalpost.getMottakskanal();
//		this.avsenderMottakerNavn = journalpost.getAvsenderMottaker();
//		Saksrelasjon saksrelasjon = journalpost.getSaksrelasjon();
//		this.saksrelasjon = saksrelasjon == null ?
//				null : SaksrelasjonDto.builder()
//				.sakId(saksrelasjon.getSakId())
//				.fagsystem(saksrelasjon.getFagsystem())
//				.feilregistrert(journalpost.isFeilregistrert())
//				.build();
//		this.dokumenter = journalpost.getJournalpostDokumentInfoRelasjoner()
//				.stream().map(jprel -> new DokumentInfoDto(jprel.getDokumentInfo()))
//				.collect(Collectors.toList());
//	}
}
