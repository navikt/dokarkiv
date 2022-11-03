package no.nav.dokarkiv.behandlejournal.v3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark060.ArkiverUstrukturertKravV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark061.LagreVedleggPaaJournalpostV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark062.FerdigstillDokumentopplastingV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark063.JournalfoerInngaaendeHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark064.JournalfoerUtgaaendeHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3ResponseMapper;
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

import static no.nav.abac.xacml.NavAttributter.RESOURCE_ARKIV_JOURNALPOST;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.xacml.StandardAttributter.ACTION_ID;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;

/**
 * POJO BehandleJournalProvider for MOD-services managed by Spring. Maps from
 * and to WS model (FIM) and delegates to Service implementations.
 */
@Slf4j
@Component
public class BehandleJournalV3Provider implements BehandleJournalV3 {

	private final BehandleJournalV3Pep behandleJournalV3Pep;
	private final BehandleJournalV3ServiceBi behandleJournalV3ServiceBi;
	private final BehandleJournalV3FaultInfoPopulator behandleJournalV3FaultInfoPopulator;
	private final ArkiverUstrukturertKravV3RequestMapper arkiverUstrukturertKravRequestMapper;
	private final ArkiverUstrukturertKravV3ResponseMapper arkiverUstrukturertKravResponseMapper;
	private final LagreVedleggPaaJournalpostV3RequestMapper lagreVedleggPaaJournalpostRequestMapper;
	private final LagreVedleggPaaJournalpostV3ResponseMapper lagreVedleggPaaJournalpostResponseMapper;
	private final FerdigstillDokumentopplastingV3RequestMapper ferdigstillDokumentopplastingRequestMapper;
	private final JournalfoerInngaaendeHenvendelseV3RequestMapper journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper;
	private final JournalfoerInngaaendeHenvendelseV3ResponseMapper journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper;
	private final JournalfoerUtgaaendeHenvendelseV3RequestMapper journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper;
	private final JournalfoerUtgaaendeHenvendelseV3ResponseMapper journalfoerUtgaaendeHenvendelseResponseMapper;
	private final JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapper;
	private final JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapper;

	public BehandleJournalV3Provider(BehandleJournalV3Pep behandleJournalV3Pep, BehandleJournalV3ServiceBi behandleJournalV3ServiceBi, BehandleJournalV3FaultInfoPopulator behandleJournalV3FaultInfoPopulator, ArkiverUstrukturertKravV3RequestMapper arkiverUstrukturertKravRequestMapper, ArkiverUstrukturertKravV3ResponseMapper arkiverUstrukturertKravResponseMapper, LagreVedleggPaaJournalpostV3RequestMapper lagreVedleggPaaJournalpostRequestMapper, LagreVedleggPaaJournalpostV3ResponseMapper lagreVedleggPaaJournalpostResponseMapper, FerdigstillDokumentopplastingV3RequestMapper ferdigstillDokumentopplastingRequestMapper, JournalfoerInngaaendeHenvendelseV3RequestMapper journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper, JournalfoerInngaaendeHenvendelseV3ResponseMapper journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper, JournalfoerUtgaaendeHenvendelseV3RequestMapper journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper, JournalfoerUtgaaendeHenvendelseV3ResponseMapper journalfoerUtgaaendeHenvendelseResponseMapper, JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapper, JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapper) {
		this.behandleJournalV3Pep = behandleJournalV3Pep;
		this.behandleJournalV3ServiceBi = behandleJournalV3ServiceBi;
		this.behandleJournalV3FaultInfoPopulator = behandleJournalV3FaultInfoPopulator;
		this.arkiverUstrukturertKravRequestMapper = arkiverUstrukturertKravRequestMapper;
		this.arkiverUstrukturertKravResponseMapper = arkiverUstrukturertKravResponseMapper;
		this.lagreVedleggPaaJournalpostRequestMapper = lagreVedleggPaaJournalpostRequestMapper;
		this.lagreVedleggPaaJournalpostResponseMapper = lagreVedleggPaaJournalpostResponseMapper;
		this.ferdigstillDokumentopplastingRequestMapper = ferdigstillDokumentopplastingRequestMapper;
		this.journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper = journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper;
		this.journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper = journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper;
		this.journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper = journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper;
		this.journalfoerUtgaaendeHenvendelseResponseMapper = journalfoerUtgaaendeHenvendelseResponseMapper;
		this.journalfoerNotatHenvendelseRequestMapper = journalfoerNotatHenvendelseRequestMapper;
		this.journalfoerNotatHenvendelseResponseMapper = journalfoerNotatHenvendelseResponseMapper;
	}

	@Transactional
	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request) {
		ArkiverUstrukturertKravResponse response = arkiverUstrukturertKravResponseMapper.map(behandleJournalV3ServiceBi
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequestMapper.map(request)));
		log.info("tjoark060 arkiverer ustrukturert krav i journalpostId={}, dokumentId={}", response.getJournalpostId(), response.getDokumentId());
		return response;
	}

	@Transactional
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		try {
			LagreVedleggPaaJournalpostResponse response = lagreVedleggPaaJournalpostResponseMapper.map(behandleJournalV3ServiceBi
					.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequestMapper.map(request)));
			log.info("tjoark061 lagret vedlegg dokumentId={} til journalpostId={}", response.getDokumentId(), request.getJournalpostId());
			return response;
		} catch (NoJournalpostFoundException e) {
			throw new LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet(e.getMessage(),
					behandleJournalV3FaultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e,
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
			log.info("tjoark063 ferdigstilte dokumentopplasting journalpostId={}", request.getJournalpostId());
		} catch (NoJournalpostFoundException e) {
			throw new FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet(e.getMessage(),
					behandleJournalV3FaultInfoPopulator.populateFaultInfo(new JournalpostIkkeFunnet(), e,
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
		} catch (AuthorizationException e) {
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

}
