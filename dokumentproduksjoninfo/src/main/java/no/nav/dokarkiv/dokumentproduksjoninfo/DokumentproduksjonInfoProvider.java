package no.nav.dokarkiv.dokumentproduksjoninfo;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.NoDokumentInfoFoundException;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatus;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusRequestMapper;
import no.nav.dokarkiv.dokumentproduksjoninfo.tjoark120.HentJournalOgDokumentStatusResponseMapper;
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

@Slf4j
@Component
public class DokumentproduksjonInfoProvider implements DokumentproduksjonInfoV1 {

	private final HentJournalOgDokumentStatus hentJournalOgDokumentStatus;
	private final HentJournalOgDokumentStatusRequestMapper hentJournalOgDokumentStatusRequestMapper;
	private final HentJournalOgDokumentStatusResponseMapper hentJournalOgDokumentStatusResponseMapper;

	public DokumentproduksjonInfoProvider(HentJournalOgDokumentStatus hentJournalOgDokumentStatus,
										  HentJournalOgDokumentStatusRequestMapper hentJournalOgDokumentStatusRequestMapper,
										  HentJournalOgDokumentStatusResponseMapper hentJournalOgDokumentStatusResponseMapper) {
		this.hentJournalOgDokumentStatus = hentJournalOgDokumentStatus;
		this.hentJournalOgDokumentStatusRequestMapper = hentJournalOgDokumentStatusRequestMapper;
		this.hentJournalOgDokumentStatusResponseMapper = hentJournalOgDokumentStatusResponseMapper;
	}

	@Transactional(readOnly = true)
	@Override
	public HentJournalOgDokumentStatusResponse hentJournalOgDokumentStatus(HentJournalOgDokumentStatusRequest request)
			throws HentJournalOgDokumentStatusJournalpostIkkeFunnet, HentJournalOgDokumentStatusDokumentInfoIkkeFunnet {
		try {
			log.info("tjoark120 henter journal og dokumentstatus for journalpostId={}, dokumentInfoId={}", request.getJournalpostId(), request.getDokumentInfoId());
			return hentJournalOgDokumentStatusResponseMapper.map(hentJournalOgDokumentStatus
					.hentJournalOgDokumentStatus(hentJournalOgDokumentStatusRequestMapper.map(request)));
		} catch (NoJournalpostFoundException e) {
			throw new HentJournalOgDokumentStatusJournalpostIkkeFunnet(e.getMessage(), new FunctionalFault());
		} catch (NoDokumentInfoFoundException e) {
			throw new HentJournalOgDokumentStatusDokumentInfoIkkeFunnet(e.getMessage(), new FunctionalFault());
		}
	}

	@Deprecated
	@Override
	public HentJournalpostInfoResponse hentJournalpostInfo(HentJournalpostInfoRequest hentJournalpostInfoRequest) throws HentJournalpostInfoJournalpostIkkeFunnet, HentJournalpostInfoDokumentInfoIkkeFunnet {
		throw new UnsupportedOperationException("hentJournalpostInfo er sanert 2023-02");
	}

	@Deprecated
	@Override
	public HentFerdigstilteDokumenterResponse hentFerdigstilteDokumenter(HentFerdigstilteDokumenterRequest request)
			throws HentFerdigstilteDokumenterUgyldingInput, HentFerdigstilteDokumenterDokumenterKanIkkeHentes, HentFerdigstilteDokumenterDokumenterIkkeFunnet {
		throw new UnsupportedOperationException("hentFerdigstilteDokumenter er sanert 2023-02");
	}

	@Override
	public void ping() {
		// Noop
	}
}
