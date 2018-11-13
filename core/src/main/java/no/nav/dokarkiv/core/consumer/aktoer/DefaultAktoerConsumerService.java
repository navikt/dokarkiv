package no.nav.dokarkiv.core.consumer.aktoer;

import com.google.common.cache.Cache;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Default implementation of {@link AktoerConsumerService}
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
@Component
public class DefaultAktoerConsumerService implements AktoerConsumerService {

	@Inject
	private AktoerV2 aktoerV2;
	@Inject
	private HentAktoerIdForIdentRequestMapper requestAktoerMapper;
	@Inject
	private HentAktoerIdForIdentResponseMapper responseAktoerMapper;
	@Inject
	private Cache<String, HentAktoerIdForIdentResponse> aktoerResponseCache;
	@Inject
	private Cache<String, HentIdentForAktoerIdResponse> identResponseCache;
	@Inject
	private HentIdentForAktoerIdRequestMapper requestIdentMapper;
	@Inject
	private HentIdentForAktoerIdResponseMapper responseIdentMapper;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HentAktoerIdForIdentResponseTo hentAktoerIdForIdent(HentAktoerIdForIdentRequestTo request) throws PersonIkkeFunnetException {
		HentAktoerIdForIdentResponse response = aktoerResponseCache.getIfPresent(request.getIdent());
		if (response == null) {
			try {
				response = aktoerV2.hentAktoerIdForIdent(requestAktoerMapper.map(request));
				aktoerResponseCache.put(request.getIdent(), response);
			} catch (HentAktoerIdForIdentPersonIkkeFunnet e) {
				throw new PersonIkkeFunnetException(e, "Fant ikke person med ident: " + request.getIdent());
			}
		}
		return responseAktoerMapper.map(response);
	}

	@Override
	public HentIdentForAktoerIdResponseTo hentIdentForAktoerId(HentIdentForAktoerIdRequestTo request) throws PersonIkkeFunnetException {
		HentIdentForAktoerIdResponse response = identResponseCache.getIfPresent(request.getAktoerId());
		if (response == null) {
			try {
				response = aktoerV2.hentIdentForAktoerId(requestIdentMapper.map(request));
				identResponseCache.put(request.getAktoerId(), response);
			} catch (HentIdentForAktoerIdPersonIkkeFunnet e) {
				throw new PersonIkkeFunnetException(e, "Fant ikke person med ident: " + request.getAktoerId());
			}
		}
		return responseIdentMapper.map(response);
	}
}
