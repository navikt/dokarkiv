package no.nav.dokarkiv.core.consumer.aktoer;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for HentAktoerIdForIdentResponse
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Component
public class HentAktoerIdForIdentResponseMapper {

	/**
	 * Maps from ws-object {@link HentAktoerIdForIdentResponse} to domain object {@link HentAktoerIdForIdentResponseTo}
	 *
	 * @param response The ws-response to map
	 * @return The domain object
	 */
	public HentAktoerIdForIdentResponseTo map(HentAktoerIdForIdentResponse response) {
		List<IdentDetaljerTo> historiskeIdenter = response.getIdentHistorikk().stream().map(this::mapIdentDetaljerTo).collect(Collectors.toList());
		return new HentAktoerIdForIdentResponseTo(response.getAktoerId(), historiskeIdenter);
	}

	private IdentDetaljerTo mapIdentDetaljerTo(IdentDetaljer identDetaljer) {
		return new IdentDetaljerTo(identDetaljer.getTpsId(), convertXMLGregorianCalendarToDate(identDetaljer.getDatoFom()));
	}

}
