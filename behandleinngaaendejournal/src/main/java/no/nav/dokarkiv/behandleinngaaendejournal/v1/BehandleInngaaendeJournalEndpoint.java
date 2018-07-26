package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringFerdigstillingIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.FerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostOppdateringIkkeMulig;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.OppdaterJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.FerdigstillJournalfoeringRequest;
import no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.meldinger.OppdaterJournalpostRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1",
	wsdlLocation = "WEB-INF/wsdl/no/nav/tjeneste/virksomhet/behandleInngaaendeJournal/v1/behandleInngaaendeJournal.wsdl",
	targetNamespace = "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1",
	serviceName = "BehandleInngaaendeJournal_v1",
	portName = "BehandleInngaaendeJournal_v1Port")
@Addressing
@HandlerChain(file = "classpath:behandleinngaaendejournalv1handler.xml")
@Service
public class BehandleInngaaendeJournalEndpoint implements BehandleInngaaendeJournalV1 {

	private static final String DEFAULT_APPID = "joark:BehandleInngaaendeJournal_v1";

	@Inject
	private BehandleInngaaendeJournalV1 behandleInngaaendeJournalProvider;

	@Override
	public void ferdigstillJournalfoering(FerdigstillJournalfoeringRequest request)
			throws FerdigstillJournalfoeringFerdigstillingIkkeMulig, FerdigstillJournalfoeringJournalpostIkkeInngaaende,
			FerdigstillJournalfoeringObjektIkkeFunnet, FerdigstillJournalfoeringSikkerhetsbegrensning,
			FerdigstillJournalfoeringUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);
	}

	@Override
	public void oppdaterJournalpost(OppdaterJournalpostRequest request)
			throws OppdaterJournalpostJournalpostIkkeInngaaende, OppdaterJournalpostObjektIkkeFunnet,
			OppdaterJournalpostOppdateringIkkeMulig, OppdaterJournalpostSikkerhetsbegrensning,
			OppdaterJournalpostUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		behandleInngaaendeJournalProvider.oppdaterJournalpost(request);
	}
	
	@Override
	public void ping() {
		behandleInngaaendeJournalProvider.ping();
	}
    
    private String consumerIdFromMdcOrDefault() {
		String consumerId = MDC.get(MDC_CONSUMER_ID);
		if(!StringUtils.isBlank(consumerId)) {
			return consumerId;
		} else {
			return DEFAULT_APPID;
		}
	}
}
