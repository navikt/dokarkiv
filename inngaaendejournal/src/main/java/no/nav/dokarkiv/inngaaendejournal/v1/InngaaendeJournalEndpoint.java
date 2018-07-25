package no.nav.dokarkiv.inngaaendejournal.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.HentJournalpostUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovJournalpostKanIkkeBehandles;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.UtledJournalfoeringsbehovUgyldigInput;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.HentJournalpostResponse;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovRequest;
import no.nav.tjeneste.virksomhet.inngaaendejournal.v1.meldinger.UtledJournalfoeringsbehovResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1",
		wsdlLocation = "WEB-INF/wsdl/no/nav/tjeneste/virksomhet/inngaaendeJournal/v1/inngaaendeJournal.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/inngaaendeJournal/v1",
		serviceName = "InngaaendeJournal_v1",
		portName = "InngaaendeJournal_v1Port")
@Addressing
@HandlerChain(file = "InngaaendeJournalHandler.xml")
@Service
public class InngaaendeJournalEndpoint implements InngaaendeJournalV1 {

	private static final String DEFAULT_APPID = "joark:InngaaendeJournal_v1";

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private InngaaendeJournalV1 inngaaendeJournalProvider;

	@Override
	public HentJournalpostResponse hentJournalpost(HentJournalpostRequest request)
			throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
			HentJournalpostSikkerhetsbegrensning, HentJournalpostUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return inngaaendeJournalProvider.hentJournalpost(request);
	}


	@Override
	public UtledJournalfoeringsbehovResponse utledJournalfoeringsbehov(UtledJournalfoeringsbehovRequest request)
			throws UtledJournalfoeringsbehovJournalpostIkkeFunnet, UtledJournalfoeringsbehovJournalpostIkkeInngaaende,
			UtledJournalfoeringsbehovJournalpostKanIkkeBehandles, UtledJournalfoeringsbehovSikkerhetsbegrensning,
			UtledJournalfoeringsbehovUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return inngaaendeJournalProvider.utledJournalfoeringsbehov(request);
	}

	@Override
	public void ping() {
		inngaaendeJournalProvider.ping();
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
