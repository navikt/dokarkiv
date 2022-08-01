package no.nav.dokarkiv.core.security;

import no.nav.dokarkiv.core.NavHeaders;
import no.nav.dokarkiv.core.security.ldap.NavLdapService;
import no.nav.dokarkiv.core.security.ldap.NavUser;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public class ValidateAdminConsumerAccessInterceptorTest {

	private final NavLdapService navLdapService = mock(NavLdapService.class);
	protected static final String OIDC_TOKEN_JOARKADMIN_USER_TEST = "Bearer eyJraWQiOiI5Y2ZkZDlkYS1lZTdmLTQ5NjItOGVkYy0wZTc2NzU1MGI1YzMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZqb2Fya2FkbWluIiwiYXVkIjpbInNydmpvYXJrYWRtaW4iLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTU1NDgwMzgwMywiYXpwIjoic3J2am9hcmthZG1pbiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTQ4MDM4MDMsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU1NDgwNzQwMywiaWF0IjoxNTU0ODAzODAzLCJqdGkiOiJlMzllMWViYS00ZjdjLTRhNmEtOTM3OC04MzU0NGFiYzI3NzAifQ.SSjNpiF9DHJPAVqrx_6CqCS9v46dPsO_7KiKdSdUDJPfY_kwusEtlnicmaU389kdvhS0_n4wzRONjrp7Ycoe7XU6fRh-vf0fw2XhbYzbNRY5DpI2vNZZ3j6sFGBdicPA10UPpMfgO26qU0Jnp9rtzpOwN9Y1ycay8SHOfyVJQPxOjaFIfSV5jgLQUYDg6HGcfZCb_b3R3vgcBt6QAQgBQXebtQdrnOswh6x-LynSt3NJGaZPTzZkhpCeIKHPJT_tsKf5DsBYcFo7G-m4KNjgInqqTilsnG9V2gVL5Q4pkLYCYva7o8ayT5Fls2GGV6z7dW_xuGc3ogwh7MVs5ll-Tw";
	protected static final String OIDC_TOKEN_DOKARKIV_USER_TEST = "Bearer eyJraWQiOiI5Y2ZkZDlkYS1lZTdmLTQ5NjItOGVkYy0wZTc2NzU1MGI1YzMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZkb2thcmtpdiIsImF1ZCI6WyJzcnZkb2thcmtpdiIsInByZXByb2QubG9jYWwiXSwidmVyIjoiMS4wIiwibmJmIjoxNTU0ODAzNzI2LCJhenAiOiJzcnZkb2thcmtpdiIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTQ4MDM3MjYsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU1NDgwNzMyNiwiaWF0IjoxNTU0ODAzNzI2LCJqdGkiOiJjNGNkNmFkNS1iNWY2LTQ4MzUtOWI4Zi1mZjVhMzlmOTk3MzMifQ.hTh-CiDzwBIfsdCB-NHhUDAlz9-NuvwS3Cu5Loj-vLYj8TZO7sW6Bjfo0CN6LYc84SdKUHaEZjHvwvAFnw39DQVOx_fQiIe07VBda67pNTGpxAWwEduOB7r_eUWZZDKlT3Ha8N6VbKWE20YC_JOm7WkxkOYJFxTvQ2RThe5HsFAe8NPQrCcXrN3dYe5ZWplWXkJOmk7Ijqov-iPf_fYne_AgwG-WV1pEM2tlKAuNcmkpvHGL2oKXaXeEoaW9_Mwvq27VkARceUVrwaMwoGi8MGeyPuikBGY_YGiChabaL9AHsmGiovntpDiEMUwMB0XC5ddPA5Vk4uDMbEIVq8BYxQ";
	protected static final String OIDC_TOKEN_ADMIN_USER = "Bearer eyAidHlwIjogIkpXVCIsICJraWQiOiAiMWwySmtDb1RMMTBibWVBeHlsZzR4Umk4ajJZPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJHOEtteUIyTTUzcHhWNnVmT1pDVUZnIiwgInN1YiI6ICJaOTkwMDY3IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJiZTBiNzA5OC0wN2Y1LTQ5MTQtYTE4Ni1mMjUwMGZiYmEzMDYtMTk2MDE2NjMiLCAiaXNzIjogImh0dHBzOi8vaXNzby1xLmFkZW8ubm86NDQzL2lzc28vb2F1dGgyIiwgInRva2VuTmFtZSI6ICJpZF90b2tlbiIsICJhdWQiOiAiam9hcmthZG1pbi1xMSIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjM0OTI2MTg1LWYxYjgtNDU5NC1hNTZkLTgwMmMwOGM0MjMxMCIsICJhenAiOiAiam9hcmthZG1pbi1xMSIsICJhdXRoX3RpbWUiOiAxNTU0ODAwMzk5LCAicmVhbG0iOiAiLyIsICJleHAiOiAxNTU0ODAzOTk5LCAidG9rZW5UeXBlIjogIkpXVFRva2VuIiwgImlhdCI6IDE1NTQ4MDAzOTkgfQ.OzzXswMdlPPRQBnArdEyTefLeTT6IiJ2VrzLo2eYu2_WgxPxUXzkKPS3P4i1X6uykInPQ6KWLljpw_Z2wXMcmN-_y5-ssALHmJ8a8h1L4NPuTAlAO1w8UedsxOQGdoZfxTnNZhQK2FmapG6AiOidZJJErb0KjtN4gedoTcMaKBzUQa-SI16wsq62clo1MYkXHLsDL4ovaFJTSfFPXMqqVoUVr8X6tuw4-QmtRZfxBKfB4iAowWadrR2J64h8k2Nw26_CBgOCX1iv6TWhrx56LKm95rF91_j6lZdySV6x9-F3YpPMfCM5HtF1x6XmVWo2pSU9h5ds3V4qcRbLvJF1cg";
	private final ValidateAdminConsumerAccessInterceptor validateAdminConsumerAccessInterceptor = new ValidateAdminConsumerAccessInterceptor(navLdapService);

	@Test
	public void shouldAllowAccessToJoarkadminServiceUserAndNavUserMemberOfJoarkVedlikeholdGroup() throws Exception {
		when(navLdapService.findByUserId(any())).thenReturn(
				NavUser.builder()
						.memberOf(new HashSet<>(Arrays.asList("0000-GA-joark-vedlikehold")))
						.userId("Z990782")
						.userExistsInLdap(true)
						.build()
		);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_ADMIN_USER);
		request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, OIDC_TOKEN_JOARKADMIN_USER_TEST);
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = validateAdminConsumerAccessInterceptor.preHandle(request, response, new Object());
		assertThat(result, is(true));

	}

	@Test
	public void shouldDenyAccessToNonJoarkAdminConsumerUserWhenOnlyAuthorizationTokenIsPresent() throws Exception {
		when(navLdapService.findByUserId(any())).thenReturn(
				NavUser.builder()
						.memberOf(new HashSet<>(Arrays.asList("0000-GA-joark-vedlikehold")))
						.userId("Z990782")
						.userExistsInLdap(true)
						.build()
		);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_DOKARKIV_USER_TEST);
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = validateAdminConsumerAccessInterceptor.preHandle(request, response, new Object());
		assertThat(result, is(false));
		assertThat(response.getErrorMessage(), is("OIDC token på Authorization-header må tilhøre servicebruker på srvjoarkadmin"));

	}


	@Test
	public void shouldDenyAccessToNonJoarkAdminConsumerUser() throws Exception {
		when(navLdapService.findByUserId(any())).thenReturn(
				NavUser.builder()
						.memberOf(new HashSet<>(Arrays.asList("0000-GA-joark-vedlikehold")))
						.userId("Z990782")
						.userExistsInLdap(true)
						.build()
		);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_ADMIN_USER);
		request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, OIDC_TOKEN_DOKARKIV_USER_TEST);
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = validateAdminConsumerAccessInterceptor.preHandle(request, response, new Object());
		assertThat(result, is(false));
		assertThat(response.getErrorMessage(), is("OIDC token på Nav-Consumer-Token header må tilhøre serviceuser på srvjoarkadmin"));

	}


	@Test
	public void shouldDenyAccessToNavUserNotMemberOfJoarkVedlikehold() throws Exception {
		when(navLdapService.findByUserId(any())).thenReturn(
				NavUser.builder()
						.memberOf(new HashSet<>(Arrays.asList("0000-GA-NOTHING")))
						.userId("Z990782")
						.userExistsInLdap(true)
						.build()
		);

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_ADMIN_USER);
		request.addHeader(NavHeaders.NAV_CONSUMER_TOKEN, OIDC_TOKEN_JOARKADMIN_USER_TEST);
		MockHttpServletResponse response = new MockHttpServletResponse();
		boolean result = validateAdminConsumerAccessInterceptor.preHandle(request, response, new Object());
		assertThat(result, is(false));
		assertThat(response.getErrorMessage(), is("Bruker må være medlem av gruppen \"0000-GA-joark-vedlikehold\""));
	}

}