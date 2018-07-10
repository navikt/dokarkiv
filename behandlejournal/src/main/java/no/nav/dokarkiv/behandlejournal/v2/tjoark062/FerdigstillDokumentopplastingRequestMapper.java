package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

/**
 * Mapper for FerdigstillDokumentopplastingRequest from WS to domain.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
public interface FerdigstillDokumentopplastingRequestMapper {

	/**
	 * Maps a FerdigstillDokumentopplastingRequest from WS to domain.
	 * 
	 * @param wsRequest The WS request
	 * @return The domain request
	 */
	FerdigstillDokumentopplastingRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest wsRequest);
}
