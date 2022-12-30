package no.nav.dokarkiv.hentjournalsakinfo.rjoark901;

import no.nav.dokarkiv.core.domain.codes.VariantFormatCode;
import org.springframework.stereotype.Service;

import static no.nav.modig.security.tilgangskontroll.policy.pip.PicketLinkAttributeCacheLocator.log;

@Service
public class HentTilgangJournalpostService {

	private final HentTilgangjournalpostTestRepository hentTilgangjournalpostTestRepository;

	public HentTilgangJournalpostService(HentTilgangjournalpostTestRepository hentTilgangjournalpostTestRepository) {
		this.hentTilgangjournalpostTestRepository = hentTilgangjournalpostTestRepository;
	}

	public HentTilgangJournalpostResponse hentTilgangJournalpost(Long journalpostId, Long dokumentInfoId, VariantFormatCode variantFormat) {
		try {
			TilgangJournalpostDto tilgangJournalpostDto = hentTilgangjournalpostTestRepository.hentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat);
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
