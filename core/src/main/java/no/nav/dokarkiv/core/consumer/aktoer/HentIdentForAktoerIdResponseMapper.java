package no.nav.dokarkiv.core.consumer.aktoer;

import static no.nav.dokarkiv.core.util.DateConverterUtil.convertXMLGregorianCalendarToDate;

import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.IdentDetaljer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for HentAktoerIdForIdentResponse
 *
 * @author Ketill Fenne, Visma Consulting.
 */
@Component
public class HentIdentForAktoerIdResponseMapper {

	/**
	 * Maps from ws-object {@link HentIdentForAktoerIdResponse} to domain object {@link HentIdentForAktoerIdResponseTo}
	 *
	 * @param response The ws-response to map
	 * @return The domain object
	 */
	public HentIdentForAktoerIdResponseTo map(HentIdentForAktoerIdResponse response) {
		return new HentIdentForAktoerIdResponseTo(response.getIdent());
	}
}
