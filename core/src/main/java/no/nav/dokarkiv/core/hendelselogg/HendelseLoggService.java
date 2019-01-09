package no.nav.dokarkiv.core.hendelselogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObject;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.domain.entities.Hendelselogg;
import no.nav.dokarkiv.core.exceptions.UgyldigHendelseLoggInfoException;
import no.nav.dokarkiv.core.repository.Hendelseloggrepository;
import org.jboss.logging.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class HendelseLoggService {

	public static final String HENDELSE_INFO_HEADER = "dok_hendelseinfo";

	private final Hendelseloggrepository hendelseloggrepository;
	private final HendelseLoggMapper hendelseLoggMapper;

	public HendelseLoggService(Hendelseloggrepository hendelseloggrepository) {
		this.hendelseloggrepository = hendelseloggrepository;
		this.hendelseLoggMapper = new HendelseLoggMapper();
	}

	public void validerOgLagreHendelse(String hendelseInfo) throws UgyldigHendelseLoggInfoException {

		if (isBlank(hendelseInfo)) {
			throw new UgyldigHendelseLoggInfoException(String.format("Meldingen mangler påkrevd %s header.", HENDELSE_INFO_HEADER));
		}

		try {
			HendelseLoggRequest hendelseLoggRequest = jsonStringToObject(hendelseInfo, HendelseLoggRequest.class);
			validateHendelselogg(hendelseLoggRequest);

			Hendelselogg hendelselogg = hendelseLoggMapper.mapToHendelseLogg(hendelseLoggRequest);
			String consumerId = (String) MDC.get(MDCConstants.MDC_CONSUMER_ID);
			hendelselogg.setApplikasjon(consumerId.substring(3));

			hendelseloggrepository.save(hendelselogg);
		} catch (IOException e) {
			throw new UgyldigHendelseLoggInfoException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", HENDELSE_INFO_HEADER), e);
		}

	}


	private void validateHendelselogg(HendelseLoggRequest hendelseLoggRequest) throws UgyldigHendelseLoggInfoException {

		if (Objects.isNull(hendelseLoggRequest.getDokumentInfoId()) && Objects.isNull(hendelseLoggRequest.getJournalpostId())) {
			throw new UgyldigHendelseLoggInfoException("Hendelselogg mangler både dokumentInfoId og journalpostId. Enten dokumentInfoId eller journalpostId må settes");
		}

		List<String> parameters = new ArrayList<>();
		addMessageWhenNullOrEmpty(hendelseLoggRequest.getApplikasjon(), "applikasjon", parameters);
		addMessageWhenNullOrEmpty(hendelseLoggRequest.getAksjon(), "aksjon", parameters);

		if (isFalse(parameters.isEmpty())) {
			throw new UgyldigHendelseLoggInfoException("Hendelselogg mangler påkrevde parametere: " + String.join(", ", parameters));
		}

	}

	private void addMessageWhenNullOrEmpty(Object value, String parameter, List<String> parameters) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			parameters.add(parameter);
		}
	}

}
