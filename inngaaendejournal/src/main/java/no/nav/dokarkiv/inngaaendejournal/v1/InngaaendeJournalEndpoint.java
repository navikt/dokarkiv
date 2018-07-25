package no.nav.dokarkiv.inngaaendejournal.v1;

import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import io.micrometer.core.annotation.Timed;
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

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.inngaaendejournal.v1.binding.InngaaendeJournalV1",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/inngaaendeJournal/v1/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/inngaaendeJournal/v1/Binding",
		serviceName = "InngaaendeJournal_v1",
		portName = "InngaaendeJournal_v1Port")
@Addressing
@HandlerChain(file = "classpath:inngaaendejournalv1handler.xml")
@Service
public class InngaaendeJournalEndpoint implements InngaaendeJournalV1 {

	private static final String DEFAULT_APPID = "joark:InngaaendeJournal_v1";

	@Inject
	private InngaaendeJournalV1 inngaaendeJournalProvider;

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark056"}, percentiles = {0.5, 0.95})
	@Override
	public HentJournalpostResponse hentJournalpost(HentJournalpostRequest request)
			throws HentJournalpostJournalpostIkkeFunnet, HentJournalpostJournalpostIkkeInngaaende,
			HentJournalpostSikkerhetsbegrensning, HentJournalpostUgyldigInput {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return inngaaendeJournalProvider.hentJournalpost(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark057"}, percentiles = {0.5, 0.95})
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
