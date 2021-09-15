package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import static no.nav.modig.security.tilgangskontroll.policy.pip.PicketLinkAttributeCacheLocator.log;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
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

	public HentTilgangJournalpostResponse hentTilgangJournalpost(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		try {
			TilgangJournalpostDto tilgangJournalpostDto = hentTilgangJournalpostRepository.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
			if (tilgangJournalpostDto == null) {
				throw new TilgangJournalpostException("Ingen journalpost funnet for journalpostId=" + journalpostId + ", dokumentInfoId=" + dokumentInfoId + ", variantFormat=" + variantFormat.name());
			}
			return HentTilgangJournalpostResponse.builder()
					.tilgangJournalpostDto(tilgangJournalpostDto)
					.build();
		} catch(TilgangJournalpostException e) {
			log.warn("rjoark901 kunne ikke hente TilgangJournalpost. Feilmelding={}", e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			log.error("Ukjent teknisk feil. Feilmelding={}", e.getMessage(), e);
			throw e;
		}
	}
}
