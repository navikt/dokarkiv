package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.stringToEnum;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggMapper {


	public AksjonsLogg mapToAksjonsLogg(AksjonsLoggRequest.Aksjon aksjon) {
		return AksjonsLogg.builder()
				.aksjon(stringToEnum(AksjonTypeCode.class, aksjon.getAksjon()))
				.applikasjon(aksjon.getApplikasjon())
				.bruker(aksjon.getBruker())
				.arkivElement(aksjon.getArkivElement())
				.fraVerdi(aksjon.getFraVerdi())
				.tilVerdi(aksjon.getTilVerdi())
				.dokumentInfoId(aksjon.getDokumentInfoId())
				.journalpostId(aksjon.getJournalpostId())
				.hjemmel(aksjon.getHjemmel())
				.melding(aksjon.getMelding())
				.utfoertAv(aksjon.getUtfoertAv())
				.build();
	}

}
