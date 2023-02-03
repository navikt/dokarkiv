package no.nav.dokarkiv.arkiverdokumentproduksjon;

import com.google.common.base.Strings;
import io.micrometer.core.annotation.Timed;
import no.nav.dokarkiv.core.stelvio.RequestContextUtil;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.OpprettUtgaaendeJournalpostUgyldigInput;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.OpprettUtgaaendeJournalpostValideringAvVedleggFeilet;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostArkiverDokumenterResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettJournalpostResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.OpprettUtgaaendeJournalpostArkiverDokumentResponse;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettJournalpostAttributterRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;

import static no.nav.dokarkiv.core.MDCConstants.MDC_APP_ID;

/**
 * Implementation of JAX-WS-generated service interface ArkiverDokumentproduksjonV1. Bootstraps the
 * Spring context and delegates to Spring-managed ArkiverDokumentProduksjonProvider.
 */
@WebService(targetNamespace = "http://nav.no/tjeneste/domene/brevogarkiv/arkiverdokumentproduksjon/v1/",
		serviceName = "ArkiverDokumentproduksjonService_v1",
		portName = "ArkiverDokumentproduksjonPort_v1",
		wsdlLocation = "classpath:wsdl/no/nav/tjeneste/domene/brevogarkiv/arkiverdokumentproduksjon/v1/arkiverdokumentproduksjon.wsdl",
		endpointInterface = "no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1")
@Addressing
@HandlerChain(file = "classpath:arkiverdokumentproduksjon-handler.xml")
@Service
public class ArkiverDokumentproduksjonEndpoint implements ArkiverDokumentproduksjonV1 {

	// hardkodet, siden appid ikke er tilgjenglig i api
	private static final String DOKPROS_APPID = "dokumentproduksjon";

	@Autowired
	private ArkiverDokumentproduksjonV1 arkiverDokumentproduksjonProvider;

	@Resource
	private WebServiceContext webServiceContext;

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark100"}, percentiles = {0.5, 0.95})
	@Override
	public OpprettJournalpostArkiverDokumentResponse opprettJournalpostArkiverDokument
			(OpprettJournalpostArkiverDokumentRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		return arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokument(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark101"}, percentiles = {0.5, 0.95})
	@Override
	public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		return arkiverDokumentproduksjonProvider.opprettJournalpost(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark102"}, percentiles = {0.5, 0.95})
	@Override
	public void oppdaterJournalpostArkiverDokument(OppdaterJournalpostArkiverDokumentRequest request) throws KanIkkeFerdigstillesException, FeilStrukturException, ObjektIkkeFunnetException, AlleredeFerdigstiltException, UgyldigInputException {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		arkiverDokumentproduksjonProvider.oppdaterJournalpostArkiverDokument(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark103"}, percentiles = {0.5, 0.95})
	@Override
	public void avbrytJournalpost(AvbrytJournalpostRequest request)
			throws AvbrytJournalpostJournalpostIkkeFunnet, AvbrytJournalpostAvbrytelseIkkeTillatt,
			AvbrytJournalpostJournalpostAlleredeAvbrutt {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		arkiverDokumentproduksjonProvider.avbrytJournalpost(request);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark104"}, percentiles = {0.5, 0.95})

	@Override
	public void settDatoSendt(SettDatoSendtRequest settDatoSendtRequest) {
		throw new UnsupportedOperationException("settDatoSendt er sanert 2023-02");
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark105"}, percentiles = {0.5, 0.95})
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
		throw new UnsupportedOperationException("avbrytVedlegg er sanert 2023-02");
	}

	@Override
	public void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequest request)
			throws FjernFerdigstiltDokumentDokumentIkkeFunnet, FjernFerdigstiltDokumentDokumentAlleredeAvbrutt,
			FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid, FjernFerdigstiltDokumentJournalpostIkkeFunnet,
			FjernFerdigstiltDokumentDokumentAlleredeRedigerbart {
		throw new UnsupportedOperationException("fjernFerdigstiltDokument er sanert 2023-02");
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark108"}, percentiles = {0.5, 0.95})
	@Override
	public void ferdigstillJournalpost(FerdigstillJournalpostRequest request)
			throws FerdigstillJournalpostJournalpostIkkeUnderArbeid, FerdigstillJournalpostInneholderDokumenterUnderRedigering,
			FerdigstillJournalpostJournalpostIkkeFunnet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		arkiverDokumentproduksjonProvider.ferdigstillJournalpost(request);
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
		throw new UnsupportedOperationException("knyttDokumentTilJournalpostSomVedlegg er sanert 2023-02");
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark110"}, percentiles = {0.5, 0.95})
	@Override
	public void settJournalpostAttributter(SettJournalpostAttributterRequest settJournalpostAttributterRequest) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		arkiverDokumentproduksjonProvider.settJournalpostAttributter(settJournalpostAttributterRequest);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark111"}, percentiles = {0.5, 0.95})
	@Override
	public OpprettUtgaaendeJournalpostArkiverDokumentResponse opprettUtgaaendeJournalpostArkiverDokument(OpprettUtgaaendeJournalpostArkiverDokumentRequest opprettUtgaaendeJournalpostArkiverDokumentRequest) throws OpprettUtgaaendeJournalpostUgyldigInput, OpprettUtgaaendeJournalpostValideringAvVedleggFeilet {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, findAppId());
		return arkiverDokumentproduksjonProvider.opprettUtgaaendeJournalpostArkiverDokument(opprettUtgaaendeJournalpostArkiverDokumentRequest);
	}

	@Timed(value = "dok_request", extraTags = {"process_code", "tjoark112"}, percentiles = {0.5, 0.95})
	@Override
	public OpprettJournalpostArkiverDokumenterResponse opprettJournalpostArkiverDokumenter
			(OpprettJournalpostArkiverDokumenterRequest request) {
		RequestContextUtil.createAndSetRequestContext(webServiceContext, DOKPROS_APPID);
		return arkiverDokumentproduksjonProvider.opprettJournalpostArkiverDokumenter(request);
	}

	@Override
	public void ping() {
		arkiverDokumentproduksjonProvider.ping();
	}

	private String findAppId() {
		String appId = MDC.get(MDC_APP_ID);
		return Strings.isNullOrEmpty(appId) ? DOKPROS_APPID : appId;
	}

}
