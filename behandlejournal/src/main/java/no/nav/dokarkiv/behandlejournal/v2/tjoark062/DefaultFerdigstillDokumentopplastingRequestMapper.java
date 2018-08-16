package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link FerdigstillDokumentopplastingRequestMapper}.
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
@Component
public class DefaultFerdigstillDokumentopplastingRequestMapper implements FerdigstillDokumentopplastingRequestMapper {

	@Override
	public FerdigstillDokumentopplastingRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest wsRequest) {
		return FerdigstillDokumentopplastingRequest.builder()
				.journalpostId(wsRequest.getJournalpostId() == null ? null : Long.parseLong(wsRequest.getJournalpostId()))
				.sporingsMetaData(SporingsMetaData.builder()
						.applikasjonsID(wsRequest.getApplikasjonsID())
						.personFornavn(wsRequest.getPersonFornavn())
						.personEtternavn(wsRequest.getPersonEtternavn())
						.build())
				.build();
	}
}
