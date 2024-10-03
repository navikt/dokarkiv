package no.nav.dokarkiv.journalpost.v1.api.avsluttSak;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.entities.Sak;
import no.nav.dokarkiv.core.repository.sak.HentSakerRepository;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAvsluttSak;
import no.nav.security.token.support.core.api.Protected;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_USER_ID;
import static no.nav.dokarkiv.core.stelvio.RequestContextUtil.createAndSetUsername;
import static no.nav.dokarkiv.journalpost.v1.api.avsluttSak.AvsluttSakValidator.validateAvsluttSakRequest;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/sak")
@Tag(name = "journalpostapi - avsluttSak", description = "Tjeneste for å avslutte sak")
public class AvsluttSakRestController {

	private final AvsluttSakService avsluttSakService;
	private final HentSakerRepository hentSakerRepository;

	public AvsluttSakRestController(AvsluttSakService avsluttSakService, HentSakerRepository hentSakerRepository) {
		this.avsluttSakService = avsluttSakService;
		this.hentSakerRepository = hentSakerRepository;
	}

	@SwaggerAvsluttSak
	@PatchMapping(value = "/avsluttSak")
	public ResponseEntity<String> avsluttSak(
			@RequestBody AvsluttSakRequest avsluttSakRequest
	) {
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info(format("AvsluttSak har fått kall om å avslutte sak med fagsakId=%s fra fagsaksystem=%s", avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem()));

		validateAvsluttSakRequest(avsluttSakRequest);
		avsluttSakService.avsluttSak(avsluttSakRequest);
		log.info(format("AvsluttSak har avsluttet saken med fagsakId=%s fra fagsaksystem=%s", avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem()));
		Sak sak = hentSakerRepository.hentSak(1L).get();
		//Mer info i response?
		return ResponseEntity.status(OK).build();
	}

}
