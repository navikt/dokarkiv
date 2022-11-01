package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;


import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.InnsynCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

import java.time.LocalDateTime;

@Value
public class TilgangJournalpostDto {
	String journalpostId;
	JournalStatusCode journalStatus;
	JournalpostTypeCode journalpostType;
	FagomradeCode fagomrade;
	LocalDateTime datoOpprettet;
	LocalDateTime journalfoertDato;
	MottaksKanalCode mottakskanal;
	SkjermingTypeCode skjerming;
	String avsenderMottakerId;
	TilgangBrukerDto bruker;
	TilgangSakDto sak;
	TilgangDokumentInfoDto dokument;
	InnsynCode innsyn;
}
