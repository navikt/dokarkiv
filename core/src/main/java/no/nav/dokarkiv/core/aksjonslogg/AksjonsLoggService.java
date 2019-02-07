package no.nav.dokarkiv.core.aksjonslogg;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class AksjonsLoggService {

	public static final String AKSJONS_LOGG_HEADER = "dok_aksjonslogg";

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggMapper aksjonsLoggMapper;
	private final AksjonsLoggValidator aksjonsLoggValidator;

	public AksjonsLoggService(AksjonsLoggRepository aksjonsLoggRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggMapper = new AksjonsLoggMapper();
		this.aksjonsLoggValidator = new AksjonsLoggValidator();
	}

	public void validateAndSaveAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList) throws UgyldigAksjonsLoggException {

		aksjonsLoggValidator.validateAksjonslogg(aksjonsLoggTO);
		aksjonsLoggValidator.validateArkivElementToList(arkivElementEndringTOList);

		AksjonsLogg aksjonsLogg = aksjonsLoggMapper.mapToAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);

		aksjonsLoggRepository.save(aksjonsLogg);
	}



}
