package no.nav.dokarkiv.innsynjournal.v2.tjoark053;

import no.nav.dokarkiv.core.domain.codes.FagsystemCode;
import no.nav.dokarkiv.core.repository.journalpostliste.SakFagsystem;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.HentTilgjengeligJournalpostListeRequest;
import org.springframework.stereotype.Component;

/**
 * Mapping from {@link HentTilgjengeligJournalpostListeRequest} to {@link HentJournalpostListeToRequest}
 *
 * @author Torgeir Cook, Visma Consulting.
 */
@Component
public class HentMinTilgjengeligeJournalpostListeV2RequestMapper {

	public HentJournalpostListeToRequest map(HentTilgjengeligJournalpostListeRequest request) {
		final HentJournalpostListeToRequest toRequest = HentJournalpostListeToRequest.builder()
				.merkInnsynDokument(request.isMerkInnsynDokument())
				.build();
		request.getSakListe().forEach(sak -> toRequest.getSaksListe().add(
				new SakFagsystem(sak.getFagsystem() == null ? null : FagsystemCode.valueOf(sak.getFagsystem().getValue()),
						sak.getSakId())
		));
		return toRequest;
	}
}
