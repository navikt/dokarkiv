package no.nav.dokarkiv.core.consumer.aktoer;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;

/**
 * Mapper for HentAktoerIdForIdentResponse
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public class HentAktoerIdForIdentResponseMapper {

	/**
	 * Maps from ws-object {@link HentAktoerIdForIdentResponse} to domain object {@link HentAktoerIdForIdentResponseTo}
	 *
	 * @param response The ws-response to map
	 * @return The domain object
	 */
	public HentAktoerIdForIdentResponseTo map(HentAktoerIdForIdentResponse response) {
		HentAktoerIdForIdentResponseTo responseTo = new HentAktoerIdForIdentResponseTo();
		responseTo.setAktoerId(response.getAktoerId());
		for (IdentDetaljer identDetaljer : response.getIdentHistorikk()) {
			responseTo.getHistoriskeIdenter().add(mapIdentDetaljerTo(identDetaljer));
		}
		return responseTo;
	}

	private IdentDetaljerTo mapIdentDetaljerTo(IdentDetaljer identDetaljer) {
		return new IdentDetaljerTo(identDetaljer.getTpsId(), convertXMLGregorianCalendarToDate(identDetaljer.getDatoFom()));
	}

}
