package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggTOMapper {

	public AksjonsLoggTO mapAksjonsLoggTo(String melding, String bruker, String utfoertAv, String hjemmel, AksjonsTypeCode aksjon, Long journalpostId, Long dokumentInfoId) throws UgyldigAksjonsLoggException {

		return AksjonsLoggTO.builder()
				.aksjon(aksjon)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.melding(melding)
				.bruker(bruker)
				.hjemmel(hjemmel)
				.utfoertAv(utfoertAv)
				.build();


	}
}
