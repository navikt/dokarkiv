package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * Request object for the operation OppdaterJournalpostArkiverDokument
 *
 * @author Torgeir Cook
 */
@Builder
@Data
@AllArgsConstructor
public class OppdaterJournalpostArkiverDokumentRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private UtsendingsKanalCode utsendingskanal;
	private String endretAvNavn;
	private Date datoDokument;
	@ToString.Exclude
	@Builder.Default
	private Set<FilDetaljer> fildetaljerSet = new HashSet<>();
	private boolean ferdigstillJournalpost;

	public OppdaterJournalpostArkiverDokumentRequestTo() {
	}

	public Date getDatoDokument() {
		return datoDokument != null ? (Date) datoDokument.clone() : null;
	}

	public void setDatoDokument(Date datoDokument) {
		this.datoDokument = datoDokument != null ? new Date(datoDokument.getTime()) : null;
	}

	public Set<FilDetaljer> getFildetaljer() {
		return Collections.unmodifiableSet(fildetaljerSet);
	}

	public void addFilDetaljer(FilDetaljer filDetaljer) {
		String fileSize = String.valueOf(filDetaljer.getFileContent().length);
		filDetaljer.setFilstorrelse(fileSize);
		fildetaljerSet.add(filDetaljer);
	}
}
