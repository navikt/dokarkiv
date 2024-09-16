package no.nav.dokarkiv.journalpost.v1.controllers;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.journalpost.v1.api.AvstemmingReferanser;
import no.nav.dokarkiv.journalpost.v1.api.FeilendeAvstemmingReferanser;
import no.nav.dokarkiv.journalpost.v1.services.AvstemReferanserService;
import no.nav.dokarkiv.journalpost.v1.validators.CommonValidator;
import no.nav.security.token.support.core.api.Protected;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.controllers.AvstemReferanserController.SKANNING_ROLE_CLAIM_TILGANG;

@Slf4j
@Protected
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + SKANNING_ROLE_CLAIM_TILGANG})
@RestController
@RequestMapping("/rest/journalpostapi/v1/avstemreferanser")
public class AvstemReferanserController {
	public static final String SKANNING_ROLE_CLAIM_TILGANG = "api_intern_skanning";

	private final AvstemReferanserService avstemReferanserService;

	public AvstemReferanserController(AvstemReferanserService avstemReferanserService) {
		this.avstemReferanserService = avstemReferanserService;
	}

	@PostMapping
	public ResponseEntity<?> avstemReferanser(AvstemmingReferanser avstemmingReferanser) {
		validateReferanser(avstemmingReferanser.referanser());

		List<String> errors = avstemReferanserService.avstemReferanser(avstemmingReferanser);

		if (errors.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(new FeilendeAvstemmingReferanser(errors));
		}
	}

	private void validateReferanser(List<String> referanser) {
		referanser.forEach(CommonValidator::validateEksternReferanseId);
	}
}
