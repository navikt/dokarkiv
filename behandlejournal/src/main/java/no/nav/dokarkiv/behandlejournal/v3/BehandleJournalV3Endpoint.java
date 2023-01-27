package no.nav.dokarkiv.behandlejournal.v3;

import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.ArkiverUstrukturertKravSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.FerdigstillDokumentopplastingSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerInngaaendeHenvendelseSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerUtgaaendeHenvendelseSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.LagreVedleggPaaJournalpostSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.FerdigstillDokumentopplastingRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerInngaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerNotatResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostRequest;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.LagreVedleggPaaJournalpostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/behandleJournal/v3/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/behandleJournal/v3/Binding",
		serviceName = "BehandleJournal_v3",
		portName = "behandleJournal_v3Port")
@Addressing
@HandlerChain(file = "classpath:behandlejournalv3.xml")
@Service
public class BehandleJournalV3Endpoint implements BehandleJournalV3 {

	public static final String OPERASJONEN_ER_SANERT = "Denne operasjonen på BehandleJournal_v3 er permanent sanert. Ta kontakt med oss i #team_dokumentløsninger for avklaring.";
	@Resource
	private WebServiceContext webServiceContext;

	@Autowired
	private BehandleJournalV3 behandleJournalProvider;

	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request)
			throws ArkiverUstrukturertKravSikkerhetsbegrensning {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet, LagreVedleggPaaJournalpostSikkerhetsbegrensning {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest request)
			throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet, FerdigstillDokumentopplastingSikkerhetsbegrensning {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark063_v3"}, percentiles = {0.5, 0.95})
	@Override
	public JournalfoerNotatResponse journalfoerNotat(JournalfoerNotatRequest request) throws JournalfoerNotatSikkerhetsbegrensning {
		// Denne operasjonen er fremdeles i bruk av spberegning
		RequestContextUtil.createAndSetRequestContext(webServiceContext, request.getApplikasjonsID());
		return behandleJournalProvider.journalfoerNotat(request);
	}

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(JournalfoerUtgaaendeHenvendelseRequest request)
			throws JournalfoerUtgaaendeHenvendelseSikkerhetsbegrensning {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(JournalfoerInngaaendeHenvendelseRequest request)
			throws JournalfoerInngaaendeHenvendelseSikkerhetsbegrensning {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public void ping() {
		// noop
	}
}