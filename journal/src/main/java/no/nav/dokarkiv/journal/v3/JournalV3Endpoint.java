package no.nav.dokarkiv.journal.v3;


import static no.nav.dokarkiv.core.MDCConstants.MDC_CONSUMER_ID;

import no.nav.dokarkiv.core.MDCConstants;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;

@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.journal.v3.JournalV3",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/journal/v3/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/journal/v3",
		serviceName = "Journal_v3",
		portName = "Journal_v3Port")
@Addressing
@HandlerChain(file = "classpath:joarkv3handler.xml")
public class JournalV3Endpoint implements JournalV3 {

	private static final String DEFAULT_APPID = "joark:Journal_v3";

	@Inject
	private JournalV3 journalV3Provider;

	@Override
	public HentKjerneJournalpostListeResponse hentKjerneJournalpostListe(HentKjerneJournalpostListeRequest request)
			throws HentKjerneJournalpostListeUgyldigInput, HentKjerneJournalpostListeSikkerhetsbegrensning {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return journalV3Provider.hentKjerneJournalpostListe(request);
	}

	@Override
	public no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse hentDokument(
			no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest request)
			throws HentDokumentJournalpostIkkeFunnet,
			no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet,
			no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return journalV3Provider.hentDokument(request);
	}

	@Override
	public HentDokumentURLResponse hentDokumentURL(HentDokumentURLRequest hentDokumentURLRequest)
			throws HentDokumentURLDokumentIkkeFunnet, HentDokumentURLSikkerhetsbegrensning {
		String userId = MDC.get(MDCConstants.MDC_USER_ID);
		RequestContextUtil.createAndSetUsername(userId, consumerIdFromMdcOrDefault());
		return journalV3Provider.hentDokumentURL(hentDokumentURLRequest);
	}

	@Override
	public void ping() {
		journalV3Provider.ping();
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
