package no.nav.dokarkiv.safintern.tilknyttetjournalpost;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import jakarta.persistence.NoResultException;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.DokumentInfoIkkeFunnetException;
import no.nav.dokarkiv.core.exceptions.InvalidFieldRequestedException;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternTilknyttetJournalpostService {

	private final SafinternTilknyttetJournalpostRepository repository;

	public SafinternTilknyttetJournalpostService(SafinternTilknyttetJournalpostRepository repository) {
		this.repository = repository;
	}

	public List<JournalpostView> hentJournalposterTilknyttetGjenbruk(long dokumentInfoId, Set<String> fields) {
		try {
			var evs = fetchDokument(fields);
			List<JournalpostView> journalpostViews = repository.hentTilknyttedeJournalposterGjenbruk(dokumentInfoId, evs);
			if (journalpostViews.isEmpty()) {
				throw new JournalpostIkkeFunnetException("Fant ingen Journalpost tilknyttet dokumentInfoId=" + dokumentInfoId);
			}
			return journalpostViews;
		} catch (EmptyResultDataAccessException|NoResultException e) {
			throw new DokumentInfoIkkeFunnetException("Fant ingen DokumentInfo med DokumentInfoId=" + dokumentInfoId);
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
}
