package no.nav.dokarkiv.behandlejournal.v3;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.core.domain.codes.BrukerTypeCode;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Bruker;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.logging.AbacLogger;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlAttribute;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_GSAK_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;

@Component
public class BehandleJournalV3Pep {
	private final AbacService abacService;
	private final AbacContext abacContext;
	private final AbacLogger abacLogger;

	public BehandleJournalV3Pep(AbacService abacService, AbacContext abacContext, AbacLogger abacLogger) {
		this.abacService = abacService;
		this.abacContext = abacContext;
		this.abacLogger = abacLogger;
	}

	public void journalfoerNotatPep(JournalfoerNotatHenvendelseRequest request) {
		final Journalpost journalpost = request.getJournalpost();
		XacmlRequest xacmlRequest = abacContext.getRequest();

		setAbacEnvironment(abacContext.getRequest());
		enrichPolicySekundaerPerson(journalpost, xacmlRequest);
		enrichPolicyIngenTilgangTilPensjonssaker(journalpost, xacmlRequest);
		enrichTema(journalpost, xacmlRequest);

		decide(xacmlRequest, abacService.evaluate(xacmlRequest));
	}

	private void decide(XacmlRequest xacmlRequest, XacmlResponse xacmlResponse) {
		if(xacmlResponse.getDecision() == Decision.DENY) {
			abacLogger.logAbacDeny(xacmlRequest, xacmlResponse, new HashMap<>());
			throw new AuthorizationException("Bruker har ikke tilgang til journalpost");
		} else {
			if(!xacmlResponse.getAdvices().isEmpty()) {
				abacLogger.logAbacPermit(xacmlRequest, xacmlResponse, new HashMap<>());
			}
		}
	}

	// https://confluence.adeo.no/pages/viewpage.action?pageId=239343219
	private void enrichPolicySekundaerPerson(Journalpost journalpost, XacmlRequest xacmlRequest) {
		if (journalpost.getBrukere() != null && journalpost.getBrukere().size() == 1) {
			Bruker bruker = journalpost.getBrukere().iterator().next();
			if(BrukerTypeCode.PERSON == bruker.getBrukerType()) {
				xacmlRequest.resource(RESOURCE_FELLES_PERSON_TILKNYTTET_FNR, bruker.getBrukerId());
			}
		}
	}

	// https://confluence.adeo.no/display/ABAC/Ingen+tilgang+til+pensjons+saker
	private void enrichPolicyIngenTilgangTilPensjonssaker(Journalpost journalpost, XacmlRequest xacmlRequest) {
		if (journalpost.getSaksrelasjon() != null && journalpost.getSaksrelasjon().getSakId() != null) {
			if (FagsystemCode.PEN.equals(journalpost.getSaksrelasjon().getFagsystem())) {
				xacmlRequest.resource(RESOURCE_ARKIV_PENSJON_SAKSID, journalpost.getSaksrelasjon().getSakId().toString());
			} else {
				xacmlRequest.resource(RESOURCE_ARKIV_GSAK_SAKSID, journalpost.getSaksrelasjon().getSakId().toString());
			}
		}
	}

	// https://confluence.adeo.no/display/ABAC/Tema
	private void enrichTema(Journalpost journalpost, XacmlRequest xacmlRequest) {
		if (journalpost.getFagomrade() != null) {
			xacmlRequest.resource(RESOURCE_FELLES_TEMA, journalpost.getFagomrade().name());
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

}
