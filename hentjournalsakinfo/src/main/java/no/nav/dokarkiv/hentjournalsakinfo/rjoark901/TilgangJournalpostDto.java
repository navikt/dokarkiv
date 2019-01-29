package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;


import lombok.Value;

import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class TilgangJournalpostDto {

	private final String journalpostId;
	private final String journalStatus;
	private final String journalpostType;
	private final String tema;
	private final LocalDateTime datoOpprettet;
	private final String mottakskanal;
	private final String avsenderMottakerId;
	private final TilgangBrukerDto bruker;
	private final TilgangSakDto sak;
	private final TilgangDokumentInfoDto dokument;
}
