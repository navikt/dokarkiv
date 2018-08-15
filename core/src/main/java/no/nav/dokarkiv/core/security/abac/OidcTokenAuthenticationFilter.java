package no.nav.dokarkiv.core.security.abac;

import static org.springframework.security.core.authority.AuthorityUtils.NO_AUTHORITIES;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

@Slf4j
public class OidcTokenAuthenticationFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		SecurityContextHolder.clearContext();

		//In case the application have several authorization headers
		String header = getOidcAuthHeader(request.getHeaders("Authorization"));

		if (header == null || !header.startsWith("Bearer ")) {
			log.warn("Kunne ikke autorisere forespoersel. Finner ingen header med key=Authorization og value=Bearer *oidcToken*.");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"Kunne ikke autorisere forespoersel. Finner ingen header med key=Authorization og value=Bearer *oidcToken*.");
			return;
		}

		String tokenBody = splitAndReturnTokenBody(header, response);
		if (tokenBody == null) {
			log.warn("OIDC-token mangler / er på feil format! Korrekt format på token er header.body.signature.");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
					"OIDC-token mangler / er på feil format! Korrekt format på token er header.body.signature.");
			return;
		}

		//Put OIDC token on the context
		Username***passord=gammelt_passord***);
		SecurityContextHolder.getContext().setAuthentication(authRequest);

		filterChain.doFilter(request, response);
	}

	private String splitAndReturnTokenBody(String header, HttpServletResponse response) throws IOException {
		String[] tokenArray = new String(header).split("\\.");
		if (tokenArray.length < 3) {
			return null;
		}
		//The token ships on the form header.body.signature. We want the token body
		return new String(tokenArray[1].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
	}

	private String getOidcAuthHeader(Enumeration<String> headers) {
		while (headers.hasMoreElements()) {
			String header = headers.nextElement();
			if (header.startsWith("Bearer ")) {
				return header;
			}
		}
		return null;
	}

}
