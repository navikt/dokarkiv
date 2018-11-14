package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import static no.nav.modig.security.tilgangskontroll.policy.pip.PicketLinkAttributeCacheLocator.log;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import no.nav.dokarkiv.hentjournalsakinfo.dto.TilgangJournalpostDto;
import no.nav.dokarkiv.hentjournalsakinfo.exceptions.TilgangJournalpostException;
import org.springframework.stereotype.Service;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Service
public class HentTilgangJournalpostService {

	private final HentTilgangJournalpostRepository hentTilgangJournalpostRepository;

	public HentTilgangJournalpostService(HentTilgangJournalpostRepository hentTilgangJournalpostRepository) {
		this.hentTilgangJournalpostRepository = hentTilgangJournalpostRepository;
	}

	public TilgangJournalpostDto hentTilgangJournalpost(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		try {
			return hentTilgangJournalpostRepository.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
		} catch (Exception e) {
			log.error("rjoark901 kunne ikke hente TilgangJournalpost. Ingen treff på journalpostId={}, dokumentInfoId={} og variantFormat={}. Feilmelding: {}",
					journalpostId, dokumentInfoId, variantFormat.name(), e.getMessage());
			throw new TilgangJournalpostException(String.format("rjoark901 kunne ikke hente TilgangJournalpost. Ingen treff på journalpostId=%s, dokumentInfoId=%s og variantFormat=%s. Feilmelding: %s",
					journalpostId, dokumentInfoId, variantFormat.name(), e.getMessage()), e);
		}
	}
}
