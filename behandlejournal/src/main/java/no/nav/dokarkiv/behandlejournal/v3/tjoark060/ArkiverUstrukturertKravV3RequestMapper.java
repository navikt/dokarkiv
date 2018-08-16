package no.nav.dokarkiv.behandlejournal.v3.tjoark060;

import no.nav.dokarkiv.behandlejournal.SporingMapper;
import no.nav.dokarkiv.behandlejournal.SporingUtil;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class ArkiverUstrukturertKravV3RequestMapper {

	private final ArkiverUstrukturertKravV3JournalpostMapper journalpostMapper;
	private final SporingMapper sporingMapper;

	@Inject
	public ArkiverUstrukturertKravV3RequestMapper(ArkiverUstrukturertKravV3JournalpostMapper journalpostMapper, SporingMapper sporingMapper) {
		this.journalpostMapper = journalpostMapper;
		this.sporingMapper = sporingMapper;
	}

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
