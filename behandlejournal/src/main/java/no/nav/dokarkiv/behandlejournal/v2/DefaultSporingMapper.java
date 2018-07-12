package no.nav.dokarkiv.behandlejournal.v2;

import no.nav.dokarkiv.core.domain.entities.DokumentInfo;
import no.nav.dokarkiv.core.domain.entities.Journalpost;
import no.nav.dokarkiv.core.sporing.KildeNavnPopulator;
import no.nav.dokarkiv.core.stelvio.RequestContextHolder;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementation of SporingMapper.
 *
 * @author Thomas Eugen Bjørge, Visma Sirius
 */
@Component
public class DefaultSporingMapper implements SporingMapper {

	private final KildeNavnPopulator kildeNavnPopulator;

	@Inject
	DefaultSporingMapper(KildeNavnPopulator kildeNavnPopulator) {
		this.kildeNavnPopulator = kildeNavnPopulator;
	}

	@Override
	public void mapSporingsinfo(Journalpost journalpost, String opprettetEndretAvNavn) {
		if (journalpost != null) {
			mapOpprettetEndretAvNavn(journalpost, opprettetEndretAvNavn);
			mapKilde(journalpost);
		}
	}

	private void mapOpprettetEndretAvNavn(Journalpost journalpost, String navn) {
		if (journalpost.hasId()) {
			journalpost.setEndretAvNavn(navn);
		} else {
			journalpost.setOpprettetAvNavn(navn);
		}
		if (journalpost.getSaksrelasjon() != null && journalpost.getSaksrelasjon().hasId()) {
			journalpost.getSaksrelasjon().setEndretAvNavn(navn);
		}
		for (DokumentInfo dokumentInfo : journalpost.findAllDokumentInfos()) {
			if (dokumentInfo.hasId()) {
				dokumentInfo.setEndretAvNavn(navn);
			}
		}
	}

	private void mapKilde(Journalpost journalpost) {
		String kildeNavn = RequestContextHolder.currentRequestContext().getComponentId();
		kildeNavnPopulator.populateKildeNavnForEntireJournalStructure(journalpost, kildeNavn);
	}
}
