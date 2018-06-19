package no.nav.dokarkiv.arkiverdokumentproduksjon;


import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.AVBRUTT;
import static no.nav.dokarkiv.core.domain.codes.DokumentStatusCode.UNDER_REDIGERING;

import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoInnskrenketPartsinnsynException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoIsOrganInterntException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.DokumentInfoSlettetException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FeilregistrertSaksrelasjonException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.FilDetaljerOnDemandException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDocumentUpdateException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalFagomraadeException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalTilleggsopplysningerException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostIkkeFerdigstiltException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigDokumentStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusOvergangException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigJournalStatusVerdiException;
import no.nav.dokarkiv.arkiverdokumentproduksjon.exceptions.UgyldigTilknyttetJournalpostSomVerdiException;
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
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104.SettDatoSendtService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggResponseTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark105.ArkiverVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106.AvbrytVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark106.AvbrytVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107.FjernFerdigstiltDokumentRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark107.FjernFerdigstiltDokumentService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark108.FerdigstillJournalpostService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109.KnyttDokumentTilJournalpostSomVedleggRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark109.KnyttDokumentTilJournalpostSomVedleggService;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110.SettJournalpostAttributterRequestMapper;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110.SettJournalpostAttributterRequestTo;
import no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark110.SettJournalpostAttributterService;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
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
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.AlleredeFerdigstiltFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.AvbrytelseIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.DokumentAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.DokumentAlleredeRedigerbart;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.DokumentIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.DokumentIkkeVedlegg;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.DokumentTillatesIkkeGjenbrukt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.EksterneVedleggIkkeTillatt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.FeilStrukturFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.InneholderDokumenterUnderRedigering;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostAlleredeAvbrutt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostIkkeFerdigstilt;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.JournalpostIkkeUnderArbeid;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.KanIkkeFerdigstillesFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.ObjektIkkeFunnetFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.UgyldigInputFault;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.feil.UlikeFagomraader;
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
import org.dozer.Mapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.inject.Inject;

