package no.nav.dokarkiv.core.cache;

import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.context.TokenValidationContextHolder;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component("cacheKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

	private final TokenValidationContextHolder tokenValidationContextHolder;

	public CacheKeyGenerator(TokenValidationContextHolder tokenValidationContextHolder) {
		this.tokenValidationContextHolder = tokenValidationContextHolder;
	}

	@Override
	public Object generate(Object target, Method method, Object... params) {
		TokenValidationContext tokenValidationContext = tokenValidationContextHolder.getTokenValidationContext();
		JwtToken jwtToken = tokenValidationContext.getFirstValidToken().get();
		return DigestUtils.sha256Hex(jwtToken.getTokenAsString());
	}
}
