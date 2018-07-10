package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.behandlejournal.v2.exceptions.NoJournalpostFoundException;
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
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
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
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * POJO BehandleJournalProvider for MOD-services managed by Spring. Maps from
 * and to WS model (FIM) and delegates to Service implementations.
 *
 * @author Rune Romundstad, Visma Consulting
 *
 */
public class BehandleJournalProvider implements BehandleJournalV2 {
	
	static final String BEHANDLE_JOURNAL_V2 = "BehandleJournalV2";
	static final String BEHANDLE_JOURNAL_V2_LAGRE_VEDLEGG_JOURNALPOST = BEHANDLE_JOURNAL_V2 + ".lagreVedleggPaaJournalpost";
	static final String BEHANDLE_JOURNAL_V2_ARKIVER_USTRUKTURERT_KRAV = BEHANDLE_JOURNAL_V2 + ".arkiverUstrukturertKrav";
	static final String BEHANDLE_JOURNAL_V2_FERDIGSTILL_DOKUMENTOPPLASTING = BEHANDLE_JOURNAL_V2 + ".ferdigstillDokumentopplasting";
	static final String BEHANDLE_JOURNAL_V2_JOURNALFOER_INNGAAENDE_HENVENDELSE = BEHANDLE_JOURNAL_V2 + ".journalfoerInngaaendeHenvendelse";
	static final String BEHANDLE_JOURNAL_V2_JOURNALFOER_UTGAAENDE_HENVENDELSE = BEHANDLE_JOURNAL_V2 + ".journalfoerUtgaaendeHenvendelse";
	static final String BEHANDLE_JOURNAL_V2_JOURNALFOER_NOTAT = BEHANDLE_JOURNAL_V2 + ".journalfoerNotat";


	@Inject
	@Named("srv.joark.mod.behandleJournalService")
	private BehandleJournalServiceBi modBehandleJournalService;
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
		return arkiverUstrukturertKravResponseMapper.map(modBehandleJournalService
				.arkiverUstrukturertKrav(arkiverUstrukturertKravRequestMapper.map(request)));
	}

	@Transactional
	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		try {
			return lagreVedleggPaaJournalpostResponseMapper.map(modBehandleJournalService
					.lagreVedleggPaaJournalpost(lagreVedleggPaaJournalpostRequestMapper.map(request)));
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
			modBehandleJournalService.ferdigstillDokumentopplasting(ferdigstillDokumentopplastingRequestMapper
					.map(request));
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

		return journalfoerInngaaendeHenvendelseMedHoveddokumentResponseMapper
				.map(modBehandleJournalService
						.journalfoerInngaaendeHenvendelse(journalfoerInngaaendeHenvendelseMedHoveddokumentRequestMapper
								.map(request)));
	}

	@Transactional
	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest request) {
		return journalfoerUtgaaendeHenvendelseResponseMapper
				.map(modBehandleJournalService
						.journalfoerUtgaaendeHenvendelse(journalfoerUtgaaendeHenvendelseMedHoveddokumentRequestMapper
								.map(request)));
	}

	@Transactional
	@Override
	public JournalfoerNotatResponse journalfoerNotat(
			JournalfoerNotatRequest request) {
		return journalfoerNotatHenvendelseResponseMapper.map(modBehandleJournalService
				.journalfoerNotatHenvendelse(journalfoerNotatHenvendelseRequestMapper
						.map(request)));
	}

	@Override
	public void ping() {
		//noop
	}

	private String getOperationName() {
		return Thread.currentThread().getStackTrace()[3].getMethodName();
	}
}
