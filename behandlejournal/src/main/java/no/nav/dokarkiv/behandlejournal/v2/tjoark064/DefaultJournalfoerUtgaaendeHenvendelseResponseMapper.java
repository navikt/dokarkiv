package no.nav.dokarkiv.behandlejournal.v2.tjoark064;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerUtgaaendeHenvendelseResponse;
import org.springframework.stereotype.Component;

/**
 * Implementation of
 * JournalfoerUtgaaendeHenvendelseResponseMapper
 *
 * @author Joakim Bjørnstad, Visma Consulting
 *
 */
@Component
public class DefaultJournalfoerUtgaaendeHenvendelseResponseMapper implements
		JournalfoerUtgaaendeHenvendelseResponseMapper {
//	private Mapper dozerMapper;

	@Override
	public JournalfoerUtgaaendeHenvendelseResponse map(
			no.nav.dokarkiv.behandlejournal.v2.tjoark064.JournalfoerUtgaaendeHenvendelseResponse domainResponse) {
//		return dozerMapper.map(domainResponse, JournalfoerUtgaaendeHenvendelseResponse.class); FIXME
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
