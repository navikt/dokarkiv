package no.nav.dokarkiv.core.aksjonslogg;

import static no.nav.dokarkiv.core.util.ConverterUtils.jsonStringToObject;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.exceptions.UgyldigHendelseLoggInfoException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AksjonsLoggService {

	public static final String AKSJONS_INFO_HEADER = "dok_aksjonsinfo";

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggMapper aksjonsLoggMapper;

	public AksjonsLoggService(AksjonsLoggRepository aksjonsLoggRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggMapper = new AksjonsLoggMapper();
	}

	public void validerOgLagreAksjon(String aksjonsInfo) throws UgyldigHendelseLoggInfoException {

		if (isBlank(aksjonsInfo)) {
			throw new UgyldigHendelseLoggInfoException(String.format("Meldingen mangler påkrevd %s header.", AKSJONS_INFO_HEADER));
		}

		try {
			AksjonsLoggRequest aksjonsLoggRequest = jsonStringToObject(aksjonsInfo, AksjonsLoggRequest.class);
			validateAksjonslogg(aksjonsLoggRequest);

			AksjonsLogg aksjonsLogg = aksjonsLoggMapper.mapToHendelseLogg(aksjonsLoggRequest);

			aksjonsLoggRepository.save(aksjonsLogg);
		} catch (IOException e) {
			throw new UgyldigHendelseLoggInfoException(String.format("Feilet ved lesing av %s header. Sjekk om headeren er i gyldig JSON format.", AKSJONS_INFO_HEADER), e);
		}

	}


	private void validateAksjonslogg(AksjonsLoggRequest aksjonsLoggRequest) throws UgyldigHendelseLoggInfoException {

		List<String> parameters = new ArrayList<>();
		addMessageWhenNullOrEmpty(aksjonsLoggRequest.getJournalpostId(), "journalpostId", parameters);
		addMessageWhenNullOrEmpty(aksjonsLoggRequest.getApplikasjon(), "applikasjon", parameters);
		addMessageWhenNullOrEmpty(aksjonsLoggRequest.getAksjon(), "aksjon", parameters);
		addMessageWhenNullOrEmpty(aksjonsLoggRequest.getUtfoertAv(), "utfoertAv", parameters);

		if (isFalse(parameters.isEmpty())) {
			throw new UgyldigHendelseLoggInfoException("AksjonsLogg mangler påkrevde parametere: " + String.join(", ", parameters));
		}

	}

	private void addMessageWhenNullOrEmpty(Object value, String parameter, List<String> parameters) {
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			parameters.add(parameter);
		}
	}

}
