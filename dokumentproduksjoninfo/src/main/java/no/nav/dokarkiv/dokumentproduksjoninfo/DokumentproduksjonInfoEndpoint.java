package no.nav.dokarkiv.dokumentproduksjoninfo;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;

/**
 * Implementation of JAX-WS-generated service interface DokumentproduksjonInfoV1. Bootstraps the
 * Spring context and delegates to Spring-managed DokumentProduksjonInfoProvider.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@WebService(endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1",
		wsdlLocation = "WEB-INF/wsdl/no/nav/tjeneste/domene/brevogarkiv/dokumentproduksjoninfo/v1/dokumentproduksjoninfo.wsdl",
		targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/dokumentproduksjoninfo/v1/",
		serviceName = "DokumentproduksjonInfo_v1",
		portName = "DokumentproduksjonInfoPort_v1")
@Addressing
@MTOM(enabled = true)
@HandlerChain(file = "handler.xml")
public class DokumentproduksjonInfoEndpoint implements DokumentproduksjonInfoV1 {

	private DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;

	@Resource
	private WebServiceContext webServiceContext;

	@Override
	public HentJournalOgDokumentStatusResponse hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequest request)
			throws HentJournalOgDokumentStatusJournalpostIkkeFunnet, HentJournalOgDokumentStatusDokumentInfoIkkeFunnet {
		return dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request);
	}

	@Override
	public HentJournalpostInfoResponse hentJournalpostInfo(HentJournalpostInfoRequest hentJournalpostInfoRequest) throws HentJournalpostInfoJournalpostIkkeFunnet, HentJournalpostInfoDokumentInfoIkkeFunnet {
		return dokumentproduksjonInfoProvider.hentJournalpostInfo(hentJournalpostInfoRequest);
	}

	@Override
	public HentFerdigstilteDokumenterResponse hentFerdigstilteDokumenter(HentFerdigstilteDokumenterRequest request) {
		return dokumentproduksjonInfoProvider.hentFerdigstilteDokumenter(request);
	}
	
	@Override
	public void ping() {
		dokumentproduksjonInfoProvider.ping();
	}

	/**
	 * Retrieve the dokumentproduksjonInfoProvider bean from the Spring context.
	 */
	@PostConstruct
	public void initDokumentproduksjonInfoProvider() {
//		ServletContext servletContext = (ServletContext) webServiceContext.getMessageContext().get(
//				MessageContext.SERVLET_CONTEXT);
//		WebApplicationContext webApplicationContext = WebApplicationContextUtils
//				.getRequiredWebApplicationContext(servletContext);
//		dokumentproduksjonInfoProvider = (DokumentproduksjonInfoV1) webApplicationContext
//				.getBean(DokumentproduksjonInfoConfig.PROVIDER_BEAN);
	}


}
