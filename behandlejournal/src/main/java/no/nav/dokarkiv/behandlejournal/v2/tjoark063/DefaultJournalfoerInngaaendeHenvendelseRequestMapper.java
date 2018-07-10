package no.nav.dokarkiv.behandlejournal.v2.tjoark063;

import no.nav.dokarkiv.behandlejournal.v2.SporingMapper;

import javax.inject.Inject;


/**
 * Implementation of
 * JournalfoerInngaaendeHenvendelseRequestMapper
 *
 * @author Rune Romundstad, Visma Consulting
 */
public class DefaultJournalfoerInngaaendeHenvendelseRequestMapper implements
		JournalfoerInngaaendeHenvendelseRequestMapper {

//	private Mapper dozerMapper;
	@Inject
	private SporingMapper sporingMapper;

	@Override
	public JournalfoerInngaaendeHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest wsRequest) {
//		Journalpost domainJournalpost = dozerMapper.map(wsRequest.getJournalpost(), Journalpost.class); FIXME
//		sporingMapper.mapSporingsinfo(domainJournalpost, wsRequest.getJournalpost().getOpprettetAvNavn());

//		return new JournalfoerInngaaendeHenvendelseRequest(domainJournalpost);
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
