package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;


import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.codes.SkjermingTypeCode;

import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class TilgangJournalpostDto {
	private final String journalpostId;
	private final JournalStatusCode journalStatus;
	private final JournalpostTypeCode journalpostType;
	private final FagomradeCode fagomrade;
	private final LocalDateTime datoOpprettet;
	private final LocalDateTime journalfoertDato;
	private final MottaksKanalCode mottakskanal;
	private final SkjermingTypeCode skjerming;
	private final String avsenderMottakerId;
	private final TilgangBrukerDto bruker;
	private final TilgangSakDto sak;
	private final TilgangDokumentInfoDto dokument;
}
