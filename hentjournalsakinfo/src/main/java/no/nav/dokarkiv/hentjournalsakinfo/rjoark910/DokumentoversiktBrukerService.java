package no.nav.dokarkiv.hentjournalsakinfo.rjoark910;

import static org.apache.commons.lang3.StringUtils.isBlank;

import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktBrukerService {
	private final DokumentoversiktBrukerSpringJdbcRepository dokumentoversiktBrukerSpringJdbcRepository;

	@Inject
	public DokumentoversiktBrukerService(DokumentoversiktBrukerSpringJdbcRepository dokumentoversiktBrukerSpringJdbcRepository) {
		this.dokumentoversiktBrukerSpringJdbcRepository = dokumentoversiktBrukerSpringJdbcRepository;
	}

	public DokumentoversiktBrukerResponseTo hentDokumentoversiktBruker(final DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo) {
		validate(dokumentoversiktBrukerRequestTo);
		final DokumentoversiktBrukerFilter dokumentoversiktBrukerFilter = new DokumentoversiktBrukerFilter(dokumentoversiktBrukerRequestTo);
		List<JournalpostDto> journalpostDtos = dokumentoversiktBrukerSpringJdbcRepository.hentDokumentoversiktBruker(
				dokumentoversiktBrukerFilter
		);
		return new DokumentoversiktBrukerResponseTo(journalpostDtos);
	}

	private void validate(DokumentoversiktBrukerRequestTo dokumentoversiktBrukerRequestTo) {
		if(isBlank(dokumentoversiktBrukerRequestTo.getAktoerId()) && isBlank(dokumentoversiktBrukerRequestTo.getOrgnr())) {
			throw new InvalidDokumentoversiktBrukerRequestException("aktørId og orgnr er blank/null.");
		}
		if(dokumentoversiktBrukerRequestTo.getFraDato() == null) {
			throw new InvalidDokumentoversiktBrukerRequestException("fraDato er null.");
		}
	}
}
