package no.nav.dokarkiv.inngaaendejournal.v1.tjoark056.to;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.domain.codes.MottaksKanalCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Builder
@Data
public class InngaaendeJournalpostTo {
	private String avsenderId;
	private LocalDateTime forsendelseMottatt;
	private MottaksKanalCode mottakskanal;
	private FagomradeCode tema;
	private String kanalReferanseId;
	@NonNull
	private final JournaltilstandTo journaltilstand;
	private String journalfEnhet;
	private ArkivSakTo arkivSak;
	private List<AktoerTo> brukere;
	@NonNull
	private final DokumentinformasjonTo hoveddokument;
	private List<DokumentinformasjonTo> vedlegg;
}
