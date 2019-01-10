package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggMapper {


	public AksjonsLogg mapToHendelseLogg(AksjonsLoggRequest aksjonsLoggRequest) {
		return AksjonsLogg.builder()
				.aksjon(aksjonsLoggRequest.getAksjon())
				.applikasjon(aksjonsLoggRequest.getApplikasjon())
				.bruker(aksjonsLoggRequest.getBruker())
				.dokumentInfoId(aksjonsLoggRequest.getDokumentInfoId())
				.journalpostId(aksjonsLoggRequest.getJournalpostId())
				.hjemmel(aksjonsLoggRequest.getHjemmel())
				.melding(aksjonsLoggRequest.getMelding())
				.utfoertAv(aksjonsLoggRequest.getUtfoertAv())
				.build();
	}

}
