package no.nav.dokarkiv.core.aksjonslogg;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.AksjonsLogg;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.AksjonsLoggRepository;
import no.nav.dokarkiv.core.repository.JournalpostRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AksjonsLoggServiceImpl implements AksjonsLoggService {

	private final AksjonsLoggRepository aksjonsLoggRepository;
	private final AksjonsLoggValidator aksjonsLoggValidator;
	private final JournalpostRepository journalpostRepository;

	public AksjonsLoggServiceImpl(AksjonsLoggRepository aksjonsLoggRepository, JournalpostRepository journalpostRepository) {
		this.aksjonsLoggRepository = aksjonsLoggRepository;
		this.aksjonsLoggValidator = new AksjonsLoggValidator();
		this.journalpostRepository = journalpostRepository;
	}

	public void validateAndSaveAksjonsLogg(AksjonsLoggTO aksjonsLoggTO, List<ArkivElementEndringTO> arkivElementEndringTOList) {

		log.info(String.format("Lagrer aksjonslogg med aksjonsType=%s, dokumentInfoId=%s og journalpostId=%s",
				aksjonsLoggTO.getAksjon(),
				aksjonsLoggTO.getDokumentInfoId(),
				aksjonsLoggTO.getJournalpostId()));
		aksjonsLoggValidator.validateAksjonslogg(aksjonsLoggTO);
		aksjonsLoggValidator.validateArkivElementToList(arkivElementEndringTOList);

		Journalpost journalpost = aksjonsLoggTO.getJournalpostId() == null ? null :
				journalpostRepository.findById(aksjonsLoggTO.getJournalpostId())
					.orElseThrow(() -> new JournalpostIkkeFunnetException(String.format("Kunne ikke finne journalpost med journalpostId=%s i joark", aksjonsLoggTO.getJournalpostId())));

		AksjonsLogg aksjonsLogg = AksjonsLoggMapper.mapToAksjonsLoggAndSetDefaults(aksjonsLoggTO, arkivElementEndringTOList, journalpost);

		aksjonsLoggRepository.persist(aksjonsLogg);
	}
}
