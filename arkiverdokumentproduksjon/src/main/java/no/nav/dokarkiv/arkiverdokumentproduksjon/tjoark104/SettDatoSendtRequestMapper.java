package no.nav.dokarkiv.arkiverdokumentproduksjon.tjoark104;

import no.nav.service.dok.joark.nsb.to.SettDatoSendtRequestTo;
import no.nav.tjeneste.domene.brevogarkiv.arkiverdokumentproduksjon.v1.meldinger.SettDatoSendtRequest;

/**
 * Maps SettDatoSendt requests
 *
 * @author Joakim Bjørnstad, Visma Consulting
 */
public interface SettDatoSendtRequestMapper {
	/**
	 * Map from webservice request to domain transfer object
	 *
	 * @param request webservice request
	 * @return domain request transfer object
	 */
	SettDatoSendtRequestTo map(SettDatoSendtRequest request);
}
