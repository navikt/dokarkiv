package no.nav.dokarkiv.arkiverdokumentproduksjon;


import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark100.OpprettJournalpostArkiverDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark101.OpprettJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark102.OppdaterJournalpostArkiverDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103.AvbrytJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark103.AvbrytJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark112.OpprettJournalpostArkiverDokumenterService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AlleredeFerdigstiltException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ArkiverDokumentproduksjonV1;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostAvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.AvbrytJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FeilStrukturException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostInneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.FerdigstillJournalpostJournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.KanIkkeFerdigstillesException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.ObjektIkkeFunnetException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.UgyldigInputException;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.AlleredeFerdigstiltFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.AvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.FeilStrukturFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.InneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.KanIkkeFerdigstillesFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.ObjektIkkeFunnetFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.UgyldigInputFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.AvbrytJournalpostRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggRequest;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.ArkiverVedleggResponse;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Slf4j
@Component
public class ArkiverDokumentproduksjonProvider implements ArkiverDokumentproduksjonV1 {

	private static final String ARKIVER_DOKUMENTPRODUKSJON_V1 = "ArkiverDokumentproduksjonV1";
	private static final String FERDIGSTILL_JOURNALPOST = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".ferdigstillJournalpost";
	private static final String REQUEST_IS_NULL_MSG = "Request is null";

	private final OpprettJournalpostArkiverDokumentRequestMapper opprettJournalpostArkiverDokumentRequestMapper;
	private final OpprettJournalpostArkiverDokumentResponseMapper opprettJournalpostArkiverDokumentResponseMapper;
	private final OpprettJournalpostArkiverDokumenterResponseMapper opprettJournalpostArkiverDokumenterResponseMapper;
	private final OpprettJournalpostRequestMapper opprettJournalpostRequestMapper;
	private final OpprettJournalpostArkiverDokumentService opprettJournalpostArkiverDokumentService;
	private final OpprettJournalpostArkiverDokumenterService opprettJournalpostArkiverDokumenterService;
	private final OpprettJournalpostService opprettJournalpostService;
	private final OppdaterJournalpostArkiverDokumentRequestMapper oppdaterJournalpostArkiverDokumentRequestMapper;
	private final OppdaterJournalpostArkiverDokumentService oppdaterJournalpostArkiverDokumentService;
	private final AvbrytJournalpostService avbrytJournalpostService;
	private final ArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulator;
	private final FerdigstillJournalpostService ferdigstillJournalpostService;
	private final FerdigstillJournalpostRequestMapper ferdigstillJournalpostRequestMapper;

	public ArkiverDokumentproduksjonProvider(OpprettJournalpostArkiverDokumentRequestMapper opprettJournalpostArkiverDokumentRequestMapper,
											 OpprettJournalpostArkiverDokumentResponseMapper opprettJournalpostArkiverDokumentResponseMapper,
											 OpprettJournalpostArkiverDokumenterResponseMapper opprettJournalpostArkiverDokumenterResponseMapper,
											 OpprettJournalpostRequestMapper opprettJournalpostRequestMapper,
											 OpprettJournalpostArkiverDokumentService opprettJournalpostArkiverDokumentService,
											 OpprettJournalpostArkiverDokumenterService opprettJournalpostArkiverDokumenterService,
											 OpprettJournalpostService opprettJournalpostService,
											 OppdaterJournalpostArkiverDokumentRequestMapper oppdaterJournalpostArkiverDokumentRequestMapper,
											 OppdaterJournalpostArkiverDokumentService oppdaterJournalpostArkiverDokumentService,
											 AvbrytJournalpostService avbrytJournalpostService,
											 ArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulator,
											 FerdigstillJournalpostService ferdigstillJournalpostService,
											 FerdigstillJournalpostRequestMapper ferdigstillJournalpostRequestMapper) {
		this.opprettJournalpostArkiverDokumentRequestMapper = opprettJournalpostArkiverDokumentRequestMapper;
		this.opprettJournalpostArkiverDokumentResponseMapper = opprettJournalpostArkiverDokumentResponseMapper;
		this.opprettJournalpostArkiverDokumenterResponseMapper = opprettJournalpostArkiverDokumenterResponseMapper;
		this.opprettJournalpostRequestMapper = opprettJournalpostRequestMapper;
		this.opprettJournalpostArkiverDokumentService = opprettJournalpostArkiverDokumentService;
		this.opprettJournalpostArkiverDokumenterService = opprettJournalpostArkiverDokumenterService;
		this.opprettJournalpostService = opprettJournalpostService;
		this.oppdaterJournalpostArkiverDokumentRequestMapper = oppdaterJournalpostArkiverDokumentRequestMapper;
		this.oppdaterJournalpostArkiverDokumentService = oppdaterJournalpostArkiverDokumentService;
		this.avbrytJournalpostService = avbrytJournalpostService;
		this.faultInfoPopulator = faultInfoPopulator;
		this.ferdigstillJournalpostService = ferdigstillJournalpostService;
		this.ferdigstillJournalpostRequestMapper = ferdigstillJournalpostRequestMapper;
	}

