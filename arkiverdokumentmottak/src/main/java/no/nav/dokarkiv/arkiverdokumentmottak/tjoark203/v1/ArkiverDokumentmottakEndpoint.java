package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v1;


import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

@WebService(endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v1.ArkiverDokumentmottakV1",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v1/arkiverdokumentmottak.wsdl",
		targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v1/",
		serviceName = "ArkiverDokumentmottakService_v1",
		portName = "ArkiverDokumentmottakPort_v1")
@Addressing
@HandlerChain(file = "classpath:tjoark203/v1/arkiverdokumentmottakhandler.xml")
@Service
public class ArkiverDokumentmottakEndpoint implements ArkiverDokumentmottakV1 {

	private static final String DEFAULT_APPID = "Dokmot";

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private ArkiverDokumentmottakV1 arkiverDokumentMottakProvider;

	@Override
	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark203_v1"}, percentiles = {0.5, 0.95})
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DEFAULT_APPID);
		return arkiverDokumentMottakProvider.journalforInngaaendeForsendelse(request);
	}

	@Override
	public void ping() {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DEFAULT_APPID);
		arkiverDokumentMottakProvider.ping();
	}


}
