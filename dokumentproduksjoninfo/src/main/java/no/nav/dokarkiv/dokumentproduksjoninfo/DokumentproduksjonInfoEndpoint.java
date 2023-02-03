package no.nav.dokarkiv.dokumentproduksjoninfo;

import io.micrometer.core.annotation.Timed;
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

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.soap.MTOM;

/**
 * Implementation of JAX-WS-generated service interface DokumentproduksjonInfoV1. Bootstraps the
 * Spring context and delegates to Spring-managed DokumentProduksjonInfoProvider.
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

	private final DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider;

	public DokumentproduksjonInfoEndpoint(DokumentproduksjonInfoV1 dokumentproduksjonInfoProvider) {
		this.dokumentproduksjonInfoProvider = dokumentproduksjonInfoProvider;
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark120"}, percentiles = {0.5, 0.95})
	@Override
	public HentJournalOgDokumentStatusResponse hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequest request)
			throws HentJournalOgDokumentStatusJournalpostIkkeFunnet, HentJournalOgDokumentStatusDokumentInfoIkkeFunnet {
		return dokumentproduksjonInfoProvider.hentJournalOgDokumentStatus(request);
	}

	@Override
	public HentJournalpostInfoResponse hentJournalpostInfo(HentJournalpostInfoRequest hentJournalpostInfoRequest) throws HentJournalpostInfoJournalpostIkkeFunnet, HentJournalpostInfoDokumentInfoIkkeFunnet {
		throw new UnsupportedOperationException("hentFerdigstilteDokumenter er sanert 2023-02");
	}

	@Override
	public HentFerdigstilteDokumenterResponse hentFerdigstilteDokumenter(HentFerdigstilteDokumenterRequest request) {
		throw new UnsupportedOperationException("hentFerdigstilteDokumenter er sanert 2023-02");
	}

	@Override
	public void ping() {
		dokumentproduksjonInfoProvider.ping();
	}
}
