package no.nav.dokarkiv.core.aksjonslogg;

import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
class AksjonsLoggServiceImpl implements AksjonsLoggService {

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggMapper aksjonsLoggMapper;
	private final AksjonsLoggValidator aksjonsLoggValidator;

	public AksjonsLoggServiceImpl(AksjonsLoggRepository aksjonsLoggRepository) {
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
