package no.nav.dokarkiv.dokumentproduksjoninfo.tjoark122;

import no.nav.tjeneste.domene.brevogarkiv.dokumentproduksjoninfo.v1.meldinger.HentJournalpostInfoResponse;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class HentJournalpostInfoResponseMapper {
	public HentJournalpostInfoResponse map(HentJournalpostInfoResponseTo responseTo) {
		return new HentJournalpostInfoResponse()
				.withJournalStatus(responseTo.getJournalStatus() == null ? null : responseTo.getJournalStatus().name())
				.withDokumentStatus(responseTo.getDokumentStatus() == null ? null : responseTo.getDokumentStatus().name())
				.withMetaForceInstanceId(responseTo.getMetaforceInstanceId())
				.withJournalfEnhet(responseTo.getJournalfEnhet())
				.withFagomrade(responseTo.getFagomrade() == null ? null : responseTo.getFagomrade().name())
				.withBrukerId(responseTo.getBrukerId())
				.withBrukerType(responseTo.getBrukerType() == null ? null : responseTo.getBrukerType().name())
				.withSaksNummer(responseTo.getSaksNummer())
				.withFagsystem(responseTo.getFagsystem() == null ? null : responseTo.getFagsystem().name())
				.withAntallRetur(responseTo.getAntallRetur());
	}
}
