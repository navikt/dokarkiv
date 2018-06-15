package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import no.nav.domain.dok.joark.FilDetaljer;
import no.nav.domain.dok.joark.codestable.UtsendingsKanalCode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Hans Petter Simonsen - Visma Consulting AS
 */
public final class OppdaterJournalpostArkiverDokumentRequestToBuilder {
	private Long journalpostId;
	private Long dokumentInfoId;
	private UtsendingsKanalCode utsendingskanal;
	private String endretAvNavn;
	private Set<FilDetaljer> fildetaljerSet = new HashSet<>();
    private boolean ferdigstillJournalpost;

	private OppdaterJournalpostArkiverDokumentRequestToBuilder() {
	}

	public static OppdaterJournalpostArkiverDokumentRequestToBuilder getOppdaterJournalpostArkiverDokumentRequestToBuilder() {
		return new OppdaterJournalpostArkiverDokumentRequestToBuilder();
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder journalpostId(Long journalpostId) {
		this.journalpostId = journalpostId;
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder dokumentInfoId(Long dokumentInfoId) {
		this.dokumentInfoId = dokumentInfoId;
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder utsendingskanal(UtsendingsKanalCode utsendingskanal) {
		this.utsendingskanal = utsendingskanal;
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder endretAvNavn(String endretAvNavn) {
		this.endretAvNavn = endretAvNavn;
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder fildetalj(FilDetaljer filDetalj) {
		this.fildetaljerSet.add(filDetalj);
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestToBuilder ferdigstillJournalpost(boolean ferdigstillJournalpost) {
		this.ferdigstillJournalpost = ferdigstillJournalpost;
		return this;
	}

	public OppdaterJournalpostArkiverDokumentRequestTo build() {
		OppdaterJournalpostArkiverDokumentRequestTo oppdaterJournalpostArkiverDokumentRequestTo
				= new OppdaterJournalpostArkiverDokumentRequestTo();
		oppdaterJournalpostArkiverDokumentRequestTo.setJournalpostId(journalpostId);
		oppdaterJournalpostArkiverDokumentRequestTo.setDokumentInfoId(dokumentInfoId);
		oppdaterJournalpostArkiverDokumentRequestTo.setUtsendingskanal(utsendingskanal);
		oppdaterJournalpostArkiverDokumentRequestTo.setEndretAvNavn(endretAvNavn);
		oppdaterJournalpostArkiverDokumentRequestTo.getFildetaljer().addAll(fildetaljerSet);
		oppdaterJournalpostArkiverDokumentRequestTo.setFerdigstillJournalpost(ferdigstillJournalpost);
		return oppdaterJournalpostArkiverDokumentRequestTo;
	}
}
