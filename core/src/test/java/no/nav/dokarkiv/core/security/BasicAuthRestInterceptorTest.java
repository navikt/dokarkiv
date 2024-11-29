package no.nav.dokarkiv.core.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.nav.dokarkiv.core.cache.CacheConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.util.Base64Utils;

import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public class BasicAuthRestInterceptorTest {

	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String BASE_DN = "dc=test,dc=local";
	private static final String SERVICE_USER_BASE_DN = "ou=ServiceAccounts,dc=test,dc=local";
	private static final String SERVICE_USER_GROUP_DN = "0000-GA-test";
	private final LdapTemplate ldapTemplateMock = mock(LdapTemplate.class);
	private final CacheManager cacheManagerMock = mock(CacheManager.class);
	private final HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);
	private final HttpServletResponse httpServletResponseMock = mock(HttpServletResponse.class);
	private final Object handlerMock = mock(Object.class);
	private final Cache cacheMock = mock(Cache.class);
	private final BasicAuthRestInterceptor basicAuthRestInterceptor = new BasicAuthRestInterceptor(BASE_DN, SERVICE_USER_BASE_DN, SERVICE_USER_GROUP_DN, ldapTemplateMock, cacheManagerMock);


	@Test
	public void shouldAuthenticateOkNoCache() throws Exception {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
				.getBytes());
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		when(cacheManagerMock.getCache(CacheConfig.USERNAME_TOKEN_CACHE)).thenReturn(cacheMock);
		when(cacheMock.get(USERNAME, Integer.class)).thenReturn(null);
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.TRUE));
		verify(ldapTemplateMock, times(1)).authenticate(any(), eq(PASSWORD));
		verify(cacheMock, times(1)).put(eq(USERNAME), eq(Objects.hash(USERNAME, PASSWORD)));
	}

	@Test
	public void shouldAuthenticateOkWithCache() throws Exception {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
				.getBytes());
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		when(cacheManagerMock.getCache(CacheConfig.USERNAME_TOKEN_CACHE)).thenReturn(cacheMock);
		when(cacheMock.get(USERNAME, Integer.class)).thenReturn(Objects.hash(USERNAME, PASSWORD));
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.TRUE));
		verify(ldapTemplateMock, times(0)).authenticate(any(), eq(PASSWORD));
		verify(cacheMock, times(0)).put(eq(USERNAME), eq(Objects.hash(USERNAME, PASSWORD)));
	}

	@Test
	public void shouldNotAuthenticateOkNoCache() throws Exception {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
				.getBytes());
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		when(cacheManagerMock.getCache(CacheConfig.USERNAME_TOKEN_CACHE)).thenReturn(cacheMock);
		when(cacheMock.get(USERNAME, Integer.class)).thenReturn(null);
		doThrow(new RuntimeException()).when(ldapTemplateMock).authenticate(any(), any());
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.FALSE));
		verify(ldapTemplateMock, times(1)).authenticate(any(), eq(PASSWORD));
		verify(cacheMock, times(0)).put(eq(USERNAME), eq(Objects.hash(USERNAME, PASSWORD)));
	}

	@Test
	public void shouldNotAuthenticateOkWithCache() throws Exception {
		String basicAuthHeader = "Basic " + Base64Utils.encodeToString(String.format("%s:%s", USERNAME, PASSWORD)
				.getBytes());
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		when(cacheManagerMock.getCache(CacheConfig.USERNAME_TOKEN_CACHE)).thenReturn(cacheMock);
		when(cacheMock.get(USERNAME, Integer.class)).thenReturn(123);
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.FALSE));
		verify(ldapTemplateMock, times(0)).authenticate(any(), eq(PASSWORD));
		verify(cacheMock, times(0)).put(eq(USERNAME), eq(Objects.hash(USERNAME, PASSWORD)));
	}

	@Test
	public void shouldFailToAuthenticateNoBasicAuthHeader() throws Exception {
		String basicAuthHeader = "";
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.FALSE));
		verify(ldapTemplateMock, times(0)).authenticate(any(), eq(PASSWORD));
		verify(cacheManagerMock, times(0)).getCache(CacheConfig.USERNAME_TOKEN_CACHE);
	}

	@Test
	public void shouldFailToAuthenticateWrongTokenFormat1() throws Exception {
		String basicAuthHeader = "Basic notCorrectFormat";
		when(httpServletRequestMock.getHeader(AUTHORIZATION)).thenReturn(basicAuthHeader);
		boolean response = basicAuthRestInterceptor.preHandle(httpServletRequestMock, httpServletResponseMock, handlerMock);

		assertThat(response, is(Boolean.FALSE));
		verify(ldapTemplateMock, times(0)).authenticate(any(), eq(PASSWORD));
		verify(cacheManagerMock, times(0)).getCache(CacheConfig.USERNAME_TOKEN_CACHE);
	}
}