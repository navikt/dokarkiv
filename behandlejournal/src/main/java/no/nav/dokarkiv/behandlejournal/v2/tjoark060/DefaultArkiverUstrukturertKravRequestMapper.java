package no.nav.dokarkiv.behandlejournal.v2.tjoark060;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.SporingUtil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link ArkiverUstrukturertKravRequestMapper}.
 * Maps request from FIM to Joark domain.
 */
@Component
public class DefaultArkiverUstrukturertKravRequestMapper implements ArkiverUstrukturertKravRequestMapper {

	private final JournalpostMapper journalpostMapper;
	private final SporingMapper sporingMapper;

	public DefaultArkiverUstrukturertKravRequestMapper(JournalpostMapper journalpostMapper, SporingMapper sporingMapper) {
		this.journalpostMapper = journalpostMapper;
		this.sporingMapper = sporingMapper;
	}

	/** {@inheritDoc} */
	@Override
	public ArkiverUstrukturertKravRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.ArkiverUstrukturertKravRequest wsRequest) {
		Journalpost domainJournalpost = journalpostMapper.map(wsRequest.getJournalpost());
		sporingMapper.mapSporingsinfo(
				domainJournalpost,
				SporingUtil.decideSporingNavn(wsRequest.getPersonFornavn(), wsRequest.getPersonEtternavn(),
						wsRequest.getApplikasjonsID()));
		return new ArkiverUstrukturertKravRequest(domainJournalpost);
	}
}
