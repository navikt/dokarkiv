package no.nav.dokarkiv.behandlejournal.v2.tjoark062;

/**
 * Implementation of {@link FerdigstillDokumentopplastingRequestMapper}.
 * 
 * @author Joakim Bjørnstad, Visma Consulting
 * 
 */
public class DefaultFerdigstillDokumentopplastingRequestMapper implements FerdigstillDokumentopplastingRequestMapper {
//	private Mapper dozerMapper;

	/** {@inheritDoc} */
	@Override
	public FerdigstillDokumentopplastingRequest map(
			no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.FerdigstillDokumentopplastingRequest wsRequest) {
//		return dozerMapper.map(wsRequest, FerdigstillDokumentopplastingRequest.class);
		return null;
	}

//	@Inject
//	@Named("dozerMapper")
//	public void setDozerMapper(Mapper dozerMapper) {
//		this.dozerMapper = dozerMapper;
//	}
}
