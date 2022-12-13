package no.nav.dokarkiv.core.security.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.consumer.azure.AzureAdGraphService;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static no.nav.dokarkiv.core.security.handler.HandlerConstants.NAVIDENT_PATTERN;
import static no.nav.dokarkiv.core.security.handler.HandlerConstants.NAVIDENT_REGEX;

/**
 * * Denne handleren dekker case for kall med kun systemtoken fra REST-STS.
 * * * Authorization header - REST-STS token
 *
 * @author Joakim Bjørnstad, Jbit AS
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 */
@Slf4j
public class NavSystemkontekstHandler {
	private static final String ERROR_MELDING_PREFIX = "Tjeneste kalt med REST-STS token og Nav-User-Id header.";
	private static final String ERROR_MELDING_SUFFIX = "Konsument må informeres og bes om å rette dette.";
	private final AzureAdGraphService azureAdGraphService;

	public NavSystemkontekstHandler(AzureAdGraphService azureAdGraphService) {
		this.azureAdGraphService = azureAdGraphService;
	}

	public boolean handle(JwtToken token, HttpServletResponse response, String navUserIdHeader) throws IOException {
		final String consumerId = token.getSubject();
		if (!consumerId.startsWith("srv")) {
			return handleUnauthorizedServicebruker(response);
		}
		if (navUserIdHeader == null) {
			handleServiceUserContext(consumerId);
		} else {
			handleServiceUserWithNavUserIdHeaderContext(consumerId, navUserIdHeader.trim());
		}
		return false;
	}

	private void handleServiceUserContext(String consumerId) {
		MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerId);
		MDC.put(MDCConstants.MDC_USER_ID, consumerId);
		MDC.put(MDCConstants.MDC_USER_NAME, consumerId);
	}

	private void handleServiceUserWithNavUserIdHeaderContext(String consumerId, String navUserIdHeader) {
		if (NAVIDENT_PATTERN.matcher(navUserIdHeader).matches()) {
			final String fulltNavn = azureAdGraphService.hentFulltNavn(navUserIdHeader);
			if (fulltNavn!=null) {
				MDC.put(MDCConstants.MDC_USER_ID, navUserIdHeader);
				MDC.put(MDCConstants.MDC_USER_NAME, fulltNavn);
				MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerId);
			} else {
				log.error(ERROR_MELDING_PREFIX + " Fant ikke NAVIdent={} i Microsoft Graph. " + ERROR_MELDING_SUFFIX, navUserIdHeader);
				handleServiceUserContext(consumerId);
			}
		} else {
			log.error(ERROR_MELDING_PREFIX + " Ugyldig format på NAVIdent={}. Må matche \"" + NAVIDENT_REGEX + "\". " + ERROR_MELDING_SUFFIX, navUserIdHeader);
			handleServiceUserContext(consumerId);
		}
	}

	private boolean handleUnauthorizedServicebruker(HttpServletResponse response) throws IOException {
		String message = "Authorization headeren må ha JWT som er utstedt av issuer REST-STS tilhørende servicebruker hvis header Nav-Consumer-Token ikke er satt.";
		log.warn(message);
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
		return true;
	}
}
