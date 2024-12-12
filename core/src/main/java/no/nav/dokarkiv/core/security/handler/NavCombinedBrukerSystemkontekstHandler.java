package no.nav.dokarkiv.core.security.handler;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.validation.JwtTokenValidator;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * Handleren registrerer sporingsdata for arkivering og endring av metadata etter arkivering.
 *
 * Denne handleren dekker case der brukerkontekst propageres fra OpenAM issuer og systemkontekst fra REST-STS.
 * * Authorization header - OpenAM token
 * * Nav-Consumer-Token header - REST-STS token
 *
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 * @deprecated Slettes etter at det er bekreftet at ingen bruker det på denne måten lenger.
 */
@Slf4j
@Deprecated
public class NavCombinedBrukerSystemkontekstHandler {
    private final AzureAdGraphService azureAdGraphService;
    private final JwtTokenValidator restStsTokenValidator;

    public NavCombinedBrukerSystemkontekstHandler(AzureAdGraphService azureAdGraphService, JwtTokenValidator restStsTokenValidator) {
        this.azureAdGraphService = azureAdGraphService;
        this.restStsTokenValidator = restStsTokenValidator;
    }

    public boolean handle(JwtToken openAmToken, String restStsToken, HttpServletResponse response) throws IOException {
        // Hvis man ikke ser denne logglinjen lenger i prod så kan NavCombinedBrukerSystemkontekstHandler slettes.
        log.warn("System kaller dokarkiv tjenester med Nav-Consumer-Token. Denne måten å bruke dokarkiv på er deprekert ifbm OpenAM saneringen 2023. " +
                 "Konsument må informeres at dette ikke er nødvendig lenger.");
        if (handleBrukerkontekst(openAmToken, response)) return true;
        return handleSystemkontekst(restStsToken, response);
    }

    private boolean handleBrukerkontekst(JwtToken openAmToken, HttpServletResponse response) throws IOException {
        final String userId = openAmToken.getSubject();
        final String fulltNavn = azureAdGraphService.hentFulltNavn(userId);
        if (fulltNavn != null) {
            MDC.put(MDCConstants.MDC_USER_ID, userId);
            MDC.put(MDCConstants.MDC_USER_NAME, fulltNavn);
        } else {
            String message = "Authorization headeren må ha JWT som er utstedt av issuer OpenAM og tilhøre saksbehandler hvis både Authorization og Nav-Consumer-Token headerene er satt. " +
                    "Grunnen til dette er at Authorization headeren propagerer brukerkontekst og Nav-Consumer-Token header systemkontekst. " +
                    "Vi anbefaler bruk av Azure OAuth 2.0 On-Behalf-Of flow for å støtte brukerkontekst i system-til-system kall.";
            log.warn(message);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
            return true;
        }
        return false;
    }

    private boolean handleSystemkontekst(String restStsToken, HttpServletResponse response) throws IOException {
        try {
            restStsTokenValidator.assertValidToken(restStsToken);
        } catch (JwtTokenValidatorException e) {
            return handleUnauthorizedServicebruker(response);
        }
        String consumerID = new JwtToken(restStsToken).getSubject();
        if (!consumerID.startsWith("srv")) {
            return handleUnauthorizedServicebruker(response);
        }
        MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
        return false;
    }

    private boolean handleUnauthorizedServicebruker(HttpServletResponse response) throws IOException {
        String message = "Nav-Consumer-Token headeren må ha JWT som er utstedt av issuer REST-STS og tilhøre servicebruker hvis både Authorization og Nav-Consumer-Token headerene er satt. " +
                "Grunnen til dette er at Nav-Consumer header propagerer systemkontekst og Authorization header propagerer brukerkontekst. " +
                "Vi anbefaler bruk av Azure OAuth 2.0 On-Behalf-Of flow for å støtte brukerkontekst i system-til-system kall.";
        log.warn(message);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        return true;
    }
}
