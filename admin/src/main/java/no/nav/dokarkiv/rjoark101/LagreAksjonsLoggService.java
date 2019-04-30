package no.nav.dokarkiv.rjoark101;

import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggService;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTO;
import no.nav.dokarkiv.core.aksjonslogg.AksjonsLoggTOMapper;
import no.nav.dokarkiv.core.aksjonslogg.ArkivElementEndringTO;
import no.nav.dokarkiv.core.domain.codes.AksjonsTypeCode;
import no.nav.dokarkiv.core.exceptions.UgyldigAksjonsLoggException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Mapper og lagrer AksjonsLogg i en egen transaksjon. AksjonsLoggSerivce henter Journalpost fra databasen for å hente ut verdier som settes i aksjonsloggen.
 * Etter sletting av Journalpost vil aksjonsLoggService feile fordi den ikke finner Journalpost.
 * Lagring av aksjonsLogg må derfor skje i egen transaksjon hvor Journalpost fortsatt ikke er slettet.
 * For at Spring skal kunne lage ny transaksjon må denne metoden bli definert i en egen bønne.
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Component
public class LagreAksjonsLoggService {

	private final AksjonsLoggService aksjonsLoggService;
	private final AksjonsLoggTOMapper aksjonsLoggTOMapper;

	public LagreAksjonsLoggService(AksjonsLoggService aksjonsLoggService) {
		this.aksjonsLoggService = aksjonsLoggService;
		this.aksjonsLoggTOMapper = new AksjonsLoggTOMapper();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void lagreAksjonsLogg(Long journalpostId, Long dokumentInfoId, String aksjonsLoggHeaderString, List<ArkivElementEndringTO> arkivElementEndringTOList) throws
			UgyldigAksjonsLoggException {

		AksjonsLoggTO aksjonsLoggTO = aksjonsLoggTOMapper.mapAksjonsLoggHeader(aksjonsLoggHeaderString, AksjonsTypeCode.SLETT, journalpostId, dokumentInfoId);
		aksjonsLoggService.validateAndSaveAksjonsLogg(aksjonsLoggTO, arkivElementEndringTOList);
	}
}
