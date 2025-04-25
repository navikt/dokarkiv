package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SettJournalpostAttributterRequestTo {
	private List<Long> journalpostIds;
	private String endretAvNavn;
	private LocalDateTime datoSendtPrint;
	private Integer antallRetur;
	private UtsendingsKanalCode utsendingskanal;
}
