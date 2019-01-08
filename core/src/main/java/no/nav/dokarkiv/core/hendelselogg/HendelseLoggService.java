package no.nav.dokarkiv.core.hendelselogg;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Hendelselogg;
import no.nav.dokarkiv.core.exceptions.UgyldigHendelseLoggInfoException;
import no.nav.dokarkiv.core.repository.Hendelseloggrepository;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class HendelseLoggService {

	public static final String HENDELSE_INFO_HEADER = "dok_hendelseinfo";

	private final Hendelseloggrepository hendelseloggrepository;

	public HendelseLoggService(Hendelseloggrepository hendelseloggrepository) {
		this.hendelseloggrepository = hendelseloggrepository;
	}

	public void lagreHendelse(String hendelseInfo) throws UgyldigHendelseLoggInfoException {

		if (isBlank(hendelseInfo)) {
			return;
		}

		Hendelselogg hendelselogg = convertToHendelseLoggObject(hendelseInfo);
		String consumerId = (String) MDC.get(MDCConstants.MDC_CONSUMER_ID);
		hendelselogg.setOpprettetAvTjeneste(consumerId);

		//TODO: Legg til validering

		hendelseloggrepository.save(hendelselogg);

	}


	private Hendelselogg convertToHendelseLoggObject(String hendelseInfoHeader) throws UgyldigHendelseLoggInfoException {
		ObjectMapper mapper = new ObjectMapper();

		try {
			return mapper.readValue(hendelseInfoHeader, Hendelselogg.class);
		} catch (IOException e) {
			throw new UgyldigHendelseLoggInfoException(String.format("Kunne ikke lese hendelse %s header", HENDELSE_INFO_HEADER), e);
		}
	}

}
