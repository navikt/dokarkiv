package no.nav.dokarkiv.core.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static no.nav.dokarkiv.core.NavHeaders.BEARER_TOKEN_PREFIX;
import static no.nav.dokarkiv.core.NavHeaders.NAV_CONSUMER_TOKEN;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class HeaderTokenExtractor {

    static String getIdToken(HttpServletRequest request) {
        return getIdTokenOptional(request).orElse(null);
    }

    public static Optional<DecodedJWT> getIdTokenDecoded(HttpServletRequest request) {
        return getIdTokenOptional(request)
                .map(JWT::decode);
    }

    static Optional<String> getIdTokenOptional(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""));
    }

    static String getConsumerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(NAV_CONSUMER_TOKEN))
                .filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
                .map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
                .orElse(null);
    }
}