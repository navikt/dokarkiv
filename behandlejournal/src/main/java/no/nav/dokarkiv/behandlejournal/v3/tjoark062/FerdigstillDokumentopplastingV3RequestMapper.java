package no.nav.dokarkiv.behandlejournal.v3.tjoark062;

import no.nav.dokarkiv.behandlejournal.SporingsMetaData;
import org.springframework.stereotype.Component;

@Component
public class FerdigstillDokumentopplastingV3RequestMapper {

	public FerdigstillDokumentopplastingRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.FerdigstillDokumentopplastingRequest wsRequest) {
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
