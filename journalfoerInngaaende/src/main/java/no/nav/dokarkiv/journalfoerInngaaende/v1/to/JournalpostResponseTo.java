package no.nav.dokarkiv.journalfoerInngaaende.v1.to;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class JournalpostResponseTo {

	private String journaltilstand;
	private AvsenderTo avsender;
	private List<BrukerTo> brukere;
	private ArkivsakTo arkivsak;
	private String tema;
	private String tittel;
	private String kanalreferanseId;
	private LocalDateTime forsendelseMottatt;
	private String mottakskanal;
	private String journalfoerendeEnhet;
	private List<DokumentTo> dokumenter;
}
