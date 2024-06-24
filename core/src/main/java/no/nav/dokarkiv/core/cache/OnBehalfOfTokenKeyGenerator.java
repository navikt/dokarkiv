package no.nav.dokarkiv.core.cache;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static org.springframework.util.StringUtils.arrayToDelimitedString;

@Component("onBehalfOfTokenKeyGenerator")
public class OnBehalfOfTokenKeyGenerator implements KeyGenerator {
	@Override
	public Object generate(Object target, Method method, Object... params) {
		return DigestUtils.sha256Hex(arrayToDelimitedString(params, "-"));
	}
}
