package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.NavHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class HeaderTokenExtractor {

    String getIdToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(e -> e.startsWith(NavHeaders.BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(NavHeaders.BEARER_TOKEN_PREFIX, ""))
                .orElse(null);
    }

    String getConsumerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(NavHeaders.NAV_CONSUMER_TOKEN))
                .filter(e -> e.startsWith(NavHeaders.BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(NavHeaders.BEARER_TOKEN_PREFIX, ""))
                .orElse(null);
    }
}