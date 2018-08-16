package no.nav.dokarkiv.behandlejournal.v3;

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseResponseMapper;
import no.nav.dokarkiv.core.domain.entities.bidrag.BidragMellomlagring;
import no.nav.dokarkiv.core.exceptions.NoJournalpostFoundException;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.feil.JournalpostIkkeFunnet;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * POJO BehandleJournalProvider for MOD-services managed by Spring. Maps from
 * and to WS model (FIM) and delegates to Service implementations.
 *
 * @author Rune Romundstad, Visma Consulting
 *
 */
@Slf4j
@Component
public class BehandleJournalV3Provider implements BehandleJournalV3 {

	@Inject
	private BehandleJournalV3Pep behandleJournalV3Pep;
	@Inject
	private BehandleJournalV3ServiceBi behandleJournalV3ServiceBi;
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
		ArkiverUstrukturertKravResponse response = arkiverUstrukturertKravResponseMapper.map(behandleJournalV3ServiceBi
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequestMapper.map(request)));
		log.info("tjoark060 arkiverer ustrukturert krav i {}={}, dokumentId={}",
				journalpostOrBidragClassifier(response.getJournalpostId()), response.getJournalpostId(), response.getDokumentId());
		return response;
	}

	@Transactional
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		try {
			LagreVedleggPaaJournalpostResponse response = lagreVedleggPaaJournalpostResponseMapper.map(behandleJournalV3ServiceBi
					.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequestMapper.map(request)));
			log.info("tjoark061 lagret vedlegg dokumentId={} til {}={}",
					response.getDokumentId(), journalpostOrBidragClassifier(request.getJournalpostId()), request.getJournalpostId());
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
			behandleJournalV3ServiceBi.ferdigstillDokumentopplasting(ferdigstillDokumentopplastingRequestMapper
					.map(request));
			log.info("tjoark063 ferdigstilte dokumentopplasting {}={}",
					journalpostOrBidragClassifier(request.getJournalpostId()), request.getJournalpostId());
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
				.map(behandleJournalV3ServiceBi
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
				.map(behandleJournalV3ServiceBi
						.journalfoerUtgaaendeHenvendelse(journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper
								.map(request)));
		log.info("tjoark064 journalførte utgående henvendelse i journalpostId={}", response.getJournalpostId());
		return response;
	}

	@Transactional
	@Abac(actions = @Abac.Attr(key = ACTION_ID, value = CREATE_ACTION),
			resources = {@Abac.Attr(key = RESOURCE_FELLES_RESOURCE_TYPE, value = RESOURCE_ARKIV_JOURNALPOST)})
	@Override
	public JournalfoerNotatResponse journalfoerNotat(JournalfoerNotatRequest request) throws JournalfoerNotatSikkerhetsbegrensning {
		JournalfoerNotatHenvendelseRequest henvendelseRequest = journalfoerNotatHenvendelseRequestMapper.map(request);
		try {
			behandleJournalV3Pep.journalfoerNotatPep(henvendelseRequest);
			JournalfoerNotatResponse response = journalfoerNotatHenvendelseResponseMapper.map(behandleJournalV3ServiceBi
					.journalfoerNotatHenvendelse(henvendelseRequest));
			log.info("tjoark065 journalførte notat i journalpostId={}", response.getJournalpostId());
			return response;
		} catch(AuthorizationException e) {
			throw new JournalfoerNotatSikkerhetsbegrensning(e.getMessage());
		}
	}

	@Override
	public void ping() {
		//noop
	}

	private String getOperationName() {
		return Thread.currentThread().getStackTrace()[2].getMethodName();
	}

	private String journalpostOrBidragClassifier(String id) {
		if(id != null && id.startsWith(BidragMellomlagring.ID_PREFIX.toString())) {
			return "bidragMellomlagringId";
		} else {
			return "journalpostId";
		}
	}
}
