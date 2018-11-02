package no.nav.dokarkiv.behandlejournal.v2;

import io.micrometer.core.annotation.Timed;
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
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/behandleJournal/v2/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/behandleJournal/v2/Binding",
		serviceName = "BehandleJournal_v2",
		portName = "behandleJournal_v2Port")
@Addressing
@HandlerChain(file = "classpath:behandlejournalv2.xml")
@Service
public class BehandleJournalEndpoint implements BehandleJournalV2 {

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private BehandleJournalV2 behandleJournalProvider;

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark060"}, percentiles = {0.5, 0.95})
	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.arkiverUstrukturertKrav(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark061"}, percentiles = {0.5, 0.95})
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.lagreVedleggPaaJournalpost(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark062"}, percentiles = {0.5, 0.95})
	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest request)
			throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		behandleJournalProvider.ferdigstillDokumentopplasting(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark065"}, percentiles = {0.5, 0.95})
	@Override
	public JournalfoerNotatResponse journalfoerNotat(
			JournalfoerNotatRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerNotat(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark064"}, percentiles = {0.5, 0.95})
	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerUtgaaendeHenvendelse(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark063"}, percentiles = {0.5, 0.95})
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