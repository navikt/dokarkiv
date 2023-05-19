package no.nav.dokarkiv.core.security.abac;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.logging.AbacLogger;
import no.nav.dokarkiv.core.repository.JournalpostRepositorySkjermet;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.freg.abac.core.dto.response.XacmlResponse;
import no.nav.freg.abac.core.service.AbacService;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_GSAK_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_PENSJON_SAKSID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_TILKNYTTET_FNR;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_TEMA;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
public class AbacSecurityService {

	private static final String ACCESS_DENIED_TO_JOURNALPOST = "Bruker har ikke tilgang til journalpost";

	private final AbacLogger abaclog;
	private final AbacService abacService;
	private final AbacContext abacContext;
	private final JdbcAbacSecurityRepository jdbcAbacSecurityRepository;
	private final JournalpostRepositorySkjermet journalpostRepositorySkjermet;

	public AbacSecurityService(AbacLogger abaclog, AbacService abacService,
							   AbacContext abacContext, JdbcAbacSecurityRepository jdbcAbacSecurityRepository,
							   JournalpostRepositorySkjermet journalpostRepositorySkjermet) {
		this.abaclog = abaclog;
		this.abacService = abacService;
		this.abacContext = abacContext;
		this.jdbcAbacSecurityRepository = jdbcAbacSecurityRepository;
		this.journalpostRepositorySkjermet = journalpostRepositorySkjermet;
	}

	public void assertAccessToJournalpost(String journalpost) {
		Long journalpostId = Long.parseLong(journalpost);

		if (!journalpostRepositorySkjermet.existsById(journalpostId)) {
			throw new JournalpostIkkeFunnetException("Journalpost ikke funnet. journalpostId=" + journalpostId);
		}

		submitJournalpostParametersAndHandleResponse(journalpostId);
	}

	private void submitJournalpostParametersAndHandleResponse(Long journalpostId) {
		AbacResources abacResources = jdbcAbacSecurityRepository.findAbacResources(journalpostId);
		decorateJoarkResources(abacContext.getRequest(), abacResources, journalpostId);
		XacmlResponse accessResponse = abacService.evaluate(abacContext.getRequest());
		handleResponseForJournalpostId(abacContext.getRequest(), accessResponse, journalpostId);
	}

	XacmlRequest decorateJoarkResources(XacmlRequest request,
										AbacResources joarkResources, Long journalpostId) {
		if (journalpostId != null && !joarkResources.getBrukerIds().isEmpty() && joarkResources.getBrukerIds().size() > 1) {
			log.warn("Requested access to journalpost with multiple users, journalpostId={}", journalpostId);
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

	private void handleResponseForJournalpostId(XacmlRequest request, XacmlResponse response, Long journalpostId) {
		final Map<String, String> resources = new HashMap<>();
		resources.put("journalpost_id", journalpostId.toString());
		if (response.getDecision() == Decision.DENY) {
			abaclog.logAbacDeny(request, response, resources);
			throw new AuthorizationException(ACCESS_DENIED_TO_JOURNALPOST);
		} else {
			if (!isEmpty(response.getAdvices())) {
				abaclog.logAbacPermit(request, response, resources);
			}
		}
	}
}
