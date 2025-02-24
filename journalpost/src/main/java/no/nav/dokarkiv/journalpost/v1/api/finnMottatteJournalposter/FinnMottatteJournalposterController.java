package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.UgyldigTemakodeException;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposterMedTemaEldreEnn;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterController.FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1")
@Tag(name = "journalpostapi", description = "Tjenester mot journalpost")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE})
public class FinnMottatteJournalposterController {

	protected static final String FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE = "finn_midlertidige_journalposter";

	private final FinnMottatteJournalposterService finnMottatteJournalposterService;
	private final String AZP_NAME_DOKSIKKERHETSNETT = "doksikkerhetsnett";

	public FinnMottatteJournalposterController(FinnMottatteJournalposterService finnMottatteJournalposterService) {
		this.finnMottatteJournalposterService = finnMottatteJournalposterService;
	}

	@Protected
	@ResponseBody
	@Transactional(readOnly = true)
	@SwaggerFinnMottatteJournalposterMedTemaEldreEnn
	@GetMapping(value = "/finnMottatteJournalposter/{tema}/{dagerGamle}")
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTemaEldreEnn(
			@PathVariable("tema") String tema,
			@PathVariable("dagerGamle") int dagerGamle) {

		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		FagomradeCode fagomraade = validateTema(tema);
		//TODO: Ser på validering av dagerGamle også. SOm det er i dag er det en limit på databasespørringen som gjør at vi max ser tilbake til 01-01 2020, men tenker denne burde endres til i dag -1/2 år.
		//TODO: Valideringen kommer etter dette, men den er på vei.

		try {
			log.info("finnMottatteJournalposter har mottatt kall om å hente ubehandlede journalposter med tema blant={}", fagomraade);

			if (MDC.get(MDC_CONSUMER_ID).contains(AZP_NAME_DOKSIKKERHETSNETT)) {
				FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(fagomraade, dagerGamle, true);
				log.info("finnMottatteJournalposter returnerer {} journalposter for tema={} for doksikkerhetsnett", ubehandledeJournalposter.getJournalposter().size(), fagomraade);
				return ResponseEntity.ok().body(ubehandledeJournalposter);
			} else {
				FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedTemaEldreEnn(fagomraade, dagerGamle, false);
				log.info("finnMottatteJournalposter returnerer {} journalposter for tema={}", ubehandledeJournalposter.getJournalposter().size(), fagomraade);
				return ResponseEntity.ok().body(ubehandledeJournalposter);
			}
		} catch (DokarkivFunctionalException e) {
			log.warn("finnMottatteJournalposter - feilet funksjonelt ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}",
					fagomraade, e.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("finnMottatteJournalposter - feilet teknisk ved søk på ubehandlede journalposter med tema blandt {}. Feilmelding={}",
					fagomraade, e.getMessage());
			throw e;
		}
	}


	private FagomradeCode validateTema(String tema) {
		try {
			return FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new UgyldigTemakodeException(format("Mottok ugyldig verd for tema. %s er ikke en gyldig temakode", tema));
		}
	}

}

