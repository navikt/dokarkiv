package no.nav.dokarkiv.behandlejournal.v2;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark060.ArkiverUstrukturertKravResponseMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark061.LagreVedleggPaaJournalpostResponseMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark062.FerdigstillDokumentopplastingRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark063.JournalfoerInngaaendeHenvendelseResponseMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseResponseMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v2.tjoark065.JournalfoerNotatHenvendelseResponseMapper;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.feil.JournalpostIkkeFunnet;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * POJO BehandleJournalProvider for MOD-services managed by Spring. Maps from
 * and to WS model (FIM) and delegates to Service implementations.
 *
 * @author Rune Romundstad, Visma Consulting
 */
@Slf4j
@Component
public class BehandleJournalProvider implements BehandleJournalV2 {

	@Inject
	private BehandleJournalServiceBi behandleJournalServiceBi;
	@Inject
	private BehandleJournalFaultInfoPopulator behandleJournalFaultInfoPopulator;
	@Inject
	private ArkiverUstrukturertKravRequestMapper arkiverUstrukturertKravRequestMapper;
	@Inject
	private ArkiverUstrukturertKravResponseMapper arkiverUstrukturertKravResponseMapper;
	@Inject
	private LagreVedleggPaaJournalpostRequestMapper lagreVedleggPaaJournalpostRequestMapper;
	@Inject
	private LagreVedleggPaaJournalpostResponseMapper lagreVedleggPaaJournalpostResponseMapper;
	@Inject
	private FerdigstillDokumentopplastingRequestMapper ferdigstillDokumentopplastingRequestMapper;
	@Inject
	private JournalfoerInngaaendeHenvendelseRequestMapper journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper;
	@Inject
	private JournalfoerInngaaendeHenvendelseResponseMapper journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper;
	@Inject
	private JournalfoerUtgaaendeHenvendelseRequestMapper journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper;
	@Inject
	private JournalfoerUtgaaendeHenvendelseResponseMapper journalfoerUtgaaendeHenvendelseResponseMapper;
	@Inject
	private JournalfoerNotatHenvendelseRequestMapper journalfoerNotatHenvendelseRequestMapper;
	@Inject
	private JournalfoerNotatHenvendelseResponseMapper journalfoerNotatHenvendelseResponseMapper;

	@Transactional
	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request) {
		ArkiverUstrukturertKravResponse response = arkiverUstrukturertKravResponseMapper.map(behandleJournalServiceBi
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequestMapper.map(request)));
		log.info("tjoark060 arkiverer ustrukturert krav i journalpostId={}, dokumentId={}",
				response.getJournalpostId(), response.getDokumentId());
		return response;
	}

	@Transactional
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		try {
			LagreVedleggPaaJournalpostResponse response = lagreVedleggPaaJournalpostResponseMapper.map(behandleJournalServiceBi
					.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequestMapper.map(request)));
			log.info("tjoark061 lagret vedlegg dokumentId={} til journalpostId={}",
					response.getDokumentId(), request.getJournalpostId());
			return response;
		} catch (NoJournalpostFoundException e) {
			throw new LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet(e.getMessage(),
					behandleJournalFaultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e,
							getOperationName()));
		}
	}

	@Transactional
	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest request)
			throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {
		try {
			behandleJournalServiceBi.ferdigstillDokumentopplasting(ferdigstillDokumentopplastingRequestMapper
					.map(request));
			log.info("tjoark062 ferdigstilte dokumentopplasting journalpostId={}", request.getJournalpostId());
		} catch (NoJournalpostFoundException e) {
			throw new FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet(e.getMessage(),
					behandleJournalFaultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e,
							getOperationName()));
		}
	}

	@Transactional
	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest request) {
		JournalfoerInngaaendeHenvendelseResponse response = journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper
				.map(behandleJournalServiceBi
						.journalfoerInngaaendeHenvendelse(journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper
								.map(request)));
		log.info("tjoark063 journalførte inngående henvendelse i journalpostId={}", response.getJournalpostId());
		return response;
	}

	@Transactional
	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest request) {
		JournalfoerUtgaaendeHenvendelseResponse response = journalfoerUtgaaendeHenvendelseResponseMapper
				.map(behandleJournalServiceBi
						.journalfoerUtgaaendeHenvendelse(journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper
								.map(request)));
		log.info("tjoark064 journalførte utgående henvendelse i journalpostId={}", response.getJournalpostId());
		return response;
	}

	@Transactional
	@Override
	public JournalfoerNotatResponse journalfoerNotat(
			JournalfoerNotatRequest request) {
		JournalfoerNotatResponse response = journalfoerNotatHenvendelseResponseMapper.map(behandleJournalServiceBi
				.journalfoerNotatHenvendelse(journalfoerNotatHenvendelseRequestMapper
						.map(request)));
		log.info("tjoark065 journalførte notat i journalpostId={}", response.getJournalpostId());
		return response;
	}

	@Override
	public void ping() {
		//noop
	}

	private String getOperationName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

}
