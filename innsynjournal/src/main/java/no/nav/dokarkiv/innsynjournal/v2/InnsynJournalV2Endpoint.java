package no.nav.dokarkiv.innsynjournal.v2;

import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.HentTilgjengeligJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostJournalpostIkkeInngaaende;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostObjektIkkeFunnet;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostUgyldigAntallJournalposter;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.IdentifiserJournalpostUgyldingInput;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeResponse;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

/**
 * Implementation of JAX-WS-generated service interface InnsynJournalV2. Bootstraps the
 * Spring context and delegates to Spring-managed InnsynJournalV2Provider.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@WebService(endpointInterface = "no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.InnsynJournalV2",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/virksomhet/innsynJournal/v2/Binding.wsdl",
		targetNamespace = "http://nav.no/tjeneste/virksomhet/innsynJournal/v2/Binding",
		serviceName = "InnsynJournal_v2",
		portName = "InnsynJournal_v2Port")
@Addressing
@HandlerChain(file = "classpath:innsynjournalv2handler.xml")
@Service
public class InnsynJournalV2Endpoint implements InnsynJournalV2 {

	@Resource
	private WebServiceContext webServiceContext;

	@Inject
	private InnsynJournalV2 innsynJournalV2Provider;

	@Override
	public void ping() {
		innsynJournalV2Provider.ping();
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark053"}, percentiles = {0.5, 0.95})
	@Override
	public HentTilgjengeligJournalpostListeResponse hentTilgjengeligJournalpostListe(HentTilgjengeligJournalpostListeRequest request)
			throws HentTilgjengeligJournalpostListeSikkerhetsbegrensning {
		// ApplikasjonsID is not used since this is read operation, so we set it to the operation name
		RequestContextUtil.createAndSetRequestContext(webServiceContext, "InnsynJournalV2.hentTilgjengeligJournalpostListe");
		return innsynJournalV2Provider.hentTilgjengeligJournalpostListe(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark054"}, percentiles = {0.5, 0.95})
	@Override
	public HentDokumentResponse hentDokument(HentDokumentRequest hentDokumentRequest) throws HentDokumentDokumentIkkeFunnet,
			HentDokumentSikkerhetsbegrensning {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, "InnsynJournalV2.hentDokument");
		return innsynJournalV2Provider.hentDokument(hentDokumentRequest);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark059"}, percentiles = {0.5, 0.95})
	@Override
	public IdentifiserJournalpostResponse identifiserJournalpost(IdentifiserJournalpostRequest request)
			throws IdentifiserJournalpostUgyldingInput, IdentifiserJournalpostObjektIkkeFunnet, IdentifiserJournalpostUgyldigAntallJournalposter, IdentifiserJournalpostJournalpostIkkeInngaaende {
		// ApplikasjonsID is not used since this is read operation, so we set it to the operation name
		RequestContextUtil.createAndSetRequestContext(webServiceContext, "InnsynJournalV2.identifiserJournalpost");
		return innsynJournalV2Provider.identifiserJournalpost(request);
	}
}
