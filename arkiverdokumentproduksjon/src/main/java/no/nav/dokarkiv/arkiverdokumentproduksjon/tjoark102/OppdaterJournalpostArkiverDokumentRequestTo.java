package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;
import no.nav.dokarkiv.core.domain.entities.FilDetaljer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


@Builder
@Data
@AllArgsConstructor
public class OppdaterJournalpostArkiverDokumentRequestTo {

	private Long journalpostId;
	private Long dokumentInfoId;
	private UtsendingsKanalCode utsendingskanal;
	private String endretAvNavn;
	private LocalDateTime datoDokument;
	@ToString.Exclude
	@Builder.Default
	private Set<FilDetaljer> fildetaljerSet = new HashSet<>();
	private boolean ferdigstillJournalpost;

	public Set<FilDetaljer> getFildetaljer() {
		return Collections.unmodifiableSet(fildetaljerSet);
	}

	public void addFilDetaljer(FilDetaljer filDetaljer) {
		String fileSize = String.valueOf(filDetaljer.getFileContent().length);
		filDetaljer.setFilstorrelse(fileSize);
		fildetaljerSet.add(filDetaljer);
	}
}
