package no.nav.dokarkiv.hentjournalsakinfo.rjoark904;

import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.hentjournalsakinfo.JournalpostFilter;
import no.nav.dokarkiv.hentjournalsakinfo.dto.JournalpostDto;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

@Component
public class FinnJournalposterStatusService {
	private static final EnumSet GYLDIGE_JOURNALSTATUSER = EnumSet.of(JournalStatusCode.U, JournalStatusCode.UB);
	private final FinnJournalposterStatusJdbcRepository finnJournalposterStatusJdbcRepository;

	public FinnJournalposterStatusService(FinnJournalposterStatusJdbcRepository finnJournalposterStatusJdbcRepository) {
		this.finnJournalposterStatusJdbcRepository = finnJournalposterStatusJdbcRepository;
	}

	public FinnJournalposterStatusResponseTo finnJournalposterStatus(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		validateRequest(finnJournalposterStatusRequestTo);
		List<JournalpostDto> journalpostDtos = finnJournalposterStatusJdbcRepository.finnJournalposterStatus(new JournalpostFilter(finnJournalposterStatusRequestTo));
		return new FinnJournalposterStatusResponseTo(journalpostDtos);
	}

	private void validateRequest(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		if (finnJournalposterStatusRequestTo.getJournalstatus() == null || isGyldigJournalstatus(finnJournalposterStatusRequestTo)) {
			throw new UgyldigJournalstatusException();
		}
	}

	private boolean isGyldigJournalstatus(FinnJournalposterStatusRequestTo finnJournalposterStatusRequestTo) {
		return !GYLDIGE_JOURNALSTATUSER.contains(finnJournalposterStatusRequestTo.getJournalstatus());
	}
}
