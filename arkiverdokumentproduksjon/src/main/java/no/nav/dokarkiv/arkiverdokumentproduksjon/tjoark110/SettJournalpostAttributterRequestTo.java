package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110;

import lombok.Builder;
import lombok.Data;
import no.nav.dokarkiv.core.domain.codes.UtsendingsKanalCode;

import java.util.Date;
import java.util.List;

/**
 * Domain transfer object for settJournalpostAttributter
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class SettJournalpostAttributterRequestTo {
	private List<Long> journalpostIds;
	private String endretAvNavn;
	private Date datoSendtPrint;
	private Integer antallRetur;
	private UtsendingsKanalCode utsendingskanal;
}
