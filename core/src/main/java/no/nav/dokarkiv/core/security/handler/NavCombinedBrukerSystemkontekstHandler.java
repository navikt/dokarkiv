package no.nav.dokarkiv.core.security.handler;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import no.nav.security.token.support.core.exceptions.JwtTokenValidatorException;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.validation.JwtTokenValidator;
import org.slf4j.MDC;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handleren registrerer sporingsdata for arkivering og endring av metadata etter arkivering.
 *
 * Denne handleren dekker case der brukerkontekst propageres fra OpenAM issuer og systemkontekst fra REST-STS.
 * * Authorization header - OpenAM token
 * * Nav-Consumer-Token header - REST-STS token
 *
 * @see no.nav.dokarkiv.core.security.SporingHandlerInterceptor
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class NavCombinedBrukerSystemkontekstHandler {
    private final NavLdapService navLdapService;
    private final JwtTokenValidator restStsTokenValidator;

    public NavCombinedBrukerSystemkontekstHandler(NavLdapService navLdapService, JwtTokenValidator restStsTokenValidator) {
        this.navLdapService = navLdapService;
        this.restStsTokenValidator = restStsTokenValidator;
    }

    public boolean handle(JwtToken openAmToken, String restStsToken, HttpServletResponse response) throws IOException {
        if (handleBrukerkontekst(openAmToken, response)) return true;
        return handleSystemkontekst(restStsToken, response);
    }

    private boolean handleBrukerkontekst(JwtToken openAmToken, HttpServletResponse response) throws IOException {
        final String userId = openAmToken.getSubject();
        final NavUser navUser = navLdapService.findByUserId(userId);
        if (navUser.isUserExistsInLdap()) {
            MDC.put(MDCConstants.MDC_USER_ID, userId);
            MDC.put(MDCConstants.MDC_USER_NAME, navUser.getFullname());
        } else {
            String message = "OIDC token på Authorization header må tilhøre en Internbruker når både Authorization og Nav-Consumer-Token header er satt";
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
        String message = "OIDC token på Nav-Consumer-Token header må tilhøre en Servicebruker når både Authorization og Nav-Consumer-Token header er satt";
        log.warn(message);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        return true;
    }
}
