package no.nav.dokarkiv.core.security.abac;

import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_GSAK_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;
import static org.springframework.util.CollectionUtils.isEmpty;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.repository.JoarkRepository;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlAttribute;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Martin Burheim Tingstad, Visma Consulting AS
 */
@Component
public class AbacSecurityService {

	public static final String ACCESS_DENIED_TO_JOURNALPOST = "Bruker har ikke tilgang til journalpost";
	public static final String ACCESS_DENIED = "Access Denied";
	public static final String ABAC_SECURITY_SERVICE = "AbacSecurityService";

	@Inject
	private AbacLoggingUtils abaclog;

	@Inject
	private AbacService abacService;

	@Inject
	private AbacContext abacContext;

	@Inject
	private JdbcAbacSecurityRepository jdbcAbacSecurityRepository;

	@Inject
	private JoarkRepository joarkRepository;

	public void assertAccessToJournalpost(String journalpost) {
		Long journalpostId = Long.parseLong(journalpost);

		if (!joarkRepository.existsById(journalpostId)) {
			throw new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + journalpostId);
		}

		setAbacEnvironment(abacContext.getRequest());
		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(journalpostId);
		decorateJoarkResources(abacContext.getRequest(), abacResources, journalpostId);
		XacmlResponse accessResponse = abacService.evaluate(abacContext.getRequest());
		handleResponseForJournalpostId(accessResponse, journalpostId);
	}

	public Decision assertAccessToSak(String sakId, FagsystemCode fagsystemCode) {
		return assertAccessToSak(abacContext.getRequest(), sakId, fagsystemCode);
	}

	public Decision assertAccessToSak(XacmlRequest abacRequest, String sakId, FagsystemCode fagsystemCode) {
		setAbacEnvironment(abacRequest);
		AbacResources abacResources = new AbacResources();
		abacResources.setFagsystem(fagsystemCode);
		abacResources.setSakId(sakId);
		decorateJoarkResources(abacRequest, abacResources, null);
		XacmlResponse accessResponse = abacService.evaluate(abacRequest);
		return handleResponseForSakId(accessResponse, abacResources);
	}

	XacmlRequest decorateJoarkResources(XacmlRequest request,
										AbacResources joarkResources, Long journalpostId) {
		if (journalpostId != null && !joarkResources.getBrukerIds().isEmpty() && joarkResources.getBrukerIds().size() > 1) {
			abaclog.logAccessToJournalpostWithSeveralUsers(journalpostId);
		}

		if (joarkResources.getBrukerIds() != null && joarkResources.getBrukerIds().size() == 1) {
			request.resource(RESOURCE_FELLES_PERSON_TILKNYTTET_FNR,
					joarkResources.getBrukerIds().get(0));
		}

		if (StringUtils.isNotEmpty(joarkResources.getSakId())) {
			if (FagsystemCode.PEN.equals(joarkResources.getFagsystem())) {
				request.resource(RESOURCE_ARKIV_PENSJON_SAKSID, joarkResources.getSakId());
			} else if (joarkResources.getFagsystem() != null) {
				request.resource(RESOURCE_ARKIV_GSAK_SAKSID, joarkResources.getSakId());
			}
		}

		if (joarkResources.getFagomrade() != null) {
			request.resource(RESOURCE_FELLES_TEMA, joarkResources.getFagomrade().name());
		}
		return request;
	}

	private void handleResponseForJournalpostId(XacmlResponse response, Long journalpostId) {
		if (response.getDecision() == Decision.DENY) {
			abaclog.logAccessDeniedToJournalpost(journalpostId);
			throw new AuthorizationException(ACCESS_DENIED_TO_JOURNALPOST);
		} else {
			if (!isEmpty(response.getAdvices())) {
				abaclog.logAccessToJournalpostWithAdvice(journalpostId);
			}
		}
	}

	private Decision handleResponseForSakId(XacmlResponse response, AbacResources abacResources) {
		if (response.getDecision() == Decision.DENY) {
			abaclog.logAttemptedAccessToSak(abacResources.getSakId(), abacResources.getFagsystem());
			return response.getDecision();
		} else {
			if (!isEmpty(response.getAdvices())) {
				abaclog.logAccessToSakdWithAdvice(abacResources.getSakId(), abacResources.getFagsystem());
			}
			return response.getDecision();
		}
	}

	/**
	 * By default, both ENVIRONMENT_FELLES_SAML_TOKEN and ENVIRONMENT_FELLES_OIDC_TOKEN_BODY is set as environment in
	 * AbacDefaultConfig.java. At that point we do not know whether the incomming request is a SOAP or a REST request.
	 * At this point we know, because either the value of ENVIRONMENT_FELLES_SAML_TOKEN or the value of ENVIRONMENT_FELLES_OIDC_TOKEN_BODY
	 * should have been set, depending on the type of the incoming request.
	 **/
	private void setAbacEnvironment(XacmlRequest request) {
		XacmlAttribute oidcTokenAttribute = request.getEnvironment().get(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY);

		if (oidcTokenAttribute != null && oidcTokenAttribute.getValue().toString().isEmpty()) {
			request.getEnvironment().remove(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY);
		} else if (oidcTokenAttribute != null) {
			request.getEnvironment().remove(ENVIRONMENT_FELLES_SAML_TOKEN);
		}
	}

	void setAbacContext(AbacContext abacContext) {
		this.abacContext = abacContext;
	}

}
