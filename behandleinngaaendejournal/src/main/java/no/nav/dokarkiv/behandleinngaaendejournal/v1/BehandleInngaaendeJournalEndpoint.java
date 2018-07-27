package no.nav.dokarkiv.behandleinngaaendejournal.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import io.micrometer.core.annotation.Timed;
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

@WebService(targetNamespace = "http://nav.no/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding",
		serviceName = "BehandleInngaaendeJournal_v1",
		portName = "BehandleInngaaendeJournal_v1Port",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/behandleInngaaendeJournal/v1/Binding.wsdl",
		endpointInterface = "no.nav.tjeneste.virksomhet.behandleinngaaendejournal.v1.binding.BehandleInngaaendeJournalV1")
@Addressing
@HandlerChain(file = "classpath:behandleinngaaendejournalv1handler.xml")
@Service
public class BehandleInngaaendeJournalEndpoint implements BehandleInngaaendeJournalV1 {

	private static final String DEFAULT_APPID = "joark:BehandleInngaaendeJournal_v1";

	@Inject
	private BehandleInngaaendeJournalV1 behandleInngaaendeJournalProvider;

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark067"}, percentiles = {0.5, 0.95})
	@Override
	public void ferdigstillJournalfoering(FerdigstillJournalfoeringRequest request)
			throws FerdigstillJournalfoeringFerdigstillingIkkeMulig, FerdigstillJournalfoeringJournalpostIkkeInngaaende,
			FerdigstillJournalfoeringObjektIkkeFunnet, FerdigstillJournalfoeringSikkerhetsbegrensning,
			FerdigstillJournalfoeringUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		behandleInngaaendeJournalProvider.ferdigstillJournalfoering(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark066"}, percentiles = {0.5, 0.95})
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
		if (!StringUtils.isBlank(consumerId)) {
			return consumerId;
		} else {
			return DEFAULT_APPID;
		}
	}
}
