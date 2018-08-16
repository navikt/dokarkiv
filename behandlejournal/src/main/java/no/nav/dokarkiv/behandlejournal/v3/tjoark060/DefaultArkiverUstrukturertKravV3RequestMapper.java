package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.SporingUtil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of {@link ArkiverUstrukturertKravRequestMapper}. Maps request
 * from FIM to Joark domain.
 * 
 * @author Rune Romundstad, Visma Consulting
 */
@Component
public class DefaultArkiverUstrukturertKravV3RequestMapper implements ArkiverUstrukturertKravRequestMapper {

	@Inject
	private JournalpostMapper journalpostMapper;
	@Inject
	private SporingMapper sporingMapper;

	/** {@inheritDoc} */
	@Override
	public ArkiverUstrukturertKravRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v3.meldinger.ArkiverUstrukturertKravRequest wsRequest) {
		Journalpost domainJournalpost = journalpostMapper.map(wsRequest.getJournalpost());
		sporingMapper.mapSporingsinfo(
				domainJournalpost,
				SporingUtil.decideSporingNavn(wsRequest.getPersonFornavn(), wsRequest.getPersonEtternavn(),
						wsRequest.getApplikasjonsID()));
		return new ArkiverUstrukturertKravRequest(domainJournalpost);
	}
}
