package no.nav.dokarkiv.behandlejournal.v3;

import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseRequest;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3RequestMapper;
import no.nav.dokarkiv.behandlejournal.v3.tjoark065.JournalfoerNotatHenvendelseV3ResponseMapper;
import no.nav.dokarkiv.core.security.abac.AuthorizationException;
import no.nav.freg.abac.core.annotation.Abac;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.BehandleJournalV3;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.JournalfoerNotatSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.behandlejournal.v3.binding.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
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
import static no.nav.dokarkiv.behandlejournal.v3.BehandleJournalV3Endpoint.OPERASJONEN_ER_SANERT;
import static no.nav.dokarkiv.core.security.abac.JoarkAbacAttributes.CREATE_ACTION;

/**
 * POJO BehandleJournalProvider for MOD-services managed by Spring. Maps from
 * and to WS model (FIM) and delegates to Service implementations.
 */
@Slf4j
@Transactional(readOnly = true)
@Component
public class BehandleJournalV3Provider implements BehandleJournalV3 {

	private final BehandleJournalV3Pep behandleJournalV3Pep;
	private final BehandleJournalV3ServiceBi behandleJournalV3ServiceBi;
	private final JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapper;
	private final JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapper;

	public BehandleJournalV3Provider(BehandleJournalV3Pep behandleJournalV3Pep, BehandleJournalV3ServiceBi behandleJournalV3ServiceBi, JournalfoerNotatHenvendelseV3RequestMapper journalfoerNotatHenvendelseRequestMapper, JournalfoerNotatHenvendelseV3ResponseMapper journalfoerNotatHenvendelseResponseMapper) {
		this.behandleJournalV3Pep = behandleJournalV3Pep;
		this.behandleJournalV3ServiceBi = behandleJournalV3ServiceBi;
		this.journalfoerNotatHenvendelseRequestMapper = journalfoerNotatHenvendelseRequestMapper;
		this.journalfoerNotatHenvendelseResponseMapper = journalfoerNotatHenvendelseResponseMapper;
	}

	@Override
	public ArkiverUstrukturertKravResponse arkiverUstrukturertKrav(ArkiverUstrukturertKravRequest request) {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public LagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(LagreVedleggPaaJournalpostRequest request)
			throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public void ferdigstillDokumentopplasting(FerdigstillDokumentopplastingRequest request)
			throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public JournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(
			JournalfoerInngaaendeHenvendelseRequest request) {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
	}

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(
			JournalfoerUtgaaendeHenvendelseRequest request) {
		throw new UnsupportedOperationException(OPERASJONEN_ER_SANERT);
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

}
