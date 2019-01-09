package no.nav.dokarkiv.core.hendelselogg;

import no.nav.dokarkiv.core.domain.entities.Hendelselogg;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class HendelseLoggMapper {


	public Hendelselogg mapToHendelseLogg(HendelseLoggRequest hendelseLoggRequest) {
		return Hendelselogg.builder()
				.aksjon(hendelseLoggRequest.getAksjon())
				.applikasjon(hendelseLoggRequest.getApplikasjon())
				.bruker(hendelseLoggRequest.getBruker())
				.dokumentInfoId(hendelseLoggRequest.getDokumentInfoId())
				.journalpostId(hendelseLoggRequest.getJournalpostId())
				.hjemmel(hendelseLoggRequest.getHjemmel())
				.melding(hendelseLoggRequest.getMelding())
				.sak(hendelseLoggRequest.getSak())
				.build();
	}

}
