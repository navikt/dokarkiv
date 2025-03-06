package no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagomradeCode;
import no.nav.dokarkiv.core.exceptions.DokarkivFunctionalException;
import no.nav.dokarkiv.core.exceptions.DokarkivTechnicalException;
import no.nav.dokarkiv.core.exceptions.InputValideringFeiletException;
import no.nav.dokarkiv.journalpost.v1.swagger.SwaggerFinnMottatteJournalposterMedTemaEldreEnn;
import no.nav.security.token.support.core.api.ProtectedWithClaims;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.format;
import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;
import static no.nav.dokarkiv.core.MDCConstants.MDC_REQUEST_ID;
import static no.nav.dokarkiv.core.security.SporingHandlerInterceptor.ISSUER_AZUREV2;
import static no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterController.FINN_MOTTATTE_JOURNALPOSTER_ROLE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/finnMottatteJournalposter")
@Tag(name = "journalpostapi", description = "Tjenester mot journalpost")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + FINN_MOTTATTE_JOURNALPOSTER_ROLE})
public class FinnMottatteJournalposterController {

	public static final String FINN_MOTTATTE_JOURNALPOSTER_ROLE = "finn_mottatte_journalposter";
	private static final String AZP_NAME_DOKSIKKERHETSNETT = "doksikkerhetsnett";
	private static final Date JANUARY_1_2020 = Date.from(LocalDate.of(2020, Month.JANUARY, 1)
			.atStartOfDay(ZoneId.systemDefault())
			.toInstant());

	private final FinnMottatteJournalposterService finnMottatteJournalposterService;

	public FinnMottatteJournalposterController(FinnMottatteJournalposterService finnMottatteJournalposterService) {
		this.finnMottatteJournalposterService = finnMottatteJournalposterService;
	}

	@GetMapping
	@SwaggerFinnMottatteJournalposterMedTemaEldreEnn
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposter(
			@Parameter(description = "Tema man ønsker å finne mottatte journalposter for") @RequestParam("tema") String tema,
			@Parameter(description = """
					Tjenesten vil returnere alle journalposter i mottatt status opprettet mellom 01.01.2020 og Instant.now() - antallDagerGamle.
					01.01.2020 er en statisk dato og kan ikke endres.
					""") @RequestParam("antallDagerGamle") int antallDagerGamle) {

		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		FagomradeCode fagomraade = validateTema(tema);
		validateDagerGamle(antallDagerGamle);

		try {
			log.info("finnMottatteJournalposter har mottatt kall om å hente mottatte journalposter med tema={} som er eldre enn={} dager", fagomraade, antallDagerGamle);

			if (MDC.get(MDC_CONSUMER_ID).contains(AZP_NAME_DOKSIKKERHETSNETT)) {
				FinnMottatteJournalposterResponse mottatteJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedBrukerMedTemaEldreEnn(fagomraade, antallDagerGamle);
				log.info("finnMottatteJournalposter returnerer {} journalposter for tema={} som er eldre enn={} dager for doksikkerhetsnett", mottatteJournalposter.getJournalposter().size(), fagomraade, antallDagerGamle);
				return ResponseEntity.ok().body(mottatteJournalposter);
			} else {
				FinnMottatteJournalposterResponse mottatteJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterUtenBrukerMedTemaEldreEnn(fagomraade, antallDagerGamle);
				log.info("finnMottatteJournalposter returnerer {} journalposter for tema={} som er eldre enn={} dager", mottatteJournalposter.getJournalposter().size(), fagomraade, antallDagerGamle);
				return ResponseEntity.ok().body(mottatteJournalposter);
			}
		} catch (DokarkivFunctionalException e) {
			log.warn("finnMottatteJournalposter - feilet funksjonelt ved søk på mottatte journalposter med tema={} som er eldre enn={} dager. Feilmelding={}",
					fagomraade, antallDagerGamle, e.getMessage());
			throw e;
		} catch (DokarkivTechnicalException e) {
			log.error("finnMottatteJournalposter - feilet teknisk ved søk på mottatte journalposter med tema={} som er eldre enn={} dager. Feilmelding={}",
					fagomraade, antallDagerGamle, e.getMessage());
			throw e;
		}
	}

	private void validateDagerGamle(int antallDagerGamle) {
		long antallDagerSidenJanuar2020 = Duration.between(JANUARY_1_2020.toInstant(), Instant.now()).toDaysPart();
		if (antallDagerGamle < 0 || antallDagerGamle > antallDagerSidenJanuar2020) {
			throw new InputValideringFeiletException(format("Mottok ugyldig verdi for antallDagerGamle. AntallDagerGamle:%s ender opp utenfor spennet 01.01.2020 -> dagens dato", "" + antallDagerGamle));
		}
	}

	private FagomradeCode validateTema(String tema) {
		if(isBlank(tema)){
			throw new InputValideringFeiletException("Mottok ugyldig verdi for tema. Tema var null eller tom");
		}
		try {
			return FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Mottok ugyldig verdi for tema. %s er ikke en gyldig temakode", tema));
		}
	}

}

