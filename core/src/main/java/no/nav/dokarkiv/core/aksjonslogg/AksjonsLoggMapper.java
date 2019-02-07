package no.nav.dokarkiv.core.aksjonslogg;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.ArkivElementEndring;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.apache.logging.log4j.util.Strings;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
class AksjonsLoggMapper {


	public AksjonsLogg mapToAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList) {
		String componentId = RequestContextHolder.currentRequestContext().getComponentId();
		String userId = RequestContextHolder.currentRequestContext().getUserId();

		String utfoertAv = Strings.isEmpty(aksjonsLoggTO.getUtfoertAv()) ? userId : aksjonsLoggTO.getUtfoertAv();

		AksjonsLogg aksjonsLogg = AksjonsLogg.builder()
				.tidspunkt(LocalDateTime.now())
				.aksjon(aksjonsLoggTO.getAksjon())
				.applikasjon(componentId)
				.bruker(aksjonsLoggTO.getBruker())
				.dokumentInfoId(aksjonsLoggTO.getDokumentInfoId())
				.journalpostId(aksjonsLoggTO.getJournalpostId())
				.hjemmel(aksjonsLoggTO.getHjemmel())
				.melding(aksjonsLoggTO.getMelding())
				.utfoertAv(utfoertAv)
				.arkivElementEndringer(mapArkivElementEndring(arkivElementEndringTOList))
				.build();

		aksjonsLogg.getArkivElementEndringer().forEach(arkivElementEndring -> arkivElementEndring.setAksjonsLogg(aksjonsLogg));
		return aksjonsLogg;
	}

	private Set<ArkivElementEndring> mapArkivElementEndring(List<ArkivElementEndringTO> arkivElementEndringTOList) {
		return arkivElementEndringTOList.stream()
				.map(arkivElementEndringTO -> ArkivElementEndring.builder()
						.arkivElement(arkivElementEndringTO.getArkivElement())
						.fraVerdi(arkivElementEndringTO.getFraVerdi())
						.tilVerdi(arkivElementEndringTO.getTilVerdi())
						.tidspunkt(LocalDateTime.now())
						.build()
				).collect(Collectors.toSet());
	}

}
