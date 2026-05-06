package no.nav.dokarkiv.internal.avstemreferanser;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.validator.EksternReferanseIdValidator;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.internal.avstemreferanser.AvstemReferanserController.SKANNING_ROLE_CLAIM_TILGANG;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + SKANNING_ROLE_CLAIM_TILGANG})
@RestController
@RequestMapping(path = "/rest/journalpostapi/v1/avstemReferanser", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class AvstemReferanserController {
	public static final String SKANNING_ROLE_CLAIM_TILGANG = "api_intern_skanning";

	private final AvstemReferanserService avstemReferanserService;

	public AvstemReferanserController(AvstemReferanserService avstemReferanserService) {
		this.avstemReferanserService = avstemReferanserService;
	}

	@PostMapping
	public ResponseEntity<FeilendeAvstemmingReferanser> avstemReferanser(@RequestBody AvstemmingReferanser avstemmingReferanser) {
		validateReferanser(avstemmingReferanser.referanser());

		List<String> manglendeReferanser = avstemReferanserService.avstemReferanser(avstemmingReferanser);

		if (manglendeReferanser.isEmpty()) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.ok(new FeilendeAvstemmingReferanser(manglendeReferanser));
		}
	}

	private void validateReferanser(Set<String> referanser) {
		if (referanser == null || referanser.isEmpty()) {
			throw new InputValideringFeiletException("liste over referanser kan ikke være null eller tom");
		}
		referanser.forEach(EksternReferanseIdValidator::validateEksternReferanseId);
	}
}
