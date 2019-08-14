package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.hentjournalsakinfo.rjoark900.JournalpostFilter;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class FinnJournalposterStatusService {
	private static final EnumSet SOEKBARE_JOURNALSTATUSER = EnumSet.of(JournalStatusCode.U, JournalStatusCode.UB);
	private final FinnJournalposterStatusJdbcRepository finnJournalposterStatusJdbcRepository;

	@Inject
	public FinnJournalposterStatusService(FinnJournalposterStatusJdbcRepository finnJournalposterStatusJdbcRepository) {
		this.finnJournalposterStatusJdbcRepository = finnJournalposterStatusJdbcRepository;
	}

	public FinnJournalposterStatusResponseTo finnJournalposterStatus(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		validateRequest(finnJournalposterStatusRequestTo);
		List<JournalpostDto> journalpostDtos = finnJournalposterStatusJdbcRepository.finnJournalposterStatus(new JournalpostFilter(finnJournalposterStatusRequestTo));
		return new FinnJournalposterStatusResponseTo(journalpostDtos);
	}

	private void validateRequest(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		if (finnJournalposterStatusRequestTo.getJournalstatus() == null || isSoekbarJournalstatus(finnJournalposterStatusRequestTo)) {
			throw new UgyldigJournalstatusException();
		}
	}

	private boolean isSoekbarJournalstatus(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		return !SOEKBARE_JOURNALSTATUSER.contains(finnJournalposterStatusRequestTo.getJournalstatus());
	}
}
