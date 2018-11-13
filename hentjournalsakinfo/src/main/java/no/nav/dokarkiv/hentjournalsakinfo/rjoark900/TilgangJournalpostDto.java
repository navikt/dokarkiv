package no.nav.dokarkiv.hentjournalsakinfo.rjoark900;

import lombok.Value;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangJournalpostDto {
	private final String journalpostId;
	private final JournalStatusCode journalStatus;
	private final JournalpostTypeCode journalpostType;
	private final FagomradeCode fagomrade;
	private final Date datoOpprettet;
	private final MottaksKanalCode mottakskanal;
	private final String avsenderMottakerId;
	private final List<TilgangDokumentInfoDto> dokumenter;

	public TilgangJournalpostDto(Journalpost journalpost) {
		this.journalpostId = journalpost.getJournalpostId().toString();
		this.journalStatus = journalpost.getJournalstatus();
		this.journalpostType = journalpost.getJournalposttype();
		this.fagomrade = journalpost.getFagomrade();
		this.datoOpprettet = journalpost.getChangeStamp().getCreatedDate();
		this.mottakskanal = journalpost.getMottakskanal();
		this.avsenderMottakerId = journalpost.getAvsenderMottakerId();
		this.dokumenter = journalpost.getJournalpostDokumentInfoRelasjoner()
				.stream().map(jprel -> new TilgangDokumentInfoDto(jprel.getDokumentInfo()))
				.collect(Collectors.toList());
	}
}
