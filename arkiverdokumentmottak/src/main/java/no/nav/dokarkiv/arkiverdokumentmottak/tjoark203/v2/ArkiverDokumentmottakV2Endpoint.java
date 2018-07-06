package no.nav.dokarkiv.arkiverdokumentmottak.tjoark203.v2;

import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.KanIkkeJournalfores;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.meldinger.JournalforInngaaendeForsendelseResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

;

/**
 * Endpoint class for TJOARK203
 *
 * @author Sigurd Midttun, Visma Consulting.
 */
@WebService(endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentmottak.v2.ArkiverDokumentmottakV2",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v2/arkiverdokumentmottak.wsdl",
		targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/arkiverdokumentmottak/v2/",
		serviceName = "ArkiverDokumentmottakService_v2",
		portName = "ArkiverDokumentmottakPort_v2")
@Addressing
@HandlerChain(file = "classpath:tjoark203/v2/arkiverdokumentmottakhandler.xml")
@Service
public class ArkiverDokumentmottakV2Endpoint implements ArkiverDokumentmottakV2 {

	private static final String DEFAULT_APPID = "Dokmot";

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private ArkiverDokumentmottakV2 arkiverDokumentmottakV2Provider;

	@Override
	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark203_v2"}, percentiles = {0.5, 0.95})
	public JournalforInngaaendeForsendelseResponse journalforInngaaendeForsendelse(
			JournalforInngaaendeForsendelseRequest request) throws KanIkkeJournalfores {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DEFAULT_APPID);
		return arkiverDokumentmottakV2Provider.journalforInngaaendeForsendelse(request);
	}

	@Override
	public void ping() {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DEFAULT_APPID);
		arkiverDokumentmottakV2Provider.ping();
	}
}
