package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.LagreVedleggPaaJournalpostResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

/**
 * Implementation of JAX-WS-generated service interface BehandleJournalPortType. Bootstraps the
 * Spring context and delegates to Spring-managed BehandleJournalProvider.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.BehandleJournalV2",
		wsdlLocation = "WEB-INF/wsdl/no/nav/tjeneste/virksomhet/behandleJournal/v2/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/behandleJournal/v2/",
		serviceName = "BehandleJournal_v2",
		portName = "BehandleJournal_v2")
@Addressing
@HandlerChain(file = "handler.xml")
@Service
public class BehandleJournalEndpoint implements BehandleJournalV2 {

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private BehandleJournalV2 behandleJournalProvider;

	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.arkiverUstrukturertKrav(request);
	}

	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.lagreVedleggPaaJournalpost(request);
	}

	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest request)
			throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		behandleJournalProvider.ferdigstillDokumentopplasting(request);
	}

	@Override
	public JournalfoerNotatResponse journalfoerNotat(
			JournalfoerNotatRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerNotat(request);
	}

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerUtgaaendeHenvendelse(request);
	}

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerInngaaendeHenvendelse(request);
	}

	@Override
	public void ping() {
		behandleJournalProvider.ping();
	}
}