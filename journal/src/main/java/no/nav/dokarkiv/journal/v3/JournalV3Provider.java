package no.nav.dokarkiv.journal.v3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.exceptions.DocumentNotFoundException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.core.security.abac.AbacSecurityService;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentURLV3RequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlResponseTo;
import no.nav.dokarkiv.journal.v3.tjoark050.HentDokumentUrlService;
import no.nav.dokarkiv.journal.v3.tjoark051.HentDokumentRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark051.HentDokumentV3RequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark051.Tjoark051HentDokumentService;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestMapper;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestTo;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeRequestValidator;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeResponseMapper;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeResponseTo;
import no.nav.dokarkiv.journal.v3.tjoark058.HentKjerneJournalpostListeService;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.freg.abac.core.annotation.context.AbacContext;
import no.nav.freg.abac.core.dto.request.XacmlRequest;
import no.nav.freg.abac.core.dto.response.Decision;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentJournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLDokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.HentDokumentURLSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.HentKjerneJournalpostListeUgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.JournalV3;
import no.nav.tjeneste.virksomhet.journal.v3.feil.DokumentIkkeFunnet;
import no.nav.tjeneste.virksomhet.journal.v3.feil.Sikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.journal.v3.feil.UgyldigInput;
import no.nav.tjeneste.virksomhet.journal.v3.informasjon.hentkjernejournalpostliste.ArkivSak;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentDokumentURLResponse;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeRequest;
import no.nav.tjeneste.virksomhet.journal.v3.meldinger.HentKjerneJournalpostListeResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_DOKUMENT;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_SAK;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.AbacSecurityService.ACCESS_DENIED;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.READ_ACTION;

/**
 * POJO JournalV3Provider that maps from and to WS model and delegates to
 * Service implementations.
 */
@Slf4j
@Component
public class JournalV3Provider implements JournalV3 {

	private static final String JOURNAL_V3 = "Journal_v3";
	private static final String JOURNAL_V3_HENT_KJERNE_JOURNALPOST_LISTE = JOURNAL_V3 + ".hentKjerneJournalpostListe";
	static final String JOURNAL_V3_HENT_DOKUMENT = JOURNAL_V3 + ".hentDokument";
	private static final String JOURNAL_V3_HENT_DOKUMENT_URL = JOURNAL_V3 + ".hentDokumentURL";
	
	private final JournalV3FaultInfoPopulator faultInfoPopulator;
	private final HentKjerneJournalpostListeRequestValidator hentKjerneJournalpostListeRequestValidator;
	private final HentKjerneJournalpostListeService hentKjerneJournalpostListeService;
	private final Tjoark051HentDokumentService tjoark051HentDokumentService;
	private final AbacSecurityService abacSecurityService;
	private final HentDokumentUrlService hentDokumentUrlService;
	private final AbacContext abacContext;
	private final HentKjerneJournalpostListeResponseMapper hentKjerneJournalpostListeResponseMapper;
	private final HentKjerneJournalpostListeRequestMapper hentKjerneJournalpostListeRequestMapper;
	private final HentDokumentV3RequestMapper hentDokumentRequestMapper;
	private final HentDokumentURLV3RequestMapper hentDokumentURLV3RequestMapper;

	public JournalV3Provider(JournalV3FaultInfoPopulator faultInfoPopulator, HentKjerneJournalpostListeRequestValidator hentKjerneJournalpostListeRequestValidator, HentKjerneJournalpostListeService hentKjerneJournalpostListeService, Tjoark051HentDokumentService tjoark051HentDokumentService, AbacSecurityService abacSecurityService, HentDokumentUrlService hentDokumentUrlService, AbacContext abacContext, HentKjerneJournalpostListeResponseMapper hentKjerneJournalpostListeResponseMapper, HentKjerneJournalpostListeRequestMapper hentKjerneJournalpostListeRequestMapper, HentDokumentV3RequestMapper hentDokumentRequestMapper, HentDokumentURLV3RequestMapper hentDokumentURLV3RequestMapper) {
		this.faultInfoPopulator = faultInfoPopulator;
		this.hentKjerneJournalpostListeRequestValidator = hentKjerneJournalpostListeRequestValidator;
		this.hentKjerneJournalpostListeService = hentKjerneJournalpostListeService;
		this.tjoark051HentDokumentService = tjoark051HentDokumentService;
		this.abacSecurityService = abacSecurityService;
		this.hentDokumentUrlService = hentDokumentUrlService;
		this.abacContext = abacContext;
		this.hentKjerneJournalpostListeResponseMapper = hentKjerneJournalpostListeResponseMapper;
		this.hentKjerneJournalpostListeRequestMapper = hentKjerneJournalpostListeRequestMapper;
		this.hentDokumentRequestMapper = hentDokumentRequestMapper;
		this.hentDokumentURLV3RequestMapper = hentDokumentURLV3RequestMapper;
	}
	
