package no.nav.dokarkiv.slettarkivenhet;

import no.nav.dokarkiv.slettarkivenhet.exception.UgyldigInputException;
import no.nav.dokarkiv.slettarkivenhet.rjoark102.SlettArkivenhetRequest;
import no.nav.dokarkiv.slettarkivenhet.rjoark102.SlettArkivenhetResponse;
import no.nav.dokarkiv.slettarkivenhet.rjoark102.SlettArkivenhetService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */

@RestController("/rest/slettarkivenhet")
public class SlettArkivenhetController {


	private final SlettArkivenhetService slettArkivenhetService;

	public SlettArkivenhetController(SlettArkivenhetService slettArkivenhetService) {
		this.slettArkivenhetService = slettArkivenhetService;
	}

	@Transactional(rollbackFor = Exception.class)
	@DeleteMapping
	public SlettArkivenhetResponse slettArkivenhet(@RequestBody SlettArkivenhetRequest slettArkivenhetRequest) {

		//TODO: Validering av input

		if (Objects.isNull(slettArkivenhetRequest.getArkivenhet())) {
			throw new UgyldigInputException("Input mangler arkivenhet");
		}

		switch (slettArkivenhetRequest.getArkivenhet()) {
			case JOURNALPOST:
				slettArkivenhetService.slettJournalpost(slettArkivenhetRequest);
				break;
			case DOKUMENT_FIL:
				slettArkivenhetService.slettDokumentFil(slettArkivenhetRequest);
				break;
			case DOKUMENT_INFO:
				slettArkivenhetService.slettDokumentInfo(slettArkivenhetRequest);
				break;
			default:
				throw new IllegalArgumentException("Input mangler arkivenhet");
		}

		return SlettArkivenhetResponse.builder()
				.dokumentInfoId(slettArkivenhetRequest.getDokumentInfoId())
				.journalpostId(slettArkivenhetRequest.getJournalpostId())
				.build();

	}

}
