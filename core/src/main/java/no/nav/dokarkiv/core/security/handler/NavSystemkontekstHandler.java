package no.nav.dokarkiv.core.security.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *  * Denne handleren dekker case for kall med kun systemtoken fra REST-STS.
 *  * * Authorization header - REST-STS token
 *
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class NavSystemkontekstHandler {
    public boolean handle(JwtToken token, HttpServletResponse response) throws IOException {
        final String consumerID = token.getSubject();
        if (!consumerID.startsWith("srv")) {
            return handleUnauthorizedServicebruker(response);
        }
        MDC.put(MDCConstants.MDC_CONSUMER_ID, consumerID);
        MDC.put(MDCConstants.MDC_USER_ID, consumerID);
        MDC.put(MDCConstants.MDC_USER_NAME, consumerID);
        return false;
    }

    private boolean handleUnauthorizedServicebruker(HttpServletResponse response) throws IOException {
        String message = "Authorization headeren må ha JWT som er utstedt av issuer REST-STS tilhørende servicebruker hvis header Nav-Consumer-Token ikke er satt.";
        log.warn(message);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        return true;
    }
}