	@Override
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_SAK)})
	public HentKjerneJournalpostListeResponse hentKjerneJournalpostListe(HentKjerneJournalpostListeRequest request)
			throws HentKjerneJournalpostListeUgyldigInput, HentKjerneJournalpostListeSikkerhetsbegrensning {
		try {
			hentKjerneJournalpostListeRequestValidator.validate(request);
			List<ArkivSak> filteredArkivSakList = assertAccessAndfilterHentKjerneJournalpostListe(request);
			HentKjerneJournalpostListeRequestTo requestTo = hentKjerneJournalpostListeRequestMapper.map(request, filteredArkivSakList);
			HentKjerneJournalpostListeResponseTo responseTo = hentKjerneJournalpostListeService.hentKjerneJournalpostListe(requestTo);
			log.info("tjoark058 hentet kjernejournalpostliste for sakFagsystemer={}", requestTo.getSaksListe());
			return hentKjerneJournalpostListeResponseMapper.map(responseTo);
		} catch (IllegalArgumentException e) {
			throw new HentKjerneJournalpostListeUgyldigInput(e.getMessage(), faultInfoPopulator
					.populateFaultInfo(new UgyldigInput(), e, JOURNAL_V3_HENT_KJERNE_JOURNALPOST_LISTE));
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)})
	public HentDokumentResponse hentDokument(HentDokumentRequest wsRequest) throws HentDokumentJournalpostIkkeFunnet,
			HentDokumentDokumentIkkeFunnet, HentDokumentSikkerhetsbegrensning {
		Assert.notNull(wsRequest, "Input request is null.");
		Assert.hasLength(wsRequest.getJournalpostId(), "JournalpostId is null or empty.");
		Assert.hasLength(wsRequest.getDokumentId(), "DokumentId is null or empty. journalpostId=" + wsRequest.getJournalpostId());
		Assert.notNull(wsRequest.getVariantformat(), "VariantFormat is null. journalpostId=" + wsRequest.getJournalpostId());
		Assert.hasLength(wsRequest.getVariantformat()
				.getValue(), "VariantFormat.Value is null or empty. journalpostId=" + wsRequest.getJournalpostId());
		
		try {
			assertAccessToHentDokument(wsRequest);
			HentDokumentRequestTo domainRequest = hentDokumentRequestMapper.map(wsRequest);
			byte[] dokument = tjoark051HentDokumentService.hentDokument(domainRequest);
			log.info("tjoark051 hentet dokument med journalpostId={}, dokumentId={}, variantformat={}", wsRequest.getJournalpostId(), wsRequest.getDokumentId(), wsRequest.getVariantformat().getValue());
			return new HentDokumentResponse().withDokument(dokument);
		} catch (DocumentNotFoundException e) {
			throw new HentDokumentDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, JOURNAL_V3_HENT_DOKUMENT));
		} catch (JournalpostIkkeFunnetException e) {
			throw new HentDokumentDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, JOURNAL_V3_HENT_DOKUMENT));
		}
	}
	
	@Override
	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = READ_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_DOKUMENT)})
	public HentDokumentURLResponse hentDokumentURL(HentDokumentURLRequest wsRequest) throws HentDokumentURLDokumentIkkeFunnet, HentDokumentURLSikkerhetsbegrensning {
		Assert.notNull(wsRequest, "Input request is null.");
		Assert.hasLength(wsRequest.getJournalpostId(), "JournalpostId is null or empty");
		Assert.hasLength(wsRequest.getDokumentId(), "DokumentId is null or empty. JournalpostId=" + wsRequest.getJournalpostId());
		Assert.notNull(wsRequest.getVariantformat(), "VariantFormat is null. JournalpostId=" + wsRequest.getJournalpostId());
		Assert.hasLength(wsRequest.getVariantformat()
				.getValue(), "VariantFormat.Value is null or empty. JournalpostId=" + wsRequest.getJournalpostId());
		try {
			assertAccessToHentDokumentURL(wsRequest);
			HentDokumentUrlRequestTo dokumentUrlRequestTo = hentDokumentURLV3RequestMapper.map(wsRequest);
			HentDokumentUrlResponseTo domainResponse = hentDokumentUrlService.
					hentDokumentUrl(dokumentUrlRequestTo);
			log.info("tjoark050 hentet dokumenturl med journalpostId={}, dokumentId={}, variantformat={}", wsRequest.getJournalpostId(), wsRequest.getDokumentId(), wsRequest.getVariantformat().getValue());
			return new HentDokumentURLResponse().withDokumentURL(domainResponse.getDokumentUrl());
		} catch (DocumentNotFoundException e) {
			throw new HentDokumentURLDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, JOURNAL_V3_HENT_DOKUMENT_URL));
		} catch (JournalpostIkkeFunnetException e) {
			throw new HentDokumentURLDokumentIkkeFunnet(e.getMessage(), faultInfoPopulator.populateFaultInfo(
					new DokumentIkkeFunnet(), e, JOURNAL_V3_HENT_DOKUMENT_URL));
		}
	}
	
	@Override
	public void ping() {
		// noop
	}
	
	private void assertAccessToHentDokument(HentDokumentRequest request) throws HentDokumentSikkerhetsbegrensning {
		try {
			abacSecurityService.assertAccessToJournalpost(request.getJournalpostId());
		} catch (AuthorizationException e) {
			throw new HentDokumentSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, JOURNAL_V3_HENT_DOKUMENT), e);
		}
	}

	private void assertAccessToHentDokumentURL(HentDokumentURLRequest request) throws HentDokumentURLSikkerhetsbegrensning {
		try {
			abacSecurityService.assertAccessToJournalpost(request.getJournalpostId());
		} catch (AuthorizationException e) {
			throw new HentDokumentURLSikkerhetsbegrensning(e.getMessage(),
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), e, JOURNAL_V3_HENT_DOKUMENT_URL), e);
		}
	}
	
	private List<ArkivSak> assertAccessAndfilterHentKjerneJournalpostListe(HentKjerneJournalpostListeRequest request)
			throws HentKjerneJournalpostListeSikkerhetsbegrensning {
		List<ArkivSak> filteredArkivSakList = filterArkivSakList(request);
		assertIfSikkerhetsbegrensning(request, filteredArkivSakList);
		return filteredArkivSakList;
	}

	private List<ArkivSak> filterArkivSakList(HentKjerneJournalpostListeRequest request) {
		List<ArkivSak> filteredArkivSakList = new ArrayList<>();
		for (ArkivSak arkivSak : request.getArkivSakListe()) {
			Decision abacAccess = abacSecurityService.assertAccessToSak(
					new XacmlRequest(abacContext.getRequest()),
					arkivSak.getArkivSakId(),
					FagsystemCode.valueOf(arkivSak.getArkivSakSystem()));
			if (abacAccess == Decision.PERMIT) {
				filteredArkivSakList.add(arkivSak);
			}
		}
		return filteredArkivSakList;
	}

	private void assertIfSikkerhetsbegrensning(HentKjerneJournalpostListeRequest request, List<ArkivSak> filteredArkivSakList)
			throws HentKjerneJournalpostListeSikkerhetsbegrensning {
		if (!request.getArkivSakListe().isEmpty() && filteredArkivSakList.isEmpty()) {
			AuthorizationException exception = new AuthorizationException(ACCESS_DENIED);
			throw new HentKjerneJournalpostListeSikkerhetsbegrensning(ACCESS_DENIED,
					faultInfoPopulator.populateFaultInfo(new Sikkerhetsbegrensning(), exception, JOURNAL_V3_HENT_KJERNE_JOURNALPOST_LISTE),
					exception);
		}
	}

}
