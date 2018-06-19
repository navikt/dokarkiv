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
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;

/**
 * Implementation of JAX-WS-generated service interface DokumentproduksjonInfoV1. Bootstraps the
 * Spring context and delegates to Spring-managed DokumentProduksjonInfoProvider.
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@WebService(endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/domene/brevogarkiv/dokumentproduksjoninfo/v1/dokumentproduksjoninfo.wsdl",
		targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/dokumentproduksjoninfo/v1/",
		serviceName = "DokumentproduksjonInfo_v1",
		portName = "DokumentproduksjonInfoPort_v1")
@Addressing
@MTOM(enabled = true)
@HandlerChain(file = "classpath:dokumentproduksjoninfo-handler.xml")
@Service
public class DokumentproduksjonInfoEndpoint implements DokumentproduksjonInfoV1 {

	@Inject
	private DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;

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
}
