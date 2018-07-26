package no.nav.dokarkiv.core.security.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * @author Tore Gard Andersen
 */
public class BrukernavnLdapService {
	private static final Logger log = LoggerFactory.getLogger(BrukernavnLdapService.class);
	private static final String ERROR_FORMAT = "%s=%s error=%s";
	
	
//	private RetryTemplate retryTemplate; FIXME
	private LdapLookup ldapLookup;


	@Inject
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
