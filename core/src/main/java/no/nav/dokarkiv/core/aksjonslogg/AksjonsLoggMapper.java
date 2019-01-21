package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.stringToEnum;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class AksjonsLoggMapper {


	public AksjonsLogg mapToAksjonsLogg(@NotNull Long journalpostId,
										@NotNull String applikasjon,
										@NotNull String aksjon,
										@NotNull String utfoertAv,
										Long dokumentInfoId,
										String hjemmel,
										String bruker,
										String arkivElement,
										String fraVerdi,
										String tilVerdi,
										String melding) {
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();

		return AksjonsLogg.builder()
				.tidspunkt(LocalDateTime.now())
				.aksjon(stringToEnum(AksjonTypeCode.class, aksjon))
				.applikasjon(applikasjon)
				.bruker(bruker)
				.arkivElement(arkivElement)
				.fraVerdi(fraVerdi)
				.tilVerdi(tilVerdi)
				.dokumentInfoId(dokumentInfoId)
				.journalpostId(journalpostId)
				.hjemmel(hjemmel)
				.melding(melding)
				.utfoertAv(utfoertAv)
				.opprettetAv(componentId)
				.build();
	}

}
