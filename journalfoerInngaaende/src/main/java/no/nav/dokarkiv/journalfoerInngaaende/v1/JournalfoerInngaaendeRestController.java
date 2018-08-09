package no.nav.dokarkiv.journalfoerInngaaende.v1;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalfoerInngaaende.v1.service.HentJournalpostByJournalpostIdService;
import no.nav.dokarkiv.journalfoerInngaaende.v1.to.JournalpostResponseTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@RestController
@RequestMapping("journalfoerInngaaende/v1")
@Slf4j
public class JournalfoerInngaaendeRestController {

	private HentJournalpostByJournalpostIdService hentJournalpostByJournalpostIdService;

	@Inject
	public JournalfoerInngaaendeRestController(HentJournalpostByJournalpostIdService hentJournalpostByJournalpostIdService) {
		this.hentJournalpostByJournalpostIdService = hentJournalpostByJournalpostIdService;
	}


	@GetMapping(value = "/journalpost/{journalpostId}")
	@ResponseBody
	public JournalpostResponseTo hentJournalpostByJournalpostId(@PathVariable Long journalpostId) {
//		log.info("Henter journalpost med journalpostId=%s, dokumentinfoId={}", dokumenttypeid));
		return null;
	}

}
