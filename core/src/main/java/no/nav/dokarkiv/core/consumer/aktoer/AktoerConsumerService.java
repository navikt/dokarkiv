package no.nav.dokarkiv.core.consumer.aktoer;

/**
 * Interface for services related to the AktoerConsumer.
 *
 * @author Roar Bjurstrom, Visma Consulting.
 */
public interface AktoerConsumerService {

	/**
	 * Retrives the user's aktoerId and historical idents for a given ident.
	 *
	 * Caches responses.
	 *
	 * @param request The request with the ident
	 * @return aktoerId and all historical idents
	 * @throws PersonIkkeFunnetException Thrown if no person is found for the given ident
	 */
	HentAktoerIdForIdentResponseTo hentAktoerIdForIdent(HentAktoerIdForIdentRequestTo request) throws PersonIkkeFunnetException;

	/**
	 * Retrives the user's aktoerId and historical idents for a given ident.
	 *
	 * Caches responses.
	 *
	 * @param request The request with the aktoerId
	 * @return ident
	 * @throws PersonIkkeFunnetException Thrown if no person is found for the given aktoerId
	 */
	HentIdentForAktoerIdResponseTo hentIdentForAktoerId(HentIdentForAktoerIdRequestTo request) throws PersonIkkeFunnetException;
}