	@Override
	@Transactional
	public OpprettJournalpostArkiverDokumentResponse opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumentRequest request) {
		Assert.notNull(request, REQUEST_IS_NULL_MSG);
		OpprettJournalpostArkiverDokumentRequestTo domeneRequest
				= opprettJournalpostArkiverDokumentRequestMapper.map(request);
		OpprettJournalpostArkiverDokumentResponseTo domeneResponse
				= opprettJournalpostArkiverDokumentService.opprettJournalpostArkiverDokument(domeneRequest);
		log.info("tjoark100 har opprettet journalpost med journalpostId={} og dokumentInfoId={}", domeneResponse.getJournalpostId(), domeneResponse
				.getDokumentInfoId());
		return opprettJournalpostArkiverDokumentResponseMapper.map(domeneResponse);
	}

	@Override
	@Transactional
	public OpprettJournalpostResponse opprettJournalpost(
			OpprettJournalpostRequest wsRequest) {
		Assert.notNull(wsRequest, REQUEST_IS_NULL_MSG);
		OpprettJournalpostRequestTo domeneRequest = opprettJournalpostRequestMapper.map(wsRequest);
		OpprettJournalpostResponseTo opprettJournalpost = opprettJournalpostService.opprettJournalpost(domeneRequest);

		log.info("tjoark101 har opprettet journalpost med journalpostId={} og dokumentInfoId={}", opprettJournalpost.getJournalpostId(), opprettJournalpost
				.getDokumentInfoId());
		OpprettJournalpostResponse opprettJournalpostWsResponse = new OpprettJournalpostResponse();
		opprettJournalpostWsResponse.setDokumentInfoId(opprettJournalpost.getDokumentInfoId());
		opprettJournalpostWsResponse.setJournalpostId(opprettJournalpost.getJournalpostId());
		return opprettJournalpostWsResponse;
	}

	@Override
	@Transactional
	public void oppdaterJournalpostArkiverDokument(
			OppdaterJournalpostArkiverDokumentRequest wsRequest) throws UgyldigInputException, ObjektIkkeFunnetException, KanIkkeFerdigstillesException
			, FeilStrukturException, AlleredeFerdigstiltException {
		if (wsRequest == null) {
			throw new UgyldigInputException("Request is empty", new UgyldigInputFault());
		}
		if ((Long.valueOf(wsRequest.getJournalpostId()) == null) || (Long.valueOf(wsRequest.getJournalpostId()) == 0)) {
			throw new UgyldigInputException("JournalpostId er tom", new UgyldigInputFault());
		}
		try {
			OppdaterJournalpostArkiverDokumentRequestTo domeneRequest
					= oppdaterJournalpostArkiverDokumentRequestMapper.map(wsRequest);
			oppdaterJournalpostArkiverDokumentService.oppdaterJournalpostArkiverDokument(domeneRequest);
			log.info("tjoark102 har oppdatert journalpost med journalpostId={} og dokumentInfoId={}", domeneRequest.getJournalpostId(), domeneRequest
					.getDokumentInfoId());
		} catch (no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigInputException e) {
			throw new UgyldigInputException(e.getMessage(), new UgyldigInputFault());
		} catch (no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.ObjektIkkeFunnetException e) {
			throw new ObjektIkkeFunnetException(e.getMessage(), new ObjektIkkeFunnetFault());
		} catch (no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.KanIkkeFerdigstillesException e) {
			throw new KanIkkeFerdigstillesException(e.getMessage(), new KanIkkeFerdigstillesFault());
		} catch (no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilStrukturException e) {
			throw new FeilStrukturException(e.getMessage(), new FeilStrukturFault());
		} catch (no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.AlleredeFerdigstiltException e) {
			throw new AlleredeFerdigstiltException(e.getMessage(), new AlleredeFerdigstiltFault());
		}
	}

	@Override
	@Transactional
	public void avbrytJournalpost(AvbrytJournalpostRequest wsRequest) throws AvbrytJournalpostJournalpostIkkeFunnet,
			AvbrytJournalpostAvbrytelseIkkeTillatt, AvbrytJournalpostJournalpostAlleredeAvbrutt {
		Assert.notNull(wsRequest, REQUEST_IS_NULL_MSG);
		String operationName = "avbrytJournalpost";
		AvbrytJournalpostRequestTo domainRequest = new AvbrytJournalpostRequestTo(wsRequest.getJournalpostId(), wsRequest.getEndretAvNavn());
		try {
			avbrytJournalpostService.avbrytJournalpost(domainRequest);
			log.info("tjoark103 har avbrutt journalpost med journalpostId={}", domainRequest.getJournalpostId());
		} catch (NoJournalpostFoundException e) {
			throw new AvbrytJournalpostJournalpostIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeFunnet(), e, operationName));
		} catch (UgyldigJournalStatusOvergangException e) {
			if (e.getExistingJournalStatus() == JournalStatusCode.A) {
				throw new AvbrytJournalpostJournalpostAlleredeAvbrutt(e.getMessage(), faultInfoPopulator.populateFaultInfo(
						new JournalpostAlleredeAvbrutt(), e, operationName));
			} else {
				throw new AvbrytJournalpostAvbrytelseIkkeTillatt(e.getMessage(), faultInfoPopulator.populateFaultInfo(
						new AvbrytelseIkkeTillatt(), e, operationName));
			}
		}
	}

	@Deprecated
	@Override
	public void settDatoSendt(SettDatoSendtRequest settDatoSendtRequest) {
		throw new UnsupportedOperationException("settDatoSendt er sanert 2023-02");
	}

	@Deprecated
	@Override
	public ArkiverVedleggResponse arkiverVedlegg(ArkiverVedleggRequest arkiverVedleggRequest) {
		throw new UnsupportedOperationException("arkiverVedlegg er avviklet 2026-10");
	}

	@Deprecated
	@Override
	public void avbrytVedlegg(AvbrytVedleggRequest wsRequest) {
		throw new UnsupportedOperationException("avbrytVedlegg er sanert 2023-02");
	}

	@Deprecated
	@Override
	public void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequest wsRequest) {
		throw new UnsupportedOperationException("fjernFerdigstiltDokument er sanert 2023-02");
	}

	@Override
	@Transactional
	public void ferdigstillJournalpost(FerdigstillJournalpostRequest wsRequest)
			throws FerdigstillJournalpostJournalpostIkkeUnderArbeid, FerdigstillJournalpostInneholderDokumenterUnderRedigering,
			FerdigstillJournalpostJournalpostIkkeFunnet {
		Assert.notNull(wsRequest, REQUEST_IS_NULL_MSG);
		try {
			FerdigstillJournalpostRequestTo domainRequest = ferdigstillJournalpostRequestMapper.map(wsRequest);
			ferdigstillJournalpostService.ferdigstillJournalpost(domainRequest);
			log.info("tjoark108 har ferdigstilt journalpost med journalpostId={}", domainRequest.getJournalpostId());
		} catch (NoJournalpostFoundException e) {
			throw new FerdigstillJournalpostJournalpostIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeFunnet(), e, FERDIGSTILL_JOURNALPOST));
		} catch (UgyldigJournalStatusVerdiException e) {
			throw new FerdigstillJournalpostJournalpostIkkeUnderArbeid(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeUnderArbeid(), e, FERDIGSTILL_JOURNALPOST));
		} catch (UgyldigDokumentStatusVerdiException e) {
			throw new FerdigstillJournalpostInneholderDokumenterUnderRedigering(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new InneholderDokumenterUnderRedigering(), e,
							FERDIGSTILL_JOURNALPOST));
		}
	}

	@Deprecated
	@Override
	public void knyttDokumentTilJournalpostSomVedlegg(KnyttDokumentTilJournalpostSomVedleggRequest request) {
		throw new UnsupportedOperationException("knyttDokumentTilJournalpostSomVedlegg er sanert 2023-02");
	}

	@Deprecated
	@Override
	public void settJournalpostAttributter(SettJournalpostAttributterRequest settJournalpostAttributterRequest) {
		throw new UnsupportedOperationException("settJournalpostAttributter er avviklet 2026-10");
	}

	@Deprecated
	@Override
	public OpprettUtgaaendeJournalpostArkiverDokumentResponse opprettUtgaaendeJournalpostArkiverDokument(OpprettUtgaaendeJournalpostArkiverDokumentRequest request) {
		throw new UnsupportedOperationException("opprettUtgaaendeJournalpostArkiverDokument er avviklet 2026-10");
	}

	@Override
	@Transactional
	public OpprettJournalpostArkiverDokumenterResponse opprettJournalpostArkiverDokumenter(
			OpprettJournalpostArkiverDokumenterRequest request) {
		Assert.notNull(request, REQUEST_IS_NULL_MSG);
		OpprettJournalpostArkiverDokumenterResponseTo domeneResponse = opprettJournalpostArkiverDokumenterService.opprettJournalpostArkiverDokument(request);
		log.info("tjoark112 har opprettet journalpost med journalpostId={} og dokumentInfoIds={}", domeneResponse.getJournalpostId(), domeneResponse
				.getDokumentInfoIds());
		return opprettJournalpostArkiverDokumenterResponseMapper.map(domeneResponse);
	}

	@Override
	public void ping() {
		// noop
	}


}