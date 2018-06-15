package no.nav.dokarkiv.arkiverdokumentproduksjon;

import no.nav.modig.common.MDCOperations;
import no.nav.provider.dok.joark.nsb.config.ArkiverDokumentproduksjonConfig;
import no.nav.provider.dok.joark.support.RequestContextUtil;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AlleredeFerdigstiltException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostAvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggDokumentIkkeVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FeilStrukturException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostInneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentAlleredeRedigerbart;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KanIkkeFerdigstillesException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ObjektIkkeFunnetException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.UgyldigInputException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FerdigstillJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.FjernFerdigstiltDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.KnyttDokumentTilJournalpostSomVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OppdaterJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumentResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.soap.Addressing;

/**
 * Implementation of JAX-WS-generated service interface ArkiverDokumentproduksjonV1. Bootstraps the
 * Spring context and delegates to Spring-managed ArkiverDokumentProduksjonProvider.
 *
 * @author Joakim Bj?rnstad, Visma Consulting
 */
@WebService(endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1",
        wsdlLocation = "WEB-INF/wsdl/no/nav/tjeneste/domene/brevogarkiv/arkiverdokumentproduksjon/v1/arkiverdokumentproduksjon.wsdl",
        targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/arkiverdokumentproduksjon/v1/",
        serviceName = "ArkiverDokumentproduksjon_v1",
        portName = "ArkiverDokumentproduksjonPort_v1")
@Addressing
@HandlerChain(file = "handler.xml")
public class ArkiverDokumentproduksjonEndpoint implements ArkiverDokumentproduksjonV1 {

    // hardkodet, siden appid ikke er tilgjenglig i api
    private static final String DOKPROS_APPID = "dokumentproduksjon";

    private ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonProvider;

    @Resource
    private WebServiceContext webServiceContext;

    @Override
    public OpprettJournalpostArkiverDokumentResponse opprettJournalpostArkiverDokument
            (OpprettJournalpostArkiverDokumentRequest request) {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        return arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
    }

    @Override
    public void settDatoSendt(SettDatoSendtRequest settDatoSendtRequest) {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.settDatoSendt(settDatoSendtRequest);
    }

    @Override
    public void oppdaterJournalpostArkiverDokument(OppdaterJournalpostArkiverDokumentRequest request) throws KanIkkeFerdigstillesException, FeilStrukturException, ObjektIkkeFunnetException, AlleredeFerdigstiltException, UgyldigInputException {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
    }

    @Override
    public void settJournalpostAttributter(SettJournalpostAttributterRequest settJournalpostAttributterRequest) {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.settJournalpostAttributter(settJournalpostAttributterRequest);
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        return arkiverDokumentproduksjonProvider.opprettJournalpost(request);
    }

    @Override
    public void ping() {
        arkiverDokumentproduksjonProvider.ping();
    }

    @Override
    public void avbrytJournalpost(AvbrytJournalpostRequest request)
            throws AvbrytJournalpostJournalpostIkkeFunnet, AvbrytJournalpostAvbrytelseIkkeTillatt,
            AvbrytJournalpostJournalpostAlleredeAvbrutt {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
    }

    @Override
    public ArkiverVedleggResponse arkiverVedlegg(ArkiverVedleggRequest arkiverVedleggRequest)
            throws ArkiverVedleggJournalpostIkkeFunnet, ArkiverVedleggJournalpostIkkeUnderArbeid {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        return arkiverDokumentproduksjonProvider.arkiverVedlegg(arkiverVedleggRequest);
    }

    @Override
    public void avbrytVedlegg(AvbrytVedleggRequest request) throws AvbrytVedleggDokumentIkkeFunnet,
            AvbrytVedleggDokumentIkkeVedlegg, AvbrytVedleggJournalpostIkkeUnderArbeid, AvbrytVedleggDokumentAlleredeAvbrutt,
            AvbrytVedleggJournalpostIkkeFunnet {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.avbrytVedlegg(request);

    }

    @Override
    public void ferdigstillJournalpost(FerdigstillJournalpostRequest request)
            throws FerdigstillJournalpostJournalpostIkkeUnderArbeid, FerdigstillJournalpostInneholderDokumenterUnderRedigering,
            FerdigstillJournalpostJournalpostIkkeFunnet {
        String userId = MDCOperations.getFromMDC(MDCOperations.MDC_USER_ID);
        RequestContextUtil.createAndSetUsername(userId, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.ferdigstillJournalpost(request);
    }

    @Override
    public void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequest request)
            throws FjernFerdigstiltDokumentDokumentIkkeFunnet, FjernFerdigstiltDokumentDokumentAlleredeAvbrutt,
            FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid, FjernFerdigstiltDokumentJournalpostIkkeFunnet,
            FjernFerdigstiltDokumentDokumentAlleredeRedigerbart {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.fjernFerdigstiltDokument(request);
    }

    @Override
    public void knyttDokumentTilJournalpostSomVedlegg(KnyttDokumentTilJournalpostSomVedleggRequest request) throws
            KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt,
            KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt,
            KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt,
            KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet,
            KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader,
            KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid,
            KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet {
        RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
        arkiverDokumentproduksjonProvider.knyttDokumentTilJournalpostSomVedlegg(request);
    }

    /**
     * Retrieve the arkiverDokumentproduksjonProvider bean from the Spring context.
     */
    @PostConstruct
    public void initArkiverDokumentproduksjonProvider() {
        ServletContext servletContext = (ServletContext) webServiceContext.getMessageContext().get(
                MessageContext.SERVLET_CONTEXT);
        WebApplicationContext webApplicationContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(servletContext);
        arkiverDokumentproduksjonProvider = (ArkiverDokumentproduksjonV1)
                webApplicationContext.getBean(ArkiverDokumentproduksjonConfig.PROVIDER_BEAN);
    }

}
