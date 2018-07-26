package no.nav.dokarkiv.core.security.ldap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Tore Gard Andersen
 */
@Slf4j
@Component
public class BrukernavnLdapService {
	private static final String ERROR_FORMAT = "%s=%s error=%s";
	
	
//	private RetryTemplate retryTemplate; FIXME
	private LdapLookup ldapLookup;


//	@Inject
//	public BrukernavnLdapService(RetryTemplate retryTemplate, LdapLookup ldapLookup) {
//		this.retryTemplate = retryTemplate;
//		this.ldapLookup = ldapLookup;
//	}

	public String searchWithRetry(final String userId) {
		LdapResponse ldapResponse;
		final LdapRetrySearchWrapper ldapRetrySearchWrapper = new LdapRetrySearchWrapper(userId, ldapLookup);
		try {
//			ldapResponse = retryTemplate.execute(
//					new RetryCallback<LdapResponse>() {
//						@Override
//						public LdapResponse doWithRetry(RetryContext retryContext) throws Exception {
//							return ldapRetrySearchWrapper.invoke();
//						}
//					}, new RecoveryCallback<LdapResponse>() {
//						@Override
//						public LdapResponse recover(RetryContext retryContext) throws Exception {
//							log.warn(String.format(ERROR_FORMAT, "Ikke treff i AD for userId", userId, retryContext.toString()));
//							return new LdapResponse(userId, null);
//						}
//					}
//			);
		} catch (Exception e) {
			log.warn("Unknown error when ldapLookup for userId=" + userId, e);
			ldapResponse = new LdapResponse(userId, null);
		}
//		return ldapResponse.or(userId);
		return null;
	}

}
