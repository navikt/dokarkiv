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
import static no.nav.dokarkiv.journalpost.v1.api.finnMottatteJournalposter.FinnMottatteJournalposterController.FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@RestController
@RequestMapping("/rest/journalpostapi/v1/finnMottatteJournalposter")
@Tag(name = "journalpostapi", description = "Tjenester mot journalpost")
@ProtectedWithClaims(issuer = ISSUER_AZUREV2, claimMap = {"roles=" + FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE})
public class FinnMottatteJournalposterController {

	protected static final String FINN_MIDLERTIDIGE_JOURNALPOSTER_ROLE = "finn_midlertidige_journalposter";
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
	public ResponseEntity<FinnMottatteJournalposterResponse> finnMottatteJournalposterMedTemaEldreEnn(
			@Parameter(description = "Tema man ønsker å finne mottatte journalposter for") @RequestParam("tema") String tema,
			@Parameter(description = """
					dagerGamle spesifiserer hvilken dato man ønsker å søke etter journalposter fra.
					Tjenesten vil returne alle journalposter i en midlertidig status som er opprettet mellom 01.01.2020 og Instant.now() - dagerGamle.
					Hvis dagerGamle er 7 dager vil alle journalposter opprettet mellom 01.01.2020 og Instant.now()-1 uke returneres
					HVis dagerGamle er 0 dager vil alle midlertidige journalposter opprettet mellom 01.01.2020 og Instant.now()
					01.01.2020 er en statisk dato og kan ikke endres.
					""") @RequestParam("dagerGamle") int dagerGamle) {

		MDC.put(MDC_REQUEST_ID, "finnMottatteJournalposter");
		FagomradeCode fagomraade = validateTema(tema);
		validateDagerGamle(dagerGamle);

		try {
			log.info("finnMottatteJournalposter har mottatt kall om å hente ubehandlede journalposter med tema blant={} som er eldre enn={} dager", fagomraade, dagerGamle);

			if (MDC.get(MDC_CONSUMER_ID).contains(AZP_NAME_DOKSIKKERHETSNETT)) {
				FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterMedBrukerMedTemaEldreEnn(fagomraade, dagerGamle);
				log.info("finnMottatteJournalposter returnerer {} journalposter for tema={} for doksikkerhetsnett", ubehandledeJournalposter.getJournalposter().size(), fagomraade);
				return ResponseEntity.ok().body(ubehandledeJournalposter);
			} else {
				FinnMottatteJournalposterResponse ubehandledeJournalposter = finnMottatteJournalposterService.finnMottatteJournalposterUtenBrukerMedTemaEldreEnn(fagomraade, dagerGamle);
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

	private void validateDagerGamle(int dagerGamle) {
		long antallDagerSidenJanuar2020 = Duration.between(JANUARY_1_2020.toInstant(), Instant.now()).toDaysPart();
		if (dagerGamle < 0 || dagerGamle > antallDagerSidenJanuar2020) {
			throw new InputValideringFeiletException(format("dagerGamle har ugyldig veri: %s. Finnmottattejournalposter kan ikke hente journalposter fra fremtiden eller fra før 01.01.2020", "" + dagerGamle));
		}
	}

	private FagomradeCode validateTema(String tema) {
		try {
			return FagomradeCode.valueOf(tema);
		} catch (IllegalArgumentException e) {
			throw new InputValideringFeiletException(format("Mottok ugyldig verd for tema. %s ", isBlank(tema) ? "Tema var null eller tom" : tema + " er ikke en gyldig temakode"));
		}
	}

}

