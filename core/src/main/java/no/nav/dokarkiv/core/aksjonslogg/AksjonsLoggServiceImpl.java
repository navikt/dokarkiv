package no.nav.dokarkiv.core.aksjonslogg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
@Slf4j
public class AksjonsLoggServiceImpl implements AksjonsLoggService {

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggValidator aksjonsLoggValidator;
	private final JoarkRepository joarkRepository;

	public AksjonsLoggServiceImpl(AksjonsLoggRepository aksjonsLoggRepository, JoarkRepository joarkRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggValidator = new AksjonsLoggValidator();
		this.joarkRepository = joarkRepository;
	}

	public void validateAndSaveAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList) {

		log.info(String.format("Lagrer aksjonslogg med aksjonsType=%s, dokumentInfoId=%s og journalpostId=%s", aksjonsLoggTO.getAksjon(), aksjonsLoggTO
				.getDokumentInfoId(), aksjonsLoggTO.getJournalpostId()));
		aksjonsLoggValidator.validateAksjonslogg(aksjonsLoggTO);
		aksjonsLoggValidator.validateArkivElementToList(arkivElementEndringTOList);

		Journalpost journalpost = aksjonsLoggTO.getJournalpostId() == null ? null :
				joarkRepository.findById(aksjonsLoggTO.getJournalpostId())
					.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", aksjonsLoggTO.getJournalpostId())));

		AksjonsLogg aksjonsLogg = AksjonsLoggMapper.mapToAksjonsLoggAndSetDefaults(aksjonsLoggTO, arkivElementEndringTOList, journalpost);

		aksjonsLoggRepository.save(aksjonsLogg);
	}


}
