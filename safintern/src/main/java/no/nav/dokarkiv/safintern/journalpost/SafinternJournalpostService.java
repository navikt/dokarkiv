package no.nav.dokarkiv.safintern.journalpost;

import com.blazebit.persistence.CriteriaBuilder;
import com.blazebit.persistence.view.EntityViewSetting;
import lombok.extern.slf4j.Slf4j;
import no.nav.dokarkiv.core.exceptions.JournalpostIkkeFunnetException;
import no.nav.dokarkiv.safintern.views.FetchPaths;
import no.nav.dokarkiv.safintern.views.JournalpostView;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Transactional(readOnly = true)
public class SafinternJournalpostService {

	private final SafinternJournalpostRepository repository;

	public SafinternJournalpostService(SafinternJournalpostRepository repository) {
		this.repository = repository;
	}

	public JournalpostView hentJournalpostById(final Long journalpostId, List<String> fields) {
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = fetch(fields);
		return repository.hentJournalpostById(journalpostId, evs)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet"));
	}

	public JournalpostView hentJournalpostByEksternReferanseId(final String eksternReferanseId, List<String> fields) {
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = fetch(fields);
		return repository.hentJournalpostByEksternReferanseId(eksternReferanseId, evs)
				.orElseThrow(() -> new JournalpostIkkeFunnetException("Journalpost med eksternReferanseId=" + eksternReferanseId + " ikke funnet"));
	}

	@NotNull
	private static EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> fetch(List<String> fields) {
		if (fields == null || fields.isEmpty()) {
			return EntityViewSetting.create(JournalpostView.class);
		}
		EntityViewSetting<JournalpostView, CriteriaBuilder<JournalpostView>> evs = EntityViewSetting.create(JournalpostView.class);
		for (String path : fields) {
			if (FetchPaths.erGyldig(path)) {
				evs.fetch(path);
			} else {
				log.error("safintern/journalpost forsøker fetch på ugyldig path={}", path);
			}
		}
		return evs;
	}
}
