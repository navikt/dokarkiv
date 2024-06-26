package no.nav.dokarkiv.safintern.journalstatus;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.domain.codes.JournalStatusCode;
import no.nav.dokarkiv.core.domain.codes.JournalpostTypeCode;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InvalidFieldRequestedException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternJournalStatusService {
	private static final EnumSet<JournalStatusCode> GYLDIGE_JOURNALSTATUSER = EnumSet.of(JournalStatusCode.U, JournalStatusCode.UB);

	private final SafinternJournalStatusRepository repository;

	public SafinternJournalStatusService(SafinternJournalStatusRepository repository) {
		this.repository = repository;
	}

	public List<JournalpostView> finnJournalposterStatus(FinnJournalposterStatusRequest	finnJournalposterStatusRequest, Set<String> fields) {
		JournalStatusCode journalstatus = finnJournalposterStatusRequest.journalstatus();
		validateJournalstatus(journalstatus);
		try {
			var evs = fetchDokument(fields);
			List<JournalpostView> journalpostViews = repository.finnJournalposterStatus(
					journalstatus,
					finnJournalposterStatusRequest.journalposttyper(),
					parseDate(finnJournalposterStatusRequest),
					finnJournalposterStatusRequest.etterPeker(),
					finnJournalposterStatusRequest.foerste(),
					evs);
			return journalpostViews;
		} catch (EmptyResultDataAccessException|NoResultException e) {
			throw new DokumentInfoIkkeFunnetException("Fant ingen Journalposter med status=" + journalstatus);
		}
	}

	private static  Date parseDate(FinnJournalposterStatusRequest finnJournalposterStatusRequest) {
		return new Date(LocalDate.parse(finnJournalposterStatusRequest.fraDato())
				.atStartOfDay()
				.atZone(ZoneId.systemDefault())
				.toEpochSecond()
		);
	}

	private static void validateJournalstatus(JournalStatusCode journalstatus) {
		if (journalstatus == null || isUgyldigJournalstatus(journalstatus)) {
			throw new UgyldigJournalstatusException();
		}
	}

	private static EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> fetchDokument(Set<String> fields) {
		if (fields == null || fields.isEmpty()) {
			return EntityViewSetting.create(JournalpostView.class);
		}
		var evs = EntityViewSetting.create(JournalpostView.class);
		for (String path : fields) {
			if (FetchPaths.erGyldig(path)) {
				evs.fetch(path);
			} else {
				String feilmelding = "safintern/tilknyttetJournalpost forsøker fetch på ugyldig path=" + path;
				log.error(feilmelding);
				throw new InvalidFieldRequestedException(feilmelding);
			}
		}
		return evs;
	}

	public static boolean isUgyldigJournalstatus(JournalStatusCode journalstatus) {
		return !GYLDIGE_JOURNALSTATUSER.contains(journalstatus);
	}
}
