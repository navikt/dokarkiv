package no.nav.dokarkiv.behandlejournal.v3;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class BehandleJournalV3Pep {
	private final AbacService abacService;
	private final AbacContext abacContext;

	@Inject
	public BehandleJournalV3Pep(AbacService abacService, AbacContext abacContext) {
		this.abacService = abacService;
		this.abacContext = abacContext;
	}

	public void journalfoerNotatPep(JournalfoerNotatHenvendelseRequest request) {
		final Journalpost journalpost = request.getJournalpost();
		XacmlRequest xacmlRequest = abacContext.getRequest();

		enrichPolicyIngenTilgangTilPensjonssaker(journalpost, xacmlRequest);
		enrichTema(journalpost, xacmlRequest);

		decide(abacService.evaluate(xacmlRequest));
	}

	private void decide(XacmlResponse xacmlResponse) {
		if(xacmlResponse.getDecision() == Decision.DENY) {
			// FIXME abac log
			throw new AuthorizationException("Bruker har ikke tilgang til journalpost");
		}
	}

	// https://confluence.adeo.no/display/ABAC/Tema
	private void enrichTema(Journalpost journalpost, XacmlRequest xacmlRequest) {
		if (journalpost.getFagomrade() != null) {
			xacmlRequest.resource(RESOURCE_FELLES_TEMA, journalpost.getFagomrade().name());
		}
	}

	// https://confluence.adeo.no/display/ABAC/Ingen+tilgang+til+pensjons+saker
	private void enrichPolicyIngenTilgangTilPensjonssaker(Journalpost journalpost, XacmlRequest xacmlRequest) {
		if (journalpost.getSaksrelasjon() != null && isNotEmpty(journalpost.getSaksrelasjon().getSakId())) {
			if (FagsystemCode.PEN.equals(journalpost.getSaksrelasjon().getFagsystem())) {
				xacmlRequest.resource(RESOURCE_ARKIV_PENSJON_SAKSID, journalpost.getSaksrelasjon().getSakId());
			}
		}
	}
}
