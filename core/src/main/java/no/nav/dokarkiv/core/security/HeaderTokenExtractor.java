package no.nav.dokarkiv.core.security;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.NavHeaders.NAV_CONSUMER_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class HeaderTokenExtractor {

    String getIdToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
                .orElse(null);
    }

    String getConsumerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(NAV_CONSUMER_TOKEN))
                .filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
                .orElse(null);
    }
}