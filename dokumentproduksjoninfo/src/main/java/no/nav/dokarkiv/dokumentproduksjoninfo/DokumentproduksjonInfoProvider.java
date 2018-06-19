package no.nav.dokarkiv.dokumentproduksjoninfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.DokumentInfoNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.FilDetaljerNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalDokumentstatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalJournalStatusException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.IllegalVariantFormatException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.JournalpostNotFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatus;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterResponseTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark121.HentFerdigstilteDokumenterService;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoRequestMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoRequestTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoResponseMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoResponseTo;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122.HentJournalpostInfoService;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.DokumentproduksjonInfoV1;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterDokumenterIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterDokumenterKanIkkeHentes;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentFerdigstilteDokumenterUgyldingInput;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalOgDokumentStatusJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoDokumentInfoIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.HentJournalpostInfoJournalpostIkkeFunnet;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.feil.FunctionalFault;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentFerdigstilteDokumenterResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalOgDokumentStatusResponse;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoRequest;
import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.inject.Inject;
import java.util.List;

/**
 * Provider delegate for the DokumentproduksjonInfo webservice
 *
 * @author Thomas Eugen Bjørge, Visma Consulting
 */
@Slf4j
@Component
public class DokumentproduksjonInfoProvider implements DokumentproduksjonInfoV1 {

	@Inject 
	private HentJournalOgDokumentStatus hentJournalOgDokumentStatus;

	@Inject
	private HentJournalpostInfoService hentJournalpostInfoService;

	@Inject
	private HentFerdigstilteDokumenterService hentFerdigstilteDokumenterService;
	
	@Inject
	private HentJournalOgDokumentStatusRequestMapper hentJournalOgDokumentStatusRequestMapper;
	
	@Inject
	private HentJournalOgDokumentStatusResponseMapper hentJournalOgDokumentStatusResponseMapper;

	@Inject
	private HentJournalpostInfoRequestMapper hentJournalpostInfoRequestMapper;

	@Inject
	private HentJournalpostInfoResponseMapper hentJournalpostInfoResponseMapper;

	@Inject
	private HentFerdigstilteDokumenterResponseMapper hentFerdigstilteDokumenterResponseMapper;
	
	@Transactional(readOnly = true)
	@Override
	public HentJournalOgDokumentStatusResponse hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequest request)
			throws HentJournalOgDokumentStatusJournalpostIkkeFunnet, HentJournalOgDokumentStatusDokumentInfoIkkeFunnet {
		try {
			return hentJournalOgDokumentStatusResponseMapper.map(hentJournalOgDokumentStatus
					.hentJournalOgDokumentStatus(hentJournalOgDokumentStatusRequestMapper.map(request)));
		} catch (NoJournalpostFoundException e) {
			throw new HentJournalOgDokumentStatusJournalpostIkkeFunnet(e.getMessage(), new FunctionalFault());
		} catch (NoDokumentInfoFoundException e) {
			throw new HentJournalOgDokumentStatusDokumentInfoIkkeFunnet(e.getMessage(), new FunctionalFault());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public HentJournalpostInfoResponse hentJournalpostInfo(HentJournalpostInfoRequest hentJournalpostInfoRequest) throws HentJournalpostInfoJournalpostIkkeFunnet, HentJournalpostInfoDokumentInfoIkkeFunnet {
		try {
			HentJournalpostInfoRequestTo request = hentJournalpostInfoRequestMapper.map(hentJournalpostInfoRequest);
			HentJournalpostInfoResponseTo responseTo = hentJournalpostInfoService.hentJournalOgDokumentStatus(request);
			return hentJournalpostInfoResponseMapper.map(responseTo);
		} catch (NoJournalpostFoundException e) {
			throw new HentJournalpostInfoJournalpostIkkeFunnet(e.getMessage(), new FunctionalFault());
		} catch (NoDokumentInfoFoundException e) {
			throw new HentJournalpostInfoDokumentInfoIkkeFunnet(e.getMessage(), new FunctionalFault());
		}
	}

	@Transactional(readOnly = true)
	@Override
	public HentFerdigstilteDokumenterResponse hentFerdigstilteDokumenter(HentFerdigstilteDokumenterRequest request)
			throws HentFerdigstilteDokumenterUgyldingInput, HentFerdigstilteDokumenterDokumenterKanIkkeHentes, HentFerdigstilteDokumenterDokumenterIkkeFunnet {
		if (request == null) {
			throw new HentFerdigstilteDokumenterUgyldingInput("request is null", new FunctionalFault());
		}
		if (request.getJournalpostId() == 0) {
			throw new HentFerdigstilteDokumenterUgyldingInput("journalpostId is null or 0", new FunctionalFault());
		}
		if (CollectionUtils.isEmpty(request.getDokumentInfoListe())) {
			throw new HentFerdigstilteDokumenterUgyldingInput("List with dokumentInfo is null or empty. journalpostId=" + request.getJournalpostId(), new FunctionalFault());
		}
		List<HentFerdigstilteDokumenterResponseTo> domainResponse;
		try {
			domainResponse = hentFerdigstilteDokumenterService
					.hentFerdigstilteDokumenter(request.getJournalpostId(), request.getDokumentInfoListe());
		} catch (IllegalJournalStatusException | IllegalVariantFormatException | IllegalDokumentstatusException e) {
			throw  new HentFerdigstilteDokumenterDokumenterKanIkkeHentes(e.getMessage(), new FunctionalFault());
		} catch (FilDetaljerNotFoundException | DokumentInfoNotFoundException | JournalpostNotFoundException e) {
			throw  new HentFerdigstilteDokumenterDokumenterIkkeFunnet(e.getMessage(), new FunctionalFault());
		}

		return hentFerdigstilteDokumenterResponseMapper.map(domainResponse);
	}

	@Override
	public void ping() {
		// Noop
	}
}
