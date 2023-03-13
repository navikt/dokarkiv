package no.nav.dokarkiv.hentjournalsakinfo.rjoark902;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import no.nav.dokarkiv.core.domain.codes.AvsenderMottakerIdTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
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

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SafHentJournalpostResponseForTest (HentJournalpostDtoForTest hentJournalpostDto) {

	record HentJournalpostDtoForTest (
			Long journalpostId,
			String innhold,
			FagomradeCode fagomrade,
			String behandlingstema,
			String behandlingstemanavn,
			JournalStatusCode journalstatus,
			String avsenderMottakerId,
			AvsenderMottakerIdTypeCode avsenderMottakerIdType,
			String avsenderMottakerNavn,
			String avsenderMottakerLand,
			String journalforendeEnhet,
			String journalfortAvNavn,
			String opprettetAvNavn,
			MottaksKanalCode mottakskanal,
			UtsendingsKanalCode utsendingskanal,
			JournalpostTypeCode journalposttype,
			SaksrelasjonDto saksrelasjon,
			BrukerDto bruker,
			Date datoOpprettet,
			Date mottattDato,
			Date journalDato,
			Date dokumentDato,
			Date avsReturDato,
			Date sendtPrintDato,
			Date ekspedertDato,
			Date lestDato,
			SkjermingTypeCode skjerming,
			String antallRetur,
			String kanalReferanseId,
			List<TilleggsopplysningDto> tilleggsopplysninger,
			List<DokumentInfoDto> dokumenter,
			InnsynCode innsyn,
			UtsendingsInfoDtoForTest utsendingsInfo
	) {}

	record UtsendingsInfoDtoForTest (
			UtsendingsInfoDto.FysiskPostadresse fysiskPostadresse,
			UtsendingsInfoDto.DigitalPostadresse digitalPostadresse,
			UtsendingsInfoDto.NavNoVarsling navNoVarsling,
			List<Varsel> epostVarsel,
			List<Varsel> smsVarsel
	) {}

	record Varsel (
			String tittel,
			String tekst,
			String epostadresse,
			String mobilnummer,
			LocalDateTime varslingstidspunkt
	) {}
}
