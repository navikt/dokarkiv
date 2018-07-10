package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.dokarkiv.behandlejournal.v2.SporingMapper;

import javax.inject.Inject;

/**
 * Implementation of JournalfoerNotatHenvendelseRequestMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultJournalfoerNotatHenvendelseRequestMapper implements
		JournalfoerNotatHenvendelseRequestMapper {

//	private Mapper dozerMapper;
	@Inject
	private SporingMapper sporingMapper;

	@Override
	public JournalfoerNotatHenvendelseRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatRequest wsRequest) {
//		Journalpost domainJournalpost = dozerMapper.map(wsRequest.getJournalpost(), Journalpost.class);
//		sporingMapper.mapSporingsinfo(domainJournalpost, wsRequest.getJournalpost().getOpprettetAvNavn());
//
//		return new JournalfoerNotatHenvendelseRequest(domainJournalpost); FIXME
		return null;
	}
//
//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
