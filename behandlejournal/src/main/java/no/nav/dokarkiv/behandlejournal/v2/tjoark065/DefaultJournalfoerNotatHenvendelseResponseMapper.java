package no.nav.dokarkiv.behandlejournal.v2.tjoark065;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerNotatResponse;

/**
 * Default implementation
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public class DefaultJournalfoerNotatHenvendelseResponseMapper implements
		JournalfoerNotatHenvendelseResponseMapper {
//	private Mapper dozerMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JournalfoerNotatResponse map(
			JournalfoerNotatHenvendelseResponse domainResponse) {
//		return dozerMapper.map(domainResponse, JournalfoerNotatResponse.class);
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
