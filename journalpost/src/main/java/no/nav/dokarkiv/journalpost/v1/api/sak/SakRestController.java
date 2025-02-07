package no.nav.dokarkiv.journalpost.v1.api.sak;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerAvsluttSak;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerGjenaapneSak;
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
import static no.nav.dokarkiv.journalpost.v1.api.sak.SakRequestValidator.validateAvsluttSakRequest;
import static no.nav.dokarkiv.journalpost.v1.api.sak.SakRequestValidator.validateGjenaapneSakRequest;

@Slf4j
@Protected
@RestController
@RequestMapping("/rest/journalpostapi/v1/sak")
@Tag(name = "journalpostapi - sak", description = "Tjenester for å modifisere saker")
public class SakRestController {

	private final AvsluttSakService avsluttSakService;
	private final GjenaapneSakService gjenaapneSakService;

	public SakRestController(AvsluttSakService avsluttSakService, GjenaapneSakService gjenaapneSakService) {
		this.avsluttSakService = avsluttSakService;
		this.gjenaapneSakService = gjenaapneSakService;
	}

	@SwaggerAvsluttSak
	@PatchMapping(value = "/avsluttSak")
	public ResponseEntity<String> avsluttSak(
			@RequestBody AvsluttSakRequest avsluttSakRequest
	) {
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("AvsluttSak har fått kall om å avslutte sak med fagsakId={} fra fagsaksystem={}", avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem());

		validateAvsluttSakRequest(avsluttSakRequest);
		String avsluttetOrAvbrutt = avsluttSakService.avsluttSaker(avsluttSakRequest);
		log.info("AvsluttSak har {} sak med fagsakId={} fra fagsaksystem={}", avsluttetOrAvbrutt, avsluttSakRequest.getFagsakId(), avsluttSakRequest.getFagsaksystem());
		return ResponseEntity.ok().build();
	}

	@SwaggerGjenaapneSak
	@PatchMapping(value = "/gjenaapneSak")
	public ResponseEntity<String> gjenaapneSak(
			@RequestBody GjenaapneSakRequest gjenaapneSakRequest
	) {
		createAndSetUsername(MDC.get(MDC_USER_ID), MDC.get(MDC_CONSUMER_ID));
		log.info("GjenaapneSak har fått kall om å gjenåpne sak med fagsakId={} fra fagsaksystem={}", gjenaapneSakRequest.getFagsakId(), gjenaapneSakRequest.getFagsaksystem());
		validateGjenaapneSakRequest(gjenaapneSakRequest);

		gjenaapneSakService.gjenaapneFagsak(gjenaapneSakRequest);
		log.info("GjenaapneSak har gjenåpnet sak med fagsakId={} fra fagsaksystem={}", gjenaapneSakRequest.getFagsakId(), gjenaapneSakRequest.getFagsaksystem());
		return ResponseEntity.ok().build();
	}

}