/**
 * Provider delegate for the ArkiverDokumentproduksjon webservice
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class ArkiverDokumentproduksjonProvider implements ArkiverDokumentproduksjonV1 {

	private static final String ARKIVER_DOKUMENTPRODUKSJON_V1 = "ArkiverDokumentproduksjonV1";
	private static final String OPPRETT_JOURNALPOST_ARKIVER_DOKUMENT = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".opprettJournalpostArkiverDokument";
	private static final String OPPDATER_JOURNALPOST_ARKIVER_DOKUMENT = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".oppdaterJournalpostArkiverDokument";
	private static final String OPPRETT_JOURNALPOST = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".opprettJournalpost";
	private static final String AVBRYT_JOURNALPOST = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".avbrytJournalpost";
	private static final String ARKIVER_VEDLEGG = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".arkiverVedlegg";
	private static final String AVBRYT_VEDLEGG = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".avbrytVedlegg";
	private static final String FERDIGSTILL_JOURNALPOST = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".ferdigstillJournalpost";
	private static final String FJERN_FERDIGSTILT_DOKUMENT = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".fjernFerdigstiltDokument";
	private static final String SETT_DATO_SENDT = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".settDatoSendt";
	private static final String KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG = ARKIVER_DOKUMENTPRODUKSJON_V1 + ".knyttDokumentTilJournalpostSomVedlegg";

	@Inject
	private OpprettJournalpostArkiverDokumentRequestMapper opprettJournalpostArkiverDokumentRequestMapper;

	@Inject
	private OpprettJournalpostArkiverDokumentResponseMapper opprettJournalpostArkiverDokumentResponseMapper;

	@Inject
	private OpprettJournalpostRequestMapper opprettJournalpostRequestMapper;

	@Inject
	private OpprettJournalpostArkiverDokumentService opprettJournalpostArkiverDokumentService;

	@Inject
	private OpprettJournalpostService opprettJournalpostService;

	@Inject
	private OppdaterJournalpostArkiverDokumentRequestMapper oppdaterJournalpostArkiverDokumentRequestMapper;

	@Inject
	private OppdaterJournalpostArkiverDokumentService oppdaterJournalpostArkiverDokumentService;

	@Inject
	private SettJournalpostAttributterRequestMapper settJournalpostAttributterRequestMapper;

	@Inject
	private SettJournalpostAttributterService settJournalpostAttributterService;

	@Inject
	private AvbrytJournalpostService avbrytJournalpostService;

	@Inject
	private SettDatoSendtRequestMapper settDatoSendtRequestMapper;

	@Inject
	private SettDatoSendtService settDatoSendtService;

	@Inject
	private ArkiverVedleggRequestMapper arkiverVedleggRequestMapper;

	@Inject
	private ArkiverVedleggResponseMapper arkiverVedleggResponseMapper;

	@Inject
	private ArkiverVedleggService arkiverVedleggService;

	@Inject
	private ArkiverDokumentproduksjonFaultInfoPopulator faultInfoPopulator;

	@Inject
	private FjernFerdigstiltDokumentService fjernFerdigstiltDokumentService;

	@Inject
	private FerdigstillJournalpostService ferdigstillJournalpostService;

	@Inject
	private FerdigstillJournalpostRequestMapper ferdigstillJournalpostRequestMapper;

	@Inject
	private AvbrytVedleggService avbrytVedleggService;

	@Inject
	private KnyttDokumentTilJournalpostSomVedleggService knyttDokumentTilJournalpostSomVedleggService;

	@Inject
	private Mapper dozerMapper;

	@Override
	@Transactional
	public OpprettJournalpostArkiverDokumentResponse opprettJournalpostArkiverDokument(
			OpprettJournalpostArkiverDokumentRequest request) {
		OpprettJournalpostArkiverDokumentRequestTo domeneRequest
				= opprettJournalpostArkiverDokumentRequestMapper.map(request);
		OpprettJournalpostArkiverDokumentResponseTo domeneResponse
				= opprettJournalpostArkiverDokumentService.opprettJournalpostArkiverDokument(domeneRequest);
		return opprettJournalpostArkiverDokumentResponseMapper.map(domeneResponse);
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
	public void settJournalpostAttributter(SettJournalpostAttributterRequest settJournalpostAttributterRequest) {
		SettJournalpostAttributterRequestTo domainRequest = settJournalpostAttributterRequestMapper.map(settJournalpostAttributterRequest);
		settJournalpostAttributterService.settJournalpostAttributter(domainRequest);
	}

	@Override
	@Transactional
	public OpprettJournalpostResponse opprettJournalpost(
			OpprettJournalpostRequest wsRequest) {
		OpprettJournalpostRequestTo domeneRequest = opprettJournalpostRequestMapper.map(wsRequest);
		OpprettJournalpostResponseTo opprettJournalpost = opprettJournalpostService.opprettJournalpost(domeneRequest);

		OpprettJournalpostResponse opprettJournalpostWsResponse = new OpprettJournalpostResponse();
		opprettJournalpostWsResponse.setDokumentInfoId(opprettJournalpost.getDokumentInfoId());
		opprettJournalpostWsResponse.setJournalpostId(opprettJournalpost.getJournalpostId());
		return opprettJournalpostWsResponse;
	}

	@Override
	@Transactional
	public void avbrytJournalpost(AvbrytJournalpostRequest wsRequest) throws AvbrytJournalpostJournalpostIkkeFunnet,
			AvbrytJournalpostAvbrytelseIkkeTillatt, AvbrytJournalpostJournalpostAlleredeAvbrutt {
		String operationName = "avbrytJournalpost";
		AvbrytJournalpostRequestTo domainRequest = null;
		if (wsRequest != null) {
			domainRequest = new AvbrytJournalpostRequestTo(wsRequest.getJournalpostId(), wsRequest.getEndretAvNavn());
		}
		try {
			avbrytJournalpostService.avbrytJournalpost(domainRequest);
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

	@Override
	@Transactional
	public ArkiverVedleggResponse arkiverVedlegg(ArkiverVedleggRequest arkiverVedleggRequest)
			throws ArkiverVedleggJournalpostIkkeFunnet, ArkiverVedleggJournalpostIkkeUnderArbeid {

		ArkiverVedleggRequestTo arkiverVedleggRequestTo = arkiverVedleggRequestMapper.map(arkiverVedleggRequest);

		ArkiverVedleggResponseTo response;
		try {
			response = arkiverVedleggService.arkiverVedlegg(arkiverVedleggRequestTo);
		} catch (NoJournalpostFoundException e) {
			throw new ArkiverVedleggJournalpostIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeFunnet(), e, ARKIVER_VEDLEGG));
		} catch (IllegalDocumentUpdateException e) {
			throw new ArkiverVedleggJournalpostIkkeUnderArbeid(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeUnderArbeid(), e, ARKIVER_VEDLEGG));
		}

		return arkiverVedleggResponseMapper.map(response);
	}

	@Override
	@Transactional
	public void avbrytVedlegg(AvbrytVedleggRequest wsRequest) throws AvbrytVedleggDokumentIkkeFunnet,
			AvbrytVedleggDokumentIkkeVedlegg, AvbrytVedleggJournalpostIkkeUnderArbeid, AvbrytVedleggDokumentAlleredeAvbrutt,
			AvbrytVedleggJournalpostIkkeFunnet {
		Assert.notNull(wsRequest, "Request is null");
		try {
			avbrytVedleggService.avbrytVedlegg(new AvbrytVedleggRequestTo(wsRequest.getJournalpostId(),
					wsRequest.getDokumentInfoId(),
					wsRequest.getEndretAvNavn()));
		} catch (NoJournalpostFoundException e) {
			throw new AvbrytVedleggJournalpostIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeFunnet(), e, AVBRYT_VEDLEGG));
		} catch (NoDokumentInfoFoundException e) {
			throw new AvbrytVedleggDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, AVBRYT_VEDLEGG));
		} catch (UgyldigJournalStatusVerdiException e) {
			throw new AvbrytVedleggJournalpostIkkeUnderArbeid(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeUnderArbeid(), e, AVBRYT_VEDLEGG));
		} catch (UgyldigDokumentStatusVerdiException e) {
			throw new AvbrytVedleggDokumentAlleredeAvbrutt(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentAlleredeAvbrutt(), e, AVBRYT_VEDLEGG));
		} catch (UgyldigTilknyttetJournalpostSomVerdiException e) {
			throw new AvbrytVedleggDokumentIkkeVedlegg(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeVedlegg(), e, AVBRYT_VEDLEGG));
		}
	}

	@Override
	@Transactional
	public void ferdigstillJournalpost(FerdigstillJournalpostRequest wsRequest)
			throws FerdigstillJournalpostJournalpostIkkeUnderArbeid, FerdigstillJournalpostInneholderDokumenterUnderRedigering,
			FerdigstillJournalpostJournalpostIkkeFunnet {
		try {
			FerdigstillJournalpostRequestTo domainRequest = ferdigstillJournalpostRequestMapper.map(wsRequest);
			ferdigstillJournalpostService.ferdigstillJournalpost(domainRequest);
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

	@Override
	@Transactional
	public void fjernFerdigstiltDokument(FjernFerdigstiltDokumentRequest wsRequest)
			throws FjernFerdigstiltDokumentDokumentIkkeFunnet, FjernFerdigstiltDokumentDokumentAlleredeAvbrutt,
			FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid, FjernFerdigstiltDokumentJournalpostIkkeFunnet,
			FjernFerdigstiltDokumentDokumentAlleredeRedigerbart {
		Assert.notNull(wsRequest, "Request is null");
		FjernFerdigstiltDokumentRequestTo domainRequest = new FjernFerdigstiltDokumentRequestTo(wsRequest.getJournalpostId(),
				wsRequest.getDokumentInfoId(), wsRequest.getEndretAvNavn());
		try {
			fjernFerdigstiltDokumentService.fjernFerdigstiltDokument(domainRequest);
		} catch (NoJournalpostFoundException e) {
			throw new FjernFerdigstiltDokumentJournalpostIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeFunnet(), e, FJERN_FERDIGSTILT_DOKUMENT));
		} catch (NoDokumentInfoFoundException e) {
			throw new FjernFerdigstiltDokumentDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, FJERN_FERDIGSTILT_DOKUMENT));
		} catch (UgyldigJournalStatusVerdiException e) {
			throw new FjernFerdigstiltDokumentJournalpostIkkeUnderArbeid(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new JournalpostIkkeUnderArbeid(), e, FJERN_FERDIGSTILT_DOKUMENT));
		} catch (UgyldigDokumentStatusVerdiException e) {
			if (UNDER_REDIGERING.equals(e.getDokumentStatus())) {
				throw new FjernFerdigstiltDokumentDokumentAlleredeRedigerbart(e.getMessage(),
						faultInfoPopulator.populateFaultInfo(new DokumentAlleredeRedigerbart(), e,
								FJERN_FERDIGSTILT_DOKUMENT));
			} else if (AVBRUTT.equals(e.getDokumentStatus())) {
				throw new FjernFerdigstiltDokumentDokumentAlleredeAvbrutt(e.getMessage(), faultInfoPopulator.populateFaultInfo(
						new DokumentAlleredeAvbrutt(), e, FJERN_FERDIGSTILT_DOKUMENT));
			} else {
				throw new IllegalStateException(e);
			}
		}
	}

	@Override
	@Transactional
	public void settDatoSendt(SettDatoSendtRequest settDatoSendtRequest) {
		SettDatoSendtRequestTo domainRequest = settDatoSendtRequestMapper.map(settDatoSendtRequest);
		settDatoSendtService.settDatoSendt(domainRequest);
	}

	@Override
	@Transactional
	public void knyttDokumentTilJournalpostSomVedlegg(KnyttDokumentTilJournalpostSomVedleggRequest request) throws
			KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet,
			KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet,
			KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt,
			KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid,
			KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt,
			KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt,
			KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader {

		KnyttDokumentTilJournalpostSomVedleggRequestTo domainRequest = null;

		if (request != null) {
			domainRequest = dozerMapper.map(request, KnyttDokumentTilJournalpostSomVedleggRequestTo.class);
		}

		try {
			knyttDokumentTilJournalpostSomVedleggService.knyttDokumentTilJournalpostSomVedlegg(domainRequest);
		} catch (DokumentInfoInnskrenketPartsinnsynException
				| DokumentInfoSlettetException
				| DokumentInfoIsOrganInterntException
				| IllegalDokumentstatusException
				| FilDetaljerOnDemandException
				| IllegalVariantFormatException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggDokumentTillatesIkkeGjenbrukt(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new DokumentTillatesIkkeGjenbrukt(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (JournalpostNotFoundException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFunnet(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (DokumentInfoNotFoundException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggDokumentIkkeFunnet(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new DokumentIkkeFunnet(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (IllegalFagomraadeException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggUlikeFagomraader(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new UlikeFagomraader(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (JournalpostIkkeFerdigstiltException | FeilregistrertSaksrelasjonException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeFerdigstilt(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeFerdigstilt(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (IllegalJournalStatusException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggJournalpostIkkeUnderArbeid(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new JournalpostIkkeUnderArbeid(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		} catch (IllegalTilleggsopplysningerException exception) {
			throw new KnyttDokumentTilJournalpostSomVedleggEksterneVedleggIkkeTillatt(exception.getMessage(),
					faultInfoPopulator.populateFaultInfo(new EksterneVedleggIkkeTillatt(), exception,
							KNYTT_DOKUMENT_TIL_JOURNALPOST_SOM_VEDLEGG));
		}
	}

	@Override
	public void ping() {
		// noop
	}
}