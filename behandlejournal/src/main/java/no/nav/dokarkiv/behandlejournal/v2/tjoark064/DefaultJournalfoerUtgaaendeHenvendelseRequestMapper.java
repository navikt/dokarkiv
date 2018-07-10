package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import no.nav.dokarkiv.behandlejournal.v2.SporingMapper;

import javax.inject.Inject;

/**
 * Implementation of
 * JournalfoerUtgaaendeHenvendelseRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultJournalfoerUtgaaendeHenvendelseRequestMapper implements
		JournalfoerUtgaaendeHenvendelseRequestMapper {

//	private Mapper dozerMapper;
	@Inject
	private SporingMapper sporingMapper;

	@Override
	public JournalfoerUtgaaendeHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseRequest wsRequest) {
//		Journalpost domainJournalpost = dozerMapper.map(wsRequest.getJournalpost(), Journalpost.class);
//		sporingMapper.mapSporingsinfo(domainJournalpost, wsRequest.getJournalpost().getOpprettetAvNavn());
//
//		return new JournalfoerUtgaaendeHenvendelseRequest(domainJournalpost); FIXME
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
