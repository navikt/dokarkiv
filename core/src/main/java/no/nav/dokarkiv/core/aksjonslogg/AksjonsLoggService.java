package no.nav.dokarkiv.core.aksjonslogg;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.cxf.common.util.PropertyUtils.isFalse;

import no.nav.dokarkiv.core.domain.codes.AksjonTypeCode;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggInfoException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AksjonsLoggService {

	public static final String AKSJONS_LOGG_HEADER = "dok_aksjonslogg";

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggMapper aksjonsLoggMapper;

	public AksjonsLoggService(AksjonsLoggRepository aksjonsLoggRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggMapper = new AksjonsLoggMapper();
	}

	public void validateAndSaveAksjon(AksjonsLoggHeader aksjonsLoggHeader) throws UgyldigAksjonsLoggInfoException {

		validateAksjonslogg(aksjonsLoggHeader);

		Iterable<AksjonsLogg> aksjonsLoggIterable = aksjonsLoggHeader.getAksjonListe()
				.stream()
				.map(aksjonsLoggMapper::mapToAksjonsLogg)
				.collect(Collectors.toSet());

		aksjonsLoggRepository.saveAll(aksjonsLoggIterable);
	}


	private void validateAksjonslogg(AksjonsLoggHeader aksjonsLoggHeader) throws UgyldigAksjonsLoggInfoException {

		for (AksjonsLoggHeader.Aksjon aksjon : aksjonsLoggHeader.getAksjonListe()) {
			assertNullOrEmpty(aksjon.getJournalpostId(), "journalpostId");
			assertNullOrEmpty(aksjon.getApplikasjon(), "applikasjon");
			assertNullOrEmpty(aksjon.getAksjon(), "aksjon");
			assertNullOrEmpty(aksjon.getUtfoertAv(), "utfoertAv");

			assertInvalidEnum(aksjon.getAksjon(), "aksjon", AksjonTypeCode.values());
		}
	}

	private void assertNullOrEmpty(Object value, String parameter) throws UgyldigAksjonsLoggInfoException{
		if (Objects.isNull(value) || (value instanceof String && isBlank((String) value))) {
			throw new UgyldigAksjonsLoggInfoException("AksjonsLogg mangler påkrevd parameter: " + parameter);
		}
	}

	private void assertInvalidEnum(Object value, String parameter, Enum[] allowedValues) throws UgyldigAksjonsLoggInfoException {
		boolean invalid = true;
		for(Enum e: allowedValues){
			if (e.name().equals(value)) {
				invalid = false;
				break;
			}
		}

		if (invalid) {
			throw new UgyldigAksjonsLoggInfoException(String.format("AksjonsLogg inneholder ugyldig verdi: %s er ikke en gyldig verdi for %s", value, parameter));
		}
	}
}
